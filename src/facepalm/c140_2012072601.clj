(ns facepalm.c140-2012072601
  (:use [korma.core]
        [kameleon.core]))

(def ^:private version
  "The destination database version."
  "1.4.0:20120726.01")

(defn- add-tool-types-table
  "Adds the tool_types table and its ID sequence."
  (println "\t* adding the tool_types table")
  (exec-raw
   "CREATE SEQUENCE tool_types_id_seq
        START WITH 1
        INCREMENT BY 1
        NO MAXVALUE
        NO MINVALUE
        CACHE 1")
  (exec-raw
   "CREATE TABLE tool_types (
        id bigint DEFAULT nextval('tool_types_id_seq'::regclass) NOT NULL,
        name varchar(50) UNIQUE NOT NULL,
        label varchar(128) NOT NULL,
        description varchar(256),
        PRIMARY KEY(id))"))

(defn- populate-tool-types-table
  "Populates the tool_types table."
  []
  (println "\t* populating the tool_types table")
  (insert tool_types
          (values {:name        "executable"
                   :label       "UA"
                   :description "Run at the University of Arizona"})
          (values {:name        "fAPI"
                   :label       "TACC"
                   :description "Run at the Texas Advanced Computing Center"})))

(defn- add-tool-type-id-to-deployed-components
  "Adds the foreign key, tool_type_id, to the deployed_components table"
  []
  (println "\t* referencing tool_types from deployed_components")
  (exec-raw
   "ALTER TABLE deployed_components
        ADD COLUMN tool_type_id bigint REFERENCES tool_types(id)"))

(defn- associate-deployed-components-with-tool-types
  "Associates existing deployed components with tool types."
  []
  (println "\t* associating existing deployed components with tool types")
  (update deployed_components
          (set-fields
           {:tool_type_id (subselect tool_types
                                     (fields :id)
                                     (where {:deployed_components.type
                                             :tool_types.name}))}))
  (update deployed_components
          (set-fields
           {:tool_type_id (subselect tool_types
                                     (fields :id)
                                     (where {:name "executable"}))})
          (where {:tool_type_id nil})))

(defn- remove-deployed-component-type
  "Removes the type column from the deployed_components table."
  []
  (println "\t* removing the type column from the deployed_components table")
  (exec-raw "ALTER TABLE deployed_components DROP COLUMN type"))

(defn- add-tool-type-id-constraints
  "Adds constraints to the tool_type_id column of the deployed_components
   table."
  []
  (println "\t* adding a not-null constraint to the tool_type_id column")
  (exec-raw
   "ALTER TABLE deployed_components ALTER COLUMN tool_type_id SET NOT NULL"))

;; TODO: redefine the analysis_job_types, analysis_listing and
;; deployed_component_listing views.

(defn convert
  "Performs the conversions for database version 1.40:20120726.01."
  []
  (println "Performing conversion for" version)
  (add-tool-types-table)
  (populate-tool-types-table)
  (add-tool-type-id-to-deployed-components)
  (associate-deployed-components-with-tool-types)
  (remove-deployed-component-type)
  (add-tool-type-id-constraints))
