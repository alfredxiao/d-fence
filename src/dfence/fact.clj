(ns dfence.fact
  (:require [clj-time.format :as format]
            [clj-time.core :as time]
            [dfence.jwt :as jwt]
            [dfence.utils :refer [lower-case-keyword]]
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

(defn- parse-token-asserts [token]
  (when token
    (let [payload (jwt/parse-token token)
          roles (mapv keyword (:flags payload))]
      (merge {:has-valid-token true
              :is-service (or (:service payload) (-> payload :tokeninformation :service))
              :is-user (.equalsIgnoreCase "USER" (:role payload))
              :account-name (-> payload :sub)}
             (zipmap roles
                     (repeat true))))))

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
        (assoc :asserts (parse-token-asserts (extract-token request token-prefix))))))
