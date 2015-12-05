(ns dfence.reverse-proxy
  (:require [org.httpkit.client :as http-client]))

(defn forward-request [url method headers body]
  (let [resp @(http-client/request {:url url
                                    :method method
                                    :headers headers
                                    :body body})]
    (if (contains? resp :error)
      {:status 500
       :headers {}
       :body (str (:error resp))}
      {:status  (:status resp)
       :headers {}
       :body    (:body resp)}
      )
    )
  )