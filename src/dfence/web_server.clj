(ns dfence.web-server
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [dfence.reverse-proxy :as proxy]
            [dfence.evaluate :as evaluate]
            [dfence.fact :as fact]))

; show config and ruleset

(defn app-handler [config rules request]
  (let [facts (fact/parse-facts (dissoc request :body)
                                (get-in config [:dfence-server :token-prefix]))
        outcome (evaluate/evaluate-rules rules facts (:api-server config))]
    (case outcome
      :allow (proxy/forward-request request config)
      :authentication-required {:status 401
                                :header {"Server" "d-fence v0.01"}
                                :body (get-in config [:dfence-server :message-unauthenticated])}
      :access-denied  {:status  403
                       :headers {"Server" "d-fence v0.01"}
                       :body    (get-in config [:dfence-server :message-forbidden])})))

(defonce server (atom nil))

(defn stop-jetty []
  (when @server
    (prn "Stopping dfence server...")
    (.stop @server)
    (reset! server nil)
    (prn "dfence server stopped.")))

(defn start-jetty [config rules]
  (prn "Starting dfence server (which is jetty)...")
  (reset! server
          (run-jetty
            (-> (partial app-handler config rules)
                (wrap-keyword-params)
                (wrap-params))
            { :join? false
              :ssl? (get-in config [:dfence-server :ssl?] false)
              :host (get-in config [:dfence-server :host] "localhost")
              :port (get-in config [:dfence-server :port] 8080)}))
  (prn "dfence server started."))