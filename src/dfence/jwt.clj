(ns dfence.jwt
  (:require [cheshire.core :as json]
            [clojure.string :refer [lower-case]]
            [dfence.utils :refer [lower-case-keyword]])
  (:import [com.nimbusds.jose JWSObject]))

(defn default-transformer [payload]
  {:has-valid-token true
   :roles           (mapv lower-case (:flags payload))
   :payload         payload})


(defn parse-token [token transformer]
  (let [jws (JWSObject/parse token)
        payload-str (.toString (.getPayload jws))
        payload (json/parse-string payload-str lower-case-keyword) ]
    (transformer payload)))