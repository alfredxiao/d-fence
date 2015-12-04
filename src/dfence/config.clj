(ns dfence.config
  (:require [environ.core :as environ]))

(defn read-edn [fname]
  (clojure.edn/read-string (slurp fname)))

(defn get-config! []
  (read-edn (environ/env "config-file" "./conf/config.edn")))
