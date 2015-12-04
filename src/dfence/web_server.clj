(ns dfence.web-server
  (:require [org.httpkit.server :as http-server]
            [org.httpkit.client :as http-client]))

(defn extract-incoming-facts [incoming-req]
  {})

(defn transform-url [{:keys [scheme host port]} {:keys [uri]}]
  (str scheme "://" host ":" port uri))

(defn app [target-config incoming-req]
  (let [incoming-facts (extract-incoming-facts incoming-req)
        resp @(http-client/request {:url (transform-url target-config incoming-req)
                                    :method (:request-method incoming-req)
                                    :headers (:headers incoming-req)
                                    :body (:body incoming-req)})]
    (if (contains? resp :error)
      {:status 500
       :headers {}
       :body (str (:error resp))}
      {:status  (:status resp)
       :headers {}
       :body    (:body resp)}
  )))

(defonce server (atom nil))

(defn stop-server []
  (when-not (nil? @server)
    (@server :timeout 100)
    (reset! server nil)))

(defn start-server [config]
  (reset! server (http-server/run-server (partial app (:target-destination config))
                                         (:http-server config))))