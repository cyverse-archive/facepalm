(ns facepalm.conversions
  (:require [clojure.string :as string]
            [clojure-commons.file-utils :as fu]))

(defn- drop-extension
  [fname]
  (first (string/split fname #"\.")))

(defn- split-on-underscore
  [fname]
  (string/split fname #"_"))

(defn- dotize
  [vstr]
  (string/join "." (into [] vstr)))

(defn- fmt-version
  [[version-str date-str]]
  [(-> version-str
       (string/replace #"^c" "")
       dotize)
   date-str])

(defn- fmt-date-str
  [date-str]
  (let [date-vec (into [] date-str)]
    (str
     (string/join (take 8 date-vec)) "." (string/join (take-last 2 date-vec)))))

(defn- fmt-date
  [[vstr date-str]]
  [vstr (fmt-date-str date-str)])

(defn- db-version
  [parts]
  (string/join ":" parts))

(defn- fname->db-version
  [fname]
  (-> fname
      fu/basename
      drop-extension
      split-on-underscore
      fmt-version
      fmt-date
      db-version))

(defn- fname->ns-str
  [fname]
  (-> (str "facepalm." fname)
      (string/replace #"\.clj$" "")
      (string/replace #"_" "-")))

(defn- ns-str->cv-str
  [ns-str]
  (str ns-str "/convert"))

(defn- fname->cv-ref
  [fname]
  (-> fname
      fu/basename
      fname->ns-str
      ns-str->cv-str
      symbol
      eval))

(defn- list-conversions
  [dir]
  (filter
   #(re-seq #"^c.*_[0-9]{10}\.clj$" (fu/basename %1))
   (map
    str
    (.listFiles (clojure.java.io/file (fu/path-join dir "conversions"))))))

(defn- load-conversions
  [cv-list]
  (doseq [cv cv-list]
    (load-file cv)))

(defn conversion-map
  [dir]
  (let [conversions (list-conversions dir)]
    (load-conversions conversions)
    (into {} (map #(vector (fname->db-version %) (fname->cv-ref %)) conversions))))
