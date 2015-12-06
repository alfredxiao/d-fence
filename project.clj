(defproject dfence "0.1.0-SNAPSHOT"
  :description "aimed to be a defence protecting your application's API"
  :url "http://alfredxi.ao/dfence"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [environ "1.0.1"]
                 [clj-http "2.0.0"]
                 [ring/ring-jetty-adapter "1.4.0"]
                 [com.cemerick/url "0.1.1"]
                 [clj-time "0.11.0"]]
  :main ^:skip-aot dfence.main
)
