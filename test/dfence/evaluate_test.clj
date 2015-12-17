(ns dfence.evaluate-test
  (:require [clojure.test :refer :all]
            [dfence.evaluate :as subject]))

(def any-ip-rule    {:matching-rule :any :method "*"       :uri "/ip"           :has-valid-token true})
(def post-rule      {:matching-rule :any :method "POST"    :uri "/post/typea"   :is-service true})
(def put-rule       {:matching-rule :any :method "PUT"     :uri "/put/*/data"   :is-service true})
(def delete-rule    {:matching-rule :any :method "DELETE"  :uri "/delete/a2"    :service-name ["service1" "service2"]})
(def options-rule   {:matching-rule :any :method "OPTIONS" :uri "/delete/:id"   })
(def all-rule       {:matching-rule :all :method "GET"     :uri "/sensitive"    :is-service true  :has-valid-token true :service-name ["service1" "service2"]})
(def data-fact-rule {:matching-rule :all :method "GET"     :uri "/sensitive/:id"    :is-service true  :has-valid-token true :data:staff-flag ["TRUE"]})

(def sample-rules-any-matcher [any-ip-rule post-rule put-rule delete-rule options-rule])
(def sample-rules-all-matcher [all-rule])

(testing "Relevant rules"
  (are [matched-rules request-method request-uri] (= matched-rules (#'subject/applicable-rule-and-params sample-rules-any-matcher request-method request-uri))
       [{:rule any-ip-rule
         :params {}}] "GET"          "/ip"
       [{:rule put-rule
         :params {}}]   "PUT"          "/put/123/data"
       [{:rule delete-rule
         :params {}}] "DELETE"       "/delete/a2"
       []             "DELETE"       "/any"
       [{:rule options-rule
         :params {:id "123"}}] "OPTIONS"      "/delete/123"
                                                 ))
(testing "Rule evaluation of any matcher"
  (are [outcome facts] (= outcome (subject/evaluate-rules sample-rules-any-matcher facts {}))
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
  (are [outcome facts] (= outcome (subject/evaluate-rules sample-rules-all-matcher facts {}))
       :allow                   {:request-method "GET" :request-uri "/sensitive"   :asserts {:has-valid-token true  :is-service true :service-name "service1"}}
       :allow                   {:request-method "GET" :request-uri "/sensitive"   :asserts {:has-valid-token true  :is-service true :service-name "service2"}}
       :access-denied           {:request-method "GET" :request-uri "/sensitive"   :asserts {:has-valid-token true  :is-service true :service-name "service3"}}
       :access-denied           {:request-method "GET" :request-uri "/sensitive"   :asserts {:has-valid-token true  :is-service nil  :service-name "service1"}}

                       )
  )

(def api-server-config {:scheme "http"
                        :host   "localhost"
                        :port   9090
                        :facts  {
                                 :staff-flag "/facts/member-attributes/:id/staff-flag"
                                 }})

(testing "Rule evaluation of data fact matching"
  (are [outcome facts] (= outcome (subject/evaluate-rules [data-fact-rule] facts api-server-config))
                       :allow                   {:request-method "GET" :request-uri "/sensitive/99"   :asserts {:has-valid-token true  :is-service true}}
                       :access-denied           {:request-method "GET" :request-uri "/sensitive/99"   :asserts {:has-valid-token true  :is-service nil}}
                       :access-denied           {:request-method "GET" :request-uri "/sensitive/123"  :asserts {:has-valid-token true  :is-service true}}
                       )
  )