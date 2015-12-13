(ns dfence.jwt
  (:require [cheshire.core :as json]
            [dfence.utils :refer [lower-case-keyword]])
  (:import [com.nimbusds.jose JWSObject]))

(defn parse-token [token]
  (let [jws (JWSObject/parse token)
        payload-str (.toString (.getPayload jws))
        payload (json/parse-string payload-str lower-case-keyword) ]
    payload))