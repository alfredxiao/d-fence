(ns dfence.main
  (:require [dfence.web-server :as web-server]
            [dfence.rule :as rule-parser]
            [environ.core :as environ])
  (:gen-class))

(defn- load-config! [filepath]
  (clojure.edn/read-string (slurp filepath)))

(defn -main [& args]
  (let [config (load-config! (environ/env "config-file" "./conf/config.edn"))
        rules (rule-parser/parse! (environ/env "rule-file" "./conf/dfence-rules.csv"))]
    (web-server/start-jetty config rules)))