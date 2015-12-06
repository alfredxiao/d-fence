(ns dfence.fact-parser
  (:require [clj-time.format :as format]
            [clj-time.core :as time]
            [clojure.string :refer [upper-case]]))

(def date-format      (format/formatter "yyyy-MM-dd" (time/default-time-zone) ))
(def time-format      (format/formatter "HH:mm:ss" (time/default-time-zone)))
(def week-day-format  (format/formatter "E" (time/default-time-zone) ))

(defn extract-incoming-facts [incoming-req]
  (let [now (time/now)]
    (-> incoming-req
        (select-keys [:uri :scheme :remote-addr])
        (merge {:request-method (-> incoming-req :request-method name upper-case)
                :scheme         (-> incoming-req :scheme name upper-case)
                :date           (format/unparse date-format now)
                :time           (format/unparse time-format now)
                :week-day       (format/unparse week-day-format now)}))))
