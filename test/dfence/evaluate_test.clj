(ns dfence.evaluate-test
  (:require [clojure.test :refer :all]
            [dfence.evaluate :as subject]))

(def any-ip-rule    {:dfence-matcher :any :method "*"       :uri "/ip"           :has-valid-token true})
(def post-rule      {:dfence-matcher :any :method "POST"    :uri "/post/typea"   :is-service true})
(def put-rule       {:dfence-matcher :any :method "PUT"     :uri "/put/*/data"   :is-service true})
(def delete-rule    {:dfence-matcher :any :method "DELETE"  :uri "/delete/a2"    :service-name ["service1" "service2"]})
(def all-rule       {:dfence-matcher :all :method "GET"     :uri "/sensitive"    :is-service true  :has-valid-token true :service-name ["service1" "service2"]})

(def sample-rules-any-matcher [any-ip-rule post-rule put-rule delete-rule])
(def sample-rules-all-matcher [all-rule])

(testing "Relevant rules"
  (are [matched-rules request-method request-uri] (= matched-rules (#'subject/applicable-rules sample-rules-any-matcher request-method request-uri))
       [any-ip-rule]  "GET"          "/ip"
       [put-rule]     "PUT"          "/put/123/data"
       [delete-rule]  "DELETE"       "/delete/a2"
       []             "DELETE"       "/any"
                                                 ))
(testing "Rule evaluation of any matcher"
  (are [outcome facts] (= outcome (subject/evaluate-rules sample-rules-any-matcher facts))
       :authentication-required {:request-method "GET"   :request-uri "/ip"           :asserts {:has-valid-token false :is-service nil}}
       :allow                   {:request-method "GET"   :request-uri "/ip"           :asserts {:has-valid-token true  :is-service nil}}
       :allow                   {:request-method "POST"  :request-uri "/post/typea"   :asserts {:has-valid-token true  :is-service true }}
       :access-denied           {:request-method "POST"  :request-uri "/post/typea"   :asserts {:has-valid-token true  :is-service false }}
       :authentication-required {:request-method "POST"  :request-uri "/post/typea"   :asserts {:has-valid-token false :is-service true }}
       :access-denied           {:request-method "PUT"   :request-uri "/put/123/data" :asserts {:has-valid-token true  :is-service false}}
       :access-denied           {:request-method "DELETE" :request-uri "/delete/a2"   :asserts {:has-valid-token true  :is-service true :service-name "service3"}}
       :allow                   {:request-method "DELETE" :request-uri "/delete/a2"   :asserts {:has-valid-token true  :is-service true :service-name "service1"}}
       :allow                   {:request-method "DELETE" :request-uri "/delete/a2"   :asserts {:has-valid-token true  :is-service true :service-name "service2"}}
                       )

  )


(testing "Rule evaluation of ALL matcher"
  (are [outcome facts] (= outcome (subject/evaluate-rules sample-rules-all-matcher facts))
       :allow                   {:request-method "GET" :request-uri "/sensitive"   :asserts {:has-valid-token true  :is-service true :service-name "service1"}}
       :allow                   {:request-method "GET" :request-uri "/sensitive"   :asserts {:has-valid-token true  :is-service true :service-name "service2"}}
       :access-denied           {:request-method "GET" :request-uri "/sensitive"   :asserts {:has-valid-token true  :is-service true :service-name "service3"}}
       :access-denied           {:request-method "GET" :request-uri "/sensitive"   :asserts {:has-valid-token true  :is-service nil  :service-name "service1"}}

                       )
  )