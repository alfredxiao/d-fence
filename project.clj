(defproject dfence "0.1.0-SNAPSHOT"
  :description "aimed to be a defence protecting your application's API"
  :url "http://alfredxi.ao/dfence"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [environ "1.0.1"]
                 [clj-http "2.0.0"]
                 [ring/ring-jetty-adapter "1.4.0"]
                 [clj-time "0.11.0"]
                 [org.clojure/data.csv "0.1.3"]
                 [cheshire "5.5.0"]
                 [com.nimbusds/nimbus-jose-jwt "4.10"]]
  :main ^:skip-aot dfence.main
  :repositories ^:replace [["releases" {:url "http://spvx10419.hq.local:8081/nexus/content/repositories/releases/" :snapshots false}]
                           ["snapshots" {:url       "http://spvx10419.hq.local:8081/nexus/content/repositories/snapshots/"
                                         :snapshots true
                                         :update :always}]
                           ["clojars" {:url       "http://spvx10419.hq.local:8081/nexus/content/repositories/clojars.org/"
                                       :name "clojars"}]
                           ["maven" {:url       "http://spvx10419.hq.local:8081/nexus/content/repositories/central/"
                                     :name "maven"}]]

  )
