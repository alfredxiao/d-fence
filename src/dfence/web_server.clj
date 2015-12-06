(ns dfence.web-server
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [dfence.fact-parser :as facts]
            [dfence.reverse-proxy :as proxy]))

(defn transform-url [{:keys [scheme host port]} {:keys [uri]}]
  (str scheme "://" host (when (not (= 80 port)) (str ":" port)) uri))

(defn app-handler [target-config incoming-req]
  (let [incoming-facts (facts/extract-incoming-facts incoming-req)
        outgoing-url (transform-url target-config incoming-req)
        outgoing-headers (assoc (:headers incoming-req) "host" (:host target-config))]
    (proxy/forward-request outgoing-url
                           (:request-method incoming-req)
                           outgoing-headers
                           (:body incoming-req))
    ))

(defonce server (atom nil))

(defn stop-jetty []
  (when @server
    (prn "Stopping jetty server...")
    (.stop @server)
    (reset! server nil)
    (prn "Jetty server stopped.")))

(defn start-jetty [{:keys [target-destination http-server]}]
  (prn "Starting jetty server...")
  (reset! server
          (run-jetty
            (partial app-handler target-destination)
            { :join? false
              :ssl? false
              :host "localhost"
              :port (:port http-server)
             }))
  (prn "Jetty server started."))