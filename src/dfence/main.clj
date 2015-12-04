(ns dfence.main
  (:require [dfence.config :as config]
            [dfence.web-server :as web-server])
  (:gen-class))

(defn -main [& args]
  (let [config (config/get-config!)]
    (web-server/start-server config)))