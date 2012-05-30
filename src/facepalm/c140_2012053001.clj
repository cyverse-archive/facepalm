(ns facepalm.c140-2012053001
  (:use [korma.core]))

(def ^:private version
  "The destination database version."
  "1.4.0:20120530.01")

(defn add-genome-reference-table
  "Adds the table used to track reference genomes."
  []
  (println "\t* adding the genome_reference table")
  (exec-raw "CREATE SEQUENCE genome_ref_id_seq")
  (exec-raw
   "CREATE TABLE genome_reference (
        id bigint DEFAULT nextval('genome_ref_id_seq'),
        uuid uuid NOT NULL,
        name varchar(512) NOT NULL,
        path text NOT NULL,
        deleted boolean DEFAULT false NOT NULL,
        created_by bigint references users(id),
        created_on timestamp DEFAULT now() NOT NULL,
        last_modified_by bigint references users(id),
        last_modified_on timestamp,
        PRIMARY KEY(id)
    )"))

(defn add-collaborators-table
  "Adds the table used to track collaborators."
  []
  (println "\t* adding the collaborators table")
  (exec-raw "CREATE SEQUENCE collaborators_id_seq")
  (exec-raw
   "CREATE TABLE collaborators (
        id bigint DEFAULT nextval('collaborators_id_seq'),
        user_id bigint NOT NULL references users(id),
        collaborator_id bigint NOT NULL references users(id),
        PRIMARY KEY(id)
    )"))

(defn convert
  "Performs the conversion for database version 1.4.0:20120530.01."
  []
  (println "Performing conversion for" version)
  (add-genome-reference-table)
  (add-collaborators-table))
