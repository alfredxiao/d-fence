(ns dfence.web-server
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [dfence.fact-parser :as facts]
            [dfence.reverse-proxy :as proxy]))

(defn app-handler [config incoming-req]
  (proxy/forward-request incoming-req
                         config))

(defonce server (atom nil))

(defn stop-jetty []
  (when @server
    (prn "Stopping dfence server...")
    (.stop @server)
    (reset! server nil)
    (prn "dfence server stopped.")))

(defn start-jetty [{:keys [dfence-server] :as config}]
  (prn "Starting dfence server (which is jetty)...")
  (reset! server
          (run-jetty
            (partial app-handler config)
            { :join? false
              :ssl? false
              :host "localhost"
              :port (:port dfence-server)
             }))
  (prn "dfence server started."))