(ns unit.dfence.evaluate-test
  (:require [clojure.test :refer :all]
            [clj-http.client :as client]
            [dfence.evaluate :as subject]))

(def any-ip-policy        {:matching-rule :any :method "ANY"     :uri "/ip"                               :has-valid-token true})
(def post-policy          {:matching-rule :any :method "POST"    :uri "/post/typea"     :is-service true})
(def put-policy           {:matching-rule :any :method "PUT"     :uri "/put/*/data"     :is-service true})
(def delete-policy        {:matching-rule :any :method "DELETE"  :uri "/delete/a2"                                              :service-name ["service1" "service2"]})
(def options-policy       {:matching-rule :any :method "OPTIONS" :uri "/delete/:id"     })

(def sample-policies-any-matcher [any-ip-policy post-policy put-policy delete-policy options-policy])

(testing "filtering relevant policies and destructuring parameters"
  (are [matched-rules request-method request-uri] (= matched-rules (#'subject/relevant-policies-and-params sample-policies-any-matcher request-method request-uri))
      [[any-ip-policy {}]]            "GET"        "/ip"
      [[put-policy {}]]               "PUT"        "/put/123/data"
      [[delete-policy {}]]            "DELETE"     "/delete/a2"
      []                              "DELETE"     "/any"
      [[options-policy {:id "123"}]]  "OPTIONS"    "/delete/123"))

(testing "Policy evaluation of :any matcher"
  (are [outcome request-facts] (= outcome (subject/evaluate-policies sample-policies-any-matcher request-facts {}))
       :authentication-required {:request-method "GET"    :request-uri "/ip"            :user-facts {:has-valid-token false :is-service nil}}
       :allow                   {:request-method "GET"    :request-uri "/ip"            :user-facts {:has-valid-token true  :is-service nil}}
       :allow                   {:request-method "POST"   :request-uri "/post/typea"    :user-facts {:has-valid-token true  :is-service true }}
       :access-denied           {:request-method "POST"   :request-uri "/post/typea"    :user-facts {:has-valid-token true  :is-service false }}
       :authentication-required {:request-method "POST"   :request-uri "/post/typea"    :user-facts {:has-valid-token false :is-service true }}
       :access-denied           {:request-method "PUT"    :request-uri "/put/123/data"  :user-facts {:has-valid-token true  :is-service false}}
       :access-denied           {:request-method "DELETE" :request-uri "/delete/a2"     :user-facts {:has-valid-token true  :is-service true :service-name "service3"}}
       :allow                   {:request-method "DELETE" :request-uri "/delete/a2"     :user-facts {:has-valid-token true  :is-service true :service-name "service1"}}
       :allow                   {:request-method "DELETE" :request-uri "/delete/a2"     :user-facts {:has-valid-token true  :is-service true :service-name "service2"}}))


(def matching-all-policy  {:matching-rule :all :method "GET"     :uri "/sensitive"      :is-service true  :has-valid-token true :service-name ["service1" "service2"]})
(def sample-policies-all-matcher [matching-all-policy])

(testing "policy evaluation of :all matcher"
  (are [outcome facts] (= outcome (subject/evaluate-policies sample-policies-all-matcher facts {}))
       :allow           {:request-method "GET" :request-uri "/sensitive"   :user-facts {:has-valid-token true  :is-service true :service-name "service1"}}
       :allow           {:request-method "GET" :request-uri "/sensitive"   :user-facts {:has-valid-token true  :is-service true :service-name "service2"}}
       :access-denied   {:request-method "GET" :request-uri "/sensitive"   :user-facts {:has-valid-token true  :is-service true :service-name "service3"}}
       :access-denied   {:request-method "GET" :request-uri "/sensitive"   :user-facts {:has-valid-token true  :is-service nil  :service-name "service1"}}))


(def api-server-config {:facts  {:staff-flag "/facts/staff-flag/:id"}})
(def data-fact-policy  {:matching-rule :all :method "GET" :uri "/sensitive/:id" :is-service true :has-valid-token true :staff-flag ["TRUE"]})

(testing "Rule evaluation of data fact matching"
  (with-redefs [client/get (fn [url] {:body (cond
                                              (.endsWith url "/staff-id") "TRUE"
                                              (.endsWith url "/non-staff-id") "FALSE")})]
    (are [outcome facts] (= outcome (subject/evaluate-policies [data-fact-policy] facts api-server-config))
         :allow            {:request-method "GET" :request-uri "/sensitive/staff-id"   :user-facts {:has-valid-token true  :is-service true}}
         :access-denied    {:request-method "GET" :request-uri "/sensitive/staff-id"   :user-facts {:has-valid-token true  :is-service nil}}
         :access-denied    {:request-method "GET" :request-uri "/sensitive/non-staff-id"  :user-facts {:has-valid-token true  :is-service true}})))


(def request-facts  {:request-method "GET" :request-uri "/sensitive/123" :user-facts {:has-valid-token true :is-service true :service-name "service2"}})
(def failing-policy {:matching-rule :all :method "GET" :uri "/sensitive/*" :is-service true :service-name ["service1"]})
(def passing-policy {:matching-rule :all :method "GET" :uri "/sensitive/*" :is-service true :service-name ["service2"]})

(testing "When failing one policy does NOT stop one from passing another policy"
  (is (= :access-denied (subject/evaluate-policies [failing-policy] request-facts {})))
  (is (= :allow (subject/evaluate-policies [passing-policy] request-facts {})))
  (is (= :allow (subject/evaluate-policies [failing-policy passing-policy] request-facts {}))))
; test where a policy has negative requirement e.g. Must not possess role1: "-"