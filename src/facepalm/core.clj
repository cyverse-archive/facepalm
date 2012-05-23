(ns facepalm.core
  (:gen-class)
  (:use [clojure.java.io :only [copy file reader]]
        [clojure.tools.cli :only [cli]]
        [kameleon.core]
        [kameleon.entities]
        [kameleon.sql-reader :only [sql-statements]]
        [korma.core]
        [korma.db]
        [slingshot.slingshot :only [throw+ try+]])
  (:require [clojure.string :as string]
            [clojure.tools.logging :as log]
            [clj-http.client :as client]
            [kameleon.pgpass :as pgpass])
  (:import [java.io File]
           [java.sql SQLException]
           [org.apache.log4j BasicConfigurator ConsoleAppender Level
            SimpleLayout]))

(def ^:private jenkins-base
  "The base URL used to connect to Jenkins."
  "http://projects.iplantcollaborative.org/hudson")

(def ^:private qa-drop-base
  "The base URL for the QA drops."
  "http://projects.iplantcollaborative.org/qa-drops")

(def ^:private build-artifact-name
  "The name of the build artifact to retrieve."
  "database.tar.gz")

(def ^:private max-temp-dir-attempts
  "The maximum number of times to attempt to create a temporary directory."
  10)

(defn- parse-args
  "Parses the command-line arguments."
  [args]
  (cli args
       ["-?" "--help" "Show help." :default false :flag true]
       ["-h" "--host" "The database hostname." :default "localhost"]
       ["-p" "--port" "The database port number." :default 5432
        :parse-fn #(Integer. %)]
       ["-d" "--database" "The database name." :default "de"]
       ["-U" "--user" "The database username." :default "de"]
       ["-j" "--job" "The name of DE database job in Jenkins."
        :default "database"]
       ["-q" "--qa-drop" "The QA drop date to use when retrieving"]
       ["-f" "--filename" "An explicit path to the database tarball."]
       ["--debug" "Enable debugging." :default false :flag true]))

(defn- pump
  "Pumps data obtained from a reader to an output stream.  Copied shamelessly
   from leiningen.core.eval/pump."
  [reader out]
  (let [buffer (char-array 1024)]
    (loop [len (.read reader buffer)]
      (when-not (neg? len)
        (.write out buffer 0 len)
        (.flush out)
        (Thread/sleep 100)
        (recur (.read reader buffer))))))

(defn- sh
  "A version of clojure.java.shell/sh that streams out/err.  Copied shamelessly
   from leiningen.core.eval/sh.  This version of (sh) is being used because
   clojure.java.shell/sh wasn't calling .destroy on the process, which was
   preventing this program from exiting in a timely manner.  It's also
   convenient to be able to stream standard output and standard error output to
   the user's terminal session."
  [& cmd]
  (log/debug "Executing command:" (string/join " " cmd))
  (let [proc (.exec (Runtime/getRuntime) (into-array cmd))]
    (.addShutdownHook (Runtime/getRuntime)
                      (Thread. (fn [] (.destroy proc))))
    (with-open [out (reader (.getInputStream proc))
                err (reader (.getErrorStream proc))]
      (let [pump-out (doto (Thread. #(pump out *out*)) .start)
            pump-err (doto (Thread. #(pump err *err*)) .start)]
        (.join pump-out)
        (.join pump-err))
      (.waitFor proc))))

(defn- create-layout
  "Creates the layout that will be used for log messages."
  []
  (doto (SimpleLayout.)
    (.activateOptions)))

(defn- configure-logging
  "Configures logging for this tool.  All logging is printed on the console,
   but the logging level may be changed."
  [opts]
  (BasicConfigurator/configure
   (doto (ConsoleAppender. (create-layout))
     (.setLayout (SimpleLayout.))
     (.setName "Console")
     (.setThreshold (if (:debug opts) Level/DEBUG Level/INFO)))))

(defn- get-password
  "Attempts to obtain the database password from the user's .pgpass file.  If
   the password can't be obtained from .pgpass, prompts the user for the
   password"
  [host port database user]
  (let [password (pgpass/get-password host port database user)]
    (if (nil? password)
      (do
        (print "Password: ")
        (flush)
        (apply str (.. System console readPassword)))
      password)))

(defn- define-db
  "Defines the database connection settings."
  [{:keys [host port database user]}]
  (let [password (get-password host port database user)]
    (defdb de {:classname "org.postgresql.Driver"
               :subprotocol "postgresql"
               :subname (str "//" host ":" port "/" database)
               :user user
               :password password})))

(defn- jenkins-build-artifact-url
  "Returns the URL used to obtain the build artifact from Jenkins.  We assume
   that the name of the artifiact is database.tar.gz."
  [job-name]
  (str jenkins-base "/job/" job-name "/lastSuccessfulBuild/artifact/"
       build-artifact-name))

(defn- qa-drop-build-artifact-url
  "Returns the URL used to obtain the build artifact from a QA drop.  We assume
   that the name of the artifact is database.tar.gz."
  [drop-date]
  (str qa-drop-base "/" drop-date "/" build-artifact-name))

(defn- required-option-error
  "Throws an exception indicating that required arguments are missing."
  [& opt-names]
  (throw+ {:type ::required-options-missing
           :opt-names opt-names}))

(defn- build-artifact-url
  "Builds and returns the URL used to obtain the build artifact."
  [{:keys [qa-drop filename job]}]
  (cond (not (string/blank? qa-drop))  (qa-drop-build-artifact-url qa-drop)
        (not (string/blank? job))      (jenkins-build-artifact-url job)
        :else                          (required-option-error :job :qa-drop)))

(defn- get-build-artifact-from-url
  "Gets the build artifact from a URL."
  [dir opts]
  (let [{:keys [status body]} (client/get (build-artifact-url opts)
                                          {:as :stream})]
    (if-not (< 199 status 300)
      (throw+ {:type ::build-artifact-retrieval-failed}))
    (with-open [in body]
      (copy in (file dir build-artifact-name)))))

(defn get-build-artifact-from-file
  "Gets the build artifact from a local file."
  [dir filename]
  (let [f (file filename)]
    (copy f (file (file dir (.getName f))))))

(defn- get-build-artifact
  "Obtains the database build artifact."
  [dir opts]
  (println "Retrieving the build artifact...")
  (let [filename (:filename opts)]
    (if (string/blank? filename)
      (get-build-artifact-from-url dir opts)
      (get-build-artifact-from-file dir filename))))

(defn- unpack-build-artifact
  "Unpacks the database build artifact after it has been obtained."
  [dir]
  (println "Unpacking the build artifact...")
  (let [file-path   (.getPath (file dir build-artifact-name))
        exit-status (sh "tar" "xvf" file-path "-C" (.getPath dir))]
    (when-not (zero? exit-status)
      (throw+ {:type ::build-artifact-expansion-failed}))))

(defn- rec-delete
  "Recursively deletes all files in a directory structure rooted at the given
   directory.  Note that this recursion does consume stack space.  This
   shouldn't be a problem, however, because a directory structure that is deep
   enough to cause a stack overflow will probably create a path that is too
   long for the OS to support."
  [f]
  (when (.isDirectory f)
    (dorun (map #(rec-delete %) (.listFiles f))))
  (log/debug "deleting" (.getPath f))
  (.delete f))

(defn- mk-temp-dir
  "Attempts to create a temporary directory named by the provided function."
  [name-fn]
  (loop [idx 0]
    (if-not (>= idx max-temp-dir-attempts)
      (let [f (name-fn idx)]
        (log/debug "attempting to create temporary directory" (.getPath f))
        (if (.mkdir f) f (recur (inc idx))))
      nil)))

(defn- temp-dir
  "Creates a temporary directory."
  [prefix parent]
  (let [base          (str prefix (System/currentTimeMillis) "-")
        temp-dir-file (fn [idx] (file parent (str base idx)))
        temp-dir      (mk-temp-dir temp-dir-file)]
    (when (nil? temp-dir)
      (throw+ {:type   ::temp-directory-creation-failure
               :parent (.getPath parent)
               :prefix prefix
               :base   base}))
    (log/debug "created temporary directory:" (.getPath temp-dir))
    temp-dir))

(defmacro ^:private with-temp-dir
  "Creates and switches the current working directory to a temporary directory.
   The body is executed in a try expression with a finally clause that
   recursively deletes the directory."
  [sym & body]
  `(let [~sym (temp-dir "-fp-" (file (System/getProperty "user.dir")))]
     (try
       (.delete ~sym)
       (.mkdir ~sym)
       ~@body
       (finally (rec-delete ~sym)))))

(defn exec-sql-statement
  "A wrapper around korma.core/exec-raw that logs the statement that is being
   executed if debugging is enabled."
  [statement]
  (log/debug "executing SQL statement:" statement)
  (exec-raw statement))

(defn- load-sql-file
  "Loads a single SQL file into the database."
  [sql-file]
  (println (str "Loading " (.getName sql-file) "..."))
  (with-open [rdr (reader sql-file)]
    (dorun (map exec-sql-statement (sql-statements rdr)))))

(defn- load-sql-files
  "Loads SQL files from a subdirectory of the artifact directory."
  [parent subdir-name]
  (let [subdir (file parent subdir-name)]
    (dorun (map load-sql-file
                (sort-by #(.getName %) (.listFiles subdir))))))

(defn- refresh-public-schema
  "Refreshes the public shema associated with the database."
  [user]
  (println "Refreshing the public schema...")
  (dorun (map exec-raw
              ["DROP SCHEMA public CASCADE"
               "CREATE SCHEMA public"
               (str "ALTER SCHEMA public OWNER TO " user)])))

(defn- log-next-exception
  "Calls getNextException and logs the resulting exception for any SQL
   exception in the cause stack."
  [e]
  (when-not (nil? e)
    (if (instance? SQLException e)
      (log/error (.getNextException e) "next exception"))
    (recur (.getCause e))))

(defn- apply-database-init-scripts
  "Applies the database initialization scripts to the database."
  [dir opts]
  (try+
    (refresh-public-schema (:user opts))
    (dorun (map #(load-sql-files dir %) ["tables" "views" "data"]))
    (catch Exception e
      (log-next-exception e)
      (throw+))))

(defn- initialize-database
  "Initializes the database using a database archive obtained from a well-known
   location."
  [opts]
  (with-temp-dir dir
    (get-build-artifact dir opts)
    (unpack-build-artifact dir)
    (apply-database-init-scripts dir opts)))

(defn -main
  "Parses the command-line options and performs the database updates."
  [& args-vec]
  (let [[opts args banner] (parse-args args-vec)]
    (when (:help opts)
      (println banner)
      (System/exit 0))
    (configure-logging opts)
    (log/debug opts)
    (define-db opts)
    (initialize-database opts)))
