(ns dfence.fact
  (:require [clj-time.format :as format]
            [clj-time.core :as time]
            [dfence.jwt :as jwt]
            [clojure.string :refer [trim upper-case lower-case]]))

(def date-format      (format/formatter "yyyy-MM-dd" (time/default-time-zone) ))
(def time-format      (format/formatter "HH:mm:ss" (time/default-time-zone)))
(def week-day-format  (format/formatter "E" (time/default-time-zone) ))

(defn- extract-token [request token-prefix]
  (when-let [header-value (get-in request [:headers "authorization"])]
    (when (.startsWith header-value token-prefix)
      (-> header-value
          (subs (.length token-prefix))
          trim))))

(defn- parse-token-facts [token]
  (when token
    (let [payload (jwt/parse-token token)]
      {:has-valid-token true
       :roles           (mapv lower-case (:flags payload))
       :payload         payload})))

(defn parse-facts [request token-prefix]
  (let [now (time/now)]
    (-> request
        (select-keys [:scheme :remote-addr])
        (merge {:request-method (-> request :request-method name upper-case)
                :request-scheme (-> request :scheme name upper-case)
                :request-uri    (:uri request)
                :request-params (:params request)
                :date           (format/unparse date-format now)
                :time           (format/unparse time-format now)
                :week-day       (format/unparse week-day-format now)})
        (merge (parse-token-facts (extract-token request token-prefix))))))
