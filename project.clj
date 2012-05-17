(defproject facepalm "0.0.1-SNAPSHOT"
  :description "Command-line utility for DE database managment."
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [org.clojure/tools.cli "0.2.1"]
                 [org.clojure/tools.logging "0.2.3"]
                 [korma "0.3.0-beta7"]
                 [org.iplantc/kameleon "0.0.1-SNAPSHOT"]
                 [postgresql "9.0-801.jdbc4"]
                 [slingshot "0.10.2"]]
  :plugins [[lein-marginalia "0.7.0"]]
  :aot [facepalm.core]
  :main facepalm.core)
