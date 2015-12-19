(ns dfence.reverse-proxy
  (:require [clj-http.client :as client]
            [dfence.utils.url-utils :refer [generate-new-location location-matches?]]
            [dfence.utils.common-utils :refer [capitalise-all-words update-when]]))

(defn transform-url [{:keys [scheme host port]} {:keys [uri query-string]}]
  (cond-> (str scheme "://" host (when (not (= 80 port)) (str ":" port)) uri)
          (not (empty? query-string)) (str "?" query-string)))

(defn- prepare-outgoing-headers [incoming-headers target-config]
  (cond-> incoming-headers
          true (assoc "host" (:host target-config))
          true (dissoc "content-length")))

(defn- capitalise [keywordized-name]
  (-> keywordized-name
      name
      capitalise-all-words))

(defn- capitalise-header-names [headers]
  (into {} (for [[k v] headers]
             [(capitalise k) v])))

(defn- replace-with-dfence-server-location [{:keys [scheme host port]} old-location]
  (generate-new-location old-location scheme host port))

(defn- convert-location-if-moved-permanently [headers {dfence-server :dfence-server {:keys [scheme host port]} :api-server}]
  (update-when headers
               "Location"
               (partial location-matches? scheme host port)
               (partial replace-with-dfence-server-location dfence-server)))

(defn- process-response-headers [headers config]
  (-> headers
      capitalise-header-names
      (convert-location-if-moved-permanently config)))

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
       :body (str "dfence failed to forward request to api server at " (:host api-server) ":" (:port api-server) ", and error message is: " error)}
      {:status status
       :headers (process-response-headers headers config)
       :body body})))