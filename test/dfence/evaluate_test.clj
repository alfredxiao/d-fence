(ns dfence.evaluate-test
  (:require [clojure.test :refer :all]
            [dfence.evaluate :as subject]))

(def any-ip-rule    {:method "*"       :uri "/ip"           :is-service nil  :has-valid-token true})
(def post-rule      {:method "POST"    :uri "/post/typea"   :is-service true :has-valid-token nil})
(def put-rule       {:method "PUT"     :uri "/put/*/data"   :is-service true :has-valid-token nil})
(def delete-rule    {:method "DELETE"  :uri "/delete/a?"    :is-service true :has-valid-token nil})

(def sample-rules [any-ip-rule post-rule put-rule delete-rule])

(testing "Relevant rules"
  (are [matched-rules request-method request-uri] (= matched-rules (#'subject/applicable-rules sample-rules request-method request-uri))
       [any-ip-rule]  "GET"          "/ip"
       [put-rule]     "PUT"          "/put/123/data"
       [delete-rule]  "DELETE"       "/delete/a2"
       []             "DELETE"       "/any"
                                                 ))
(testing "Rule evaluation"
  (are [outcome facts] (= outcome (subject/evaluate-rules sample-rules facts))
       :authentication-required {:request-method "GET"   :request-uri "/ip"           :asserts {:has-valid-token false :is-service nil}}
       :allow                   {:request-method "GET"   :request-uri "/ip"           :asserts {:has-valid-token true  :is-service nil}}
       :allow                   {:request-method "POST"  :request-uri "/post/typea"   :asserts {:has-valid-token true  :is-service true }}
       :access-denied           {:request-method "POST"  :request-uri "/post/typea"   :asserts {:has-valid-token true  :is-service false }}
       :authentication-required {:request-method "POST"  :request-uri "/post/typea"   :asserts {:has-valid-token false :is-service true }}
       :access-denied           {:request-method "PUT"   :request-uri "/put/123/data" :asserts {:has-valid-token true  :is-service false}}))

