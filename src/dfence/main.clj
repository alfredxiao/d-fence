(ns dfence.main
  (:require [dfence.web-server :as web-server]
            [dfence.rule :as rule-parser]
            [environ.core :as environ]
            [cheshire.core :as json]
            [dfence.utils :refer [keywordise-in-lower-case]])
  (:gen-class))

(defn- load-config! [filepath]
  (json/parse-string (slurp filepath) keywordise-in-lower-case))

(defn -main
  "Loads config and rules from specified path in the following order:
  1. environment variables
  2. Java system properties, e.g. -Ddfence-config=/home/myfile.json
  3. defaults to ./conf/dfence-config.json and .conf/dfence-rules.csv"
  [& args]
  (let [config (load-config! (environ/env "dfence-config" "./conf/dfence-config.json"))
        rules (rule-parser/parse-rule-set! (environ/env "dfence-rules" "./conf/dfence-rules.csv")
                                           (-> config :api-server :facts keys))]
    (web-server/start-jetty config rules)))