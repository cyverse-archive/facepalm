(ns facepalm.c140-2012061801
  (:use [korma.core]
        [kameleon.core]
        [kameleon.entities :only [integration_data]]))

(def ^:private version
  "The destination database version."
  "1.4.0:20120618.01")

(defn- dedup-integration-data-for-deployed-components
  "Ensures that all deployed components that point to integration data elements
   with the same name and e-mail address actually point to the same integration
   data element.  I didn't see an obvious way to use Korma for this update, so
   I decided to go with raw SQL."
  []
  (println "\t* deduplicating integration data for deployed components")
  (exec-raw
   "UPDATE deployed_components dc
    SET integration_data_id = (
        SELECT id2.id
        FROM integration_data id1
        JOIN integration_data id2
            ON id1.integrator_name = id2.integrator_name
            AND id1.integrator_email = id2.integrator_email
        WHERE id1.id = dc.integration_data_id
        ORDER BY id2.id
        LIMIT 1)"))

(defn- dedup-integration-data-for-analyses
  "Ensures that all analyses that point to integration data elements with the
   same name ande e-mail address actually point to the same integration data
   element.  I didn't see an obvious way to use Korma for this update, so I
   decided to go with raw SQL."
  []
  (println "\t* deduplicating integration data for analyses")
  (exec-raw
   "UPDATE transformation_activity a
    SET integration_data_id = (
        SELECT id2.id
        FROM integration_data id1
        JOIN integration_data id2
            ON id1.integrator_name = id2.integrator_name
            AND id1.integrator_email = id2.integrator_email
        WHERE id1.id = a.integration_data_id
        ORDER BY id2.id
        LIMIT 1)"))

(defn- remove-unreferenced-integration-data-elements
  "Removes any integration data elements that are no longer referenced."
  []
  (println "\t* removing unreferenced integration data elements")
  (exec-raw
   "DELETE FROM integration_data id
    WHERE NOT EXISTS (
        SELECT * FROM transformation_activity a
        WHERE a.integration_data_id = id.id)
    AND NOT EXISTS (
        SELECT * FROM deployed_components dc
        WHERE dc.integration_data_id = id.id)"))

(defn- add-integration-data-uniqueness-constraint
  "Adds the uniqueness constraint to the integration_data table."
  []
  (println "\t* adding a uniqueness constraint to the integration_data table.")
  (exec-raw
   "ALTER TABLE ONLY integration_data
    ADD CONSTRAINT integration_data_name_email_unique
    UNIQUE (integrator_name, integrator_email);"))

(defn convert
  "Performs the conversions for database version 1.40:20120618.01."
  []
  (println "Performing conversion for" version)
  (dedup-integration-data-for-deployed-components)
  (dedup-integration-data-for-analyses)
  (remove-unreferenced-integration-data-elements)
  (add-integration-data-uniqueness-constraint))
