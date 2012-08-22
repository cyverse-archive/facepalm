(ns facepalm.c140-2012082201
  (:use [korma.core]
        [kameleon.core]
        [kameleon.entities :only [validator property_group]])
  (:require [clojure.string :as string])
  (:import [java.util UUID]))

(def ^:private version
  "The destination database version."
  "1.4.0:20120726.01")

;; An entity that can be used to find duplicated validator IDs.
(defentity validator_count
  (table (subselect validator
                    (fields :id [(sqlfn count :id) :count])
                    (group :id))
         :validator_count))

;; An entity that can be used to find duplicated property group IDs.
(defentity property_group_count
  (table (subselect property_group
                    (fields :id [(sqlfn count :id) :count])
                    (group :id))
         :property_group_count))

(defn- uuid []
  "Generates a random UUID."
  (-> (UUID/randomUUID)
      str
      string/upper-case))

(defn- find-duplicated-validator-ids
  "Finds the list of duplicated validator identifiers in the database."
  []
  (map :id (select validator_count (where {:count [> 1]}))))

(defn- update-validator-id
  "Updates the identifier of a validator."
  [{:keys [hid]}]
  (update validator
          (set-fields {:id (uuid)})
          (where {:hid hid})))

(defn- fix-duplicated-validator-ids
  "Removes duplicate validator identifiers from the database."
  ([]
     (println "\t* eliminating duplicate validator IDs")
     (dorun (map fix-duplicated-validator-ids (find-duplicated-validator-ids))))
  ([id]
     (dorun (map update-validator-id
                 (drop 1 (select validator (where {:id id})))))))

(defn- find-duplicated-property-group-ids
  "Finds the list of duplicated property group identifiers in the database."
  []
  (map :id (select property_group_count (where {:count [> 1]}))))

(defn- update-property-group-id
  "Updates the identifier of a property group."
  [{:keys [hid]}]
  (update property_group
          (set-fields {:id (uuid)})
          (where {:hid hid})))

(defn- fix-duplicated-property-group-ids
  "Removes duplicated property group identifiers from the database."
  ([]
     (println "\t* eliminating duplicate property group IDs")
     (dorun (map fix-duplicated-property-group-ids
                 (find-duplicated-property-group-ids))))
  ([id]
     (dorun (map update-property-group-id
                 (drop 1 (select property_group (where {:id id})))))))

(defn convert
 "Performs the conversions for database version 1.4.0:20120822.01."
 []
 (println "Performing conversion for" version)
 (fix-duplicated-validator-ids)
 (fix-duplicated-property-group-ids))
