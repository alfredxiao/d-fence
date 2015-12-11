(ns dfence.evaluate-test
  (:require [clojure.test :refer :all]
            [dfence.evaluate :as subject]))

(def any-ip-rule    {:method "*"       :uri "/ip"           :has-valid-token true})
(def post-rule-a    {:method "POST"    :uri "/post/typea"   :is-service true})
(def put-rule       {:method "PUT"     :uri "/put/*/data"   :is-service true})
(def delete-rule    {:method "DELETE"  :uri "/delete/a?"    :is-service true})

(def sample-rules [any-ip-rule post-rule-a put-rule delete-rule])

(testing "Relevant rules"
  (are [matched-rules request-method request-uri] (= matched-rules (#'subject/relevant-rules sample-rules request-method request-uri))
       [any-ip-rule]  "GET"          "/ip"
       [put-rule]     "PUT"          "/put/123/data"
       [delete-rule]  "DELETE"       "/delete/a2"
       []             "DELETE"       "/any"
                                                 ))
(testing "Rule evaluation"
  (are [outcome facts] (= outcome (subject/evaluate-rules sample-rules facts))
       :deny  {:request-method "GET"   :request-uri "/ip"           :primitives {:has-valid-token false}}
       :allow {:request-method "GET"   :request-uri "/ip"           :primitives {:has-valid-token true}}
       :allow {:request-method "POST"  :request-uri "/post/typea"   :primitives {:has-valid-token true  :is-service true }}
       :deny  {:request-method "POST"  :request-uri "/post/typea"   :primitives {:has-valid-token true  :is-service false }}
       :deny  {:request-method "POST"  :request-uri "/post/typea"   :primitives {:has-valid-token false :is-service true }}
       :deny  {:request-method "PUT"   :request-uri "/put/123/data" :primitives {:has-valid-token true  :is-service false}}))

