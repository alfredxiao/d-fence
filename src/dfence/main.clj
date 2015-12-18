(ns dfence.main
  (:require [dfence.web-server :as web-server]
            [dfence.policy :as policy]
            [environ.core :as environ]
            [cheshire.core :as json]
            [dfence.utils :refer [lower-case-keyword]])
  (:gen-class))

(defn- load-config! [filepath]
  (json/parse-string (slurp filepath) lower-case-keyword))

(defn -main
  "Loads config and rules from specified path in the following order:
  1. environment variables
  2. Java system properties, e.g. -Ddfence-config=/home/myfile.json
  3. defaults to ./conf/dfence-config.json and .conf/dfence-policies.csv"
  [& args]
  (let [config (load-config! (environ/env "dfence-config" "./conf/dfence-config.json"))
        rules (policy/parse-policy-file! (environ/env "dfence-policies" "./conf/dfence-policies.csv")
                                         (-> config :api-server :facts keys))]
    (web-server/start-jetty config rules)))