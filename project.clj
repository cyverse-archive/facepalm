
;; IMPORTANT NOTE: Both an RPM and a tarball are generated for this project.
;; Because the release number is not recorded anywhere in the tarball, minor
;; changes need to be recorded in the version number.  Please increment the
;; minor version number rather than the release number for minor changes.

(defproject facepalm "2.0.0-SNAPSHOT"
  :description "Command-line utility for DE database managment."
  :url "http://www.iplantcollaborative.org"
  :license {:name "BSD"
            :url "http://iplantcollaborative.org/sites/default/files/iPLANT-LICENSE.txt"}
  :scm {:connection "scm:git:git@github.com:iPlantCollaborativeOpenSource/facepalm.git"
        :developerConnection "scm:git:git@github.com:iPlantCollaborativeOpenSource/facepalm.git"
        :url "git@github.com:iPlantCollaborativeOpenSource/facepalm.git"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/tools.cli "0.2.1"]
                 [org.clojure/tools.logging "0.2.3"]
                 [cheshire "5.2.0"]
                 [com.cemerick/pomegranate "0.0.13"]
                 [fleet "0.9.5"]
                 [korma "0.3.0-RC2"]
                 [org.iplantc/clojure-commons "1.4.8-SNAPSHOT"]
                 [org.iplantc/kameleon "1.8.4-SNAPSHOT"]
                 [postgresql "9.0-801.jdbc4"]
                 [slingshot "0.10.3"]
                 [clj-http "0.6.3"]]
  :plugins [[org.iplantc/lein-iplant-cmdtar "0.1.2-SNAPSHOT"]
            [org.iplantc/lein-iplant-rpm "1.4.3-SNAPSHOT"]
            [lein-marginalia "0.7.1"]]
  :iplant-rpm {:summary "Facepalm"
               :type :command}
  :aot [facepalm.core]
  :main facepalm.core
  :repositories [["sonatype-nexus-snapshots"
                  {:url "https://oss.sonatype.org/content/repositories/snapshots"}]
                 ["sonatype-nexus-staging"
                  {:url "https://oss.sonatype.org/service/local/staging/deploy/maven2/"}]])
