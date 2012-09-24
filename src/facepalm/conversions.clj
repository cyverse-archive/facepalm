(ns facepalm.conversions
  (:require [clojure.string :as string]
            [clojure-commons.file-utils :as fu]))

(defn drop-extension
  [fname]
  (first (string/split fname #"\.")))

(defn split-on-dash
  [fname]
  (string/split fname #"\-"))

(defn dotize
  [vstr]
  (string/join "." (into [] vstr)))

(defn fmt-version
  [[version-str date-str]]
  [(-> version-str
       (string/replace #"^c" "")
       dotize)
   date-str])

(defn fmt-date-str
  [date-str]
  (let [date-vec (into [] date-str)]
    (str
     (string/join (take 8 date-vec)) "." (string/join (take-last 2 date-vec)))))

(defn fmt-date
  [[vstr date-str]]
  [vstr (fmt-date-str date-str)])

(defn db-version
  [parts]
  (string/join ":" parts))

(defn fname->db-version
  [fname]
  (-> fname
      drop-extension
      split-on-dash
      fmt-version
      fmt-date
      db-version))

