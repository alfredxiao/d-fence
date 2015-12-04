(ns dfence.web-server
  (:require [org.httpkit.server :as http-server]
            [org.httpkit.client :as http-client]))



(defn app [target-config incoming-req]
  (let [resp @(http-client/get "http://www.simpleweb.org")]
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