(ns dfence.main
  (:require [dfence.config :as config]
            [dfence.web-server :as http-server])
  (:gen-class))

(defn -main [& args]
  (let [config (config/get-config!)]
    (http-server/run-server config)))