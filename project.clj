(defproject dfence "0.1.0-SNAPSHOT"
  :description "aimed to be a defence protecting your application's API"
  :url "http://alfredxi.ao/dfence"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [environ "1.0.1"]
                 [http-kit "2.1.18"]]
  :main ^:skip-aot dfence.main
)
