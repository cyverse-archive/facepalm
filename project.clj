
;; IMPORTANT NOTE: Both an RPM and a tarball are generated for this project.
;; Because the release number is not recorded anywhere in the tarball, minor
;; changes need to be recorded in the version number.  Please increment the
;; minor version number rather than the release number for minor changes.

(defproject facepalm "1.0.0-SNAPSHOT"
  :description "Command-line utility for DE database managment."
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure/tools.cli "0.2.1"]
                 [org.clojure/tools.logging "0.2.3"]
                 [korma "0.3.0-beta7"]
                 [org.iplantc/kameleon "0.0.1-SNAPSHOT"]
                 [postgresql "9.0-801.jdbc4"]
                 [slingshot "0.10.2"]
                 [clj-http "0.4.1"]]
  :plugins [[org.iplantc/lein-iplant-cmdtar "0.1.0-SNAPSHOT"]
            [org.iplantc/lein-iplant-rpm "1.2.0-SNAPSHOT"]
            [lein-marginalia "0.7.0"]]
  :iplant-rpm {:summary "Facepalm"
               :type :command
               :release 1}
  :aot [facepalm.core]
  :main facepalm.core
  :repositories {"iplantCollaborative"
                 "http://projects.iplantcollaborative.org/archiva/repository/internal/"})
