(ns dfence.fact
  (:require [clj-time.format :as format]
            [clj-time.core :as time]
            [clojure.string :refer [upper-case]]))

(def date-format      (format/formatter "yyyy-MM-dd" (time/default-time-zone) ))
(def time-format      (format/formatter "HH:mm:ss" (time/default-time-zone)))
(def week-day-format  (format/formatter "E" (time/default-time-zone) ))

(defn parse-facts [request]
  (let [now (time/now)]
    (-> request
        (select-keys [:scheme :remote-addr])
        (merge {:request-method (-> request :request-method name upper-case)
                :scheme         (-> request :scheme name upper-case)
                :request-uri    (:uri request)
                :date           (format/unparse date-format now)
                :time           (format/unparse time-format now)
                :week-day       (format/unparse week-day-format now)}))))
