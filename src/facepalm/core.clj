(ns facepalm.core
  (:gen-class)
  (:use [clojure.java.io :only [reader]]
        [clojure.tools.cli :only [cli]]
        [kameleon.core]
        [kameleon.entities]
        [korma.core]
        [korma.db])
  (:require [clojure.tools.logging :as log]
            [kameleon.pgpass :as pgpass])
  (:import [org.apache.log4j BasicConfigurator ConsoleAppender Level
            SimpleLayout]))

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
       ["--debug" "Enable debugging." :default false :flag true]))

(defn- configure-logging
  "Configures logging for this tool.  All logging is printed on the console,
   but the logging level may be changed."
  [opts]
  (BasicConfigurator/configure
   (doto (ConsoleAppender.)
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

(defn -main
  "Parses the command-line options and performs the database updates."
  [& args-vec]
  (let [[opts args banner] (parse-args args-vec)]
    (when (:help opts)
      (println banner)
      (System/exit 0))
    (configure-logging opts)
    (define-db opts)
    (exec-raw ["INSERT INTO users (username) VALUES ('foo@iplantcollaborative.org');"])
    (println (select users))))
