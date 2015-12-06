(ns dfence.reverse-proxy
  (:require [clj-http.client :as client]
            [dfence.utils :refer :all]
            [clojure.string :refer [lower-case]]))

(defn- convert-header-name [keywordized-name]
  (-> keywordized-name
      clojure.core/name
      capitalise-all-words))

(defn- convert-headers [headers]
  (into {} (for [[k v] headers]
             [(convert-header-name k) v])))

(defn- filter-headers [headers]
  (-> headers
      (dissoc "Transfer-Encoding" "Content-Length" "Content-Encoding")))

(defn forward-request [url method outgoing-headers outgoing-body]
  (let [{:keys [error status headers body]} (client/request {:url url
                                                             :method method
                                                             :headers outgoing-headers
                                                             :body outgoing-body
                                                             :throw-exceptions false
                                                             :as :stream})]
    (if error
      {:status 500
       :headers {}
       :body (str "dfence failed to forward request to target destination, and error message is: " error)}
      {:status status
       :headers (-> headers
                    convert-headers
                    filter-headers)
       :body body})))