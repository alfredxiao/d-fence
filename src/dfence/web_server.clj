(ns dfence.web-server
  (:require [org.httpkit.server :as http-server]
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

(defn stop-server []
  (when-not (nil? @server)
    (@server :timeout 100)
    (reset! server nil)))

(defn start-server [config]
  (reset! server (http-server/run-server (partial app-handler (:target-destination config))
                                         (:http-server config))))