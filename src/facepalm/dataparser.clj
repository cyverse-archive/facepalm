(ns facepalm.dataparser
  (:gen-class)
  (:use
      [clojure.data.json :only (read-json)]
      [conrad.genome-reference :only (uuid-gen)]))

;These are helper functions to be run in the repl to format new genome
;references for the de-database-schema instead of tedious copy pasta.

(def wrapper-open "INSERT INTO genome_reference
                  (uuid, name, path, created_by, last_modified_by)
                  \nVALUES (")

(def wrapper-close ",0,0);")

(def datamap
    (map identity (read-json
        (slurp "/Users/rchasman/Downloads/reference_genomes.json"))))

(defn wrap
[[name path]]
(str (identity wrapper-open) "'" (uuid-gen) "', '" (identity name) "', '"
     (identity path) "'" (identity wrapper-close) "\n\n"))

(defn output
[]
(spit "/Users/rchasman/results.txt" (println-str(map wrap datamap))))
