(ns dfence.fact-parser)

(defn extract-incoming-facts [incoming-req]
  (select-keys incoming-req
               [:request-method :uri :scheme]))
