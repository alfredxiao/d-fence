(ns dfence.reverse-proxy
  (:require [clj-http.client :as client]
            [dfence.utils :refer :all]
            [clojure.string :refer [lower-case]]))

(defn transform-url [{:keys [scheme host port]} {:keys [uri query-string]}]
  (cond-> (str scheme "://" host (when (not (= 80 port)) (str ":" port)) uri)
          (not (empty? query-string)) (str "?" query-string)))

(defn- prepare-outgoing-headers [incoming-headers target-config]
  (cond-> incoming-headers
          true (assoc "host" (:host target-config))
          true (dissoc "content-length")))

(defn- convert-response-header-name [keywordized-name]
  (-> keywordized-name
      clojure.core/name
      capitalise-all-words))

(defn- convert-response-headers [headers]
  (into {} (for [[k v] headers]
             [(convert-response-header-name k) v])))

(defn- new-location [{:keys [scheme host port]} old-location]
  (new-url old-location scheme host port))

(defn- convert-location-if-moved-permanently [config headers]
  (update-when headers
               "Location"
               (partial new-location (:dfence-server config))))

(defn- prepare-response-headers [headers config]
  (convert-location-if-moved-permanently config
                                         (convert-response-headers headers)))

(defn forward-request [request {:keys [api-server] :as config}]
  (let [{:keys [error status headers body]} (client/request {:url              (transform-url api-server request)
                                                             :method           (:request-method request)
                                                             :headers          (prepare-outgoing-headers (:headers request) api-server)
                                                             :body             (:body request)
                                                             :throw-exceptions false
                                                             :follow-redirects false
                                                             :decompress-body  false
                                                             :as               :stream})]
    (if error
      {:status 500
       :headers {}
       :body (str "dfence failed to forward request to target destination, and error message is: " error)}
      {:status status
       :headers (prepare-response-headers headers config)
       :body body})))