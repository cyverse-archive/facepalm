
;; IMPORTANT NOTE: Both an RPM and a tarball are generated for this project.
;; Because the release number is not recorded anywhere in the tarball, minor
;; changes need to be recorded in the version number.  Please increment the
;; minor version number rather than the release number for minor changes.

(defproject facepalm "2.0.0-SNAPSHOT"
  :description "Command-line utility for DE database managment."
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/tools.cli "0.3.1"]
                 [org.clojure/tools.logging "0.2.6"]
                 [cheshire "5.3.1"]
                 [com.cemerick/pomegranate "0.2.0"]
                 [fleet "0.10.1"]
                 [korma "0.3.0-RC2"]
                 [org.iplantc/clojure-commons "1.4.7"]
                 [org.iplantc/kameleon "1.8.4-SNAPSHOT"]
                 [postgresql "9.1-901-1.jdbc4"]
                 [slingshot "0.10.3"]
                 [clj-http "0.7.8"]]
  :plugins [[org.iplantc/lein-iplant-cmdtar "0.1.2-SNAPSHOT"]
            [org.iplantc/lein-iplant-rpm "1.4.3-SNAPSHOT"]
            [lein-marginalia "0.7.1"]]
  :iplant-rpm {:summary "Facepalm"
               :type :command}
  :aot [facepalm.core]
  :main facepalm.core
  :repositories {"iplantCollaborative"
                 "http://projects.iplantcollaborative.org/archiva/repository/internal/"})
