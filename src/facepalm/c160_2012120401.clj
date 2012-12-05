(ns facepalm.c160-2012120401
  (:use [korma.core]
        [kameleon.core]))

(def ^:private version
  "The destination database version."
  "1.6.0:20121204.01")

(defn- remove-public-apps-from-workspaces
  "Removes any apps that are marked as public from any user's 'Apps under development' group."
  []
  (println "\t* removing public apps from users' workspaces.")
  (delete :template_group_template
          (where {(subselect :template_group
                             (fields :name)
                             (where {:template_group_template.template_group_id
                                     :template_group.hid}))
                  "Applications under development"})
          (where {(subselect :analysis_listing
                             (fields :is_public)
                             (where {:template_group_template.template_id
                                     :analysis_listing.hid}))
                  true})))

(defn convert
  "Performs the conversions for database version 1.6.0:20121204.01"
  []
  (println "Performing the conversion for" version)
  (remove-public-apps-from-workspaces))
