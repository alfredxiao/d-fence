(ns unit.dfence.evaluate-test
  (:require [clojure.test :refer :all]
            [clj-http.client :as client]
            [dfence.evaluate :as subject]))

(def any-ip-policy        {:matching-rule :any :method "ANY"     :uri "/ip"           :has-valid-token true})
(def post-policy          {:matching-rule :any :method "POST"    :uri "/post/typea"   :is-service true})
(def put-policy           {:matching-rule :any :method "PUT"     :uri "/put/*/data"   :is-service true})
(def delete-policy        {:matching-rule :any :method "DELETE"  :uri "/delete/a2"    :service-name #{"service1" "service2"}})
(def options-policy       {:matching-rule :any :method "OPTIONS" :uri "/delete/:id"})

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


(testing "policy evaluation of :all matcher"
  (let [matching-all-policy  {:matching-rule :all :method "GET" :uri "/sensitive" :is-service true :has-valid-token true :service-name #{"service1" "service2"}}
        sample-policies-all-matcher [matching-all-policy]]
    (are [outcome facts] (= outcome (subject/evaluate-policies sample-policies-all-matcher facts {}))
         :allow           {:request-method "GET" :request-uri "/sensitive"   :user-facts {:has-valid-token true  :is-service true :service-name "service1"}}
         :allow           {:request-method "GET" :request-uri "/sensitive"   :user-facts {:has-valid-token true  :is-service true :service-name "service2"}}
         :access-denied   {:request-method "GET" :request-uri "/sensitive"   :user-facts {:has-valid-token true  :is-service true :service-name "service3"}}
         :access-denied   {:request-method "GET" :request-uri "/sensitive"   :user-facts {:has-valid-token true  :is-service nil  :service-name "service1"}})))


(testing "Rule evaluation of data fact matching"
  (let [api-server-config {:facts  {:staff-flag "/facts/staff-flag/:id"}}
        data-fact-policy  {:matching-rule :all :method "GET" :uri "/sensitive/:id" :is-service true :has-valid-token true :staff-flag #{"TRUE"}}]
    (with-redefs [client/get (fn [url] {:body (cond
                                                (.endsWith url "/staff-id") "TRUE"
                                                (.endsWith url "/non-staff-id") "FALSE")})]
      (are [outcome facts] (= outcome (subject/evaluate-policies [data-fact-policy] facts api-server-config))
           :allow            {:request-method "GET" :request-uri "/sensitive/staff-id"   :user-facts {:has-valid-token true  :is-service true}}
           :access-denied    {:request-method "GET" :request-uri "/sensitive/staff-id"   :user-facts {:has-valid-token true  :is-service nil}}
           :access-denied    {:request-method "GET" :request-uri "/sensitive/non-staff-id"  :user-facts {:has-valid-token true  :is-service true}}))))

(testing "When failing one policy does NOT stop one from passing another policy"
  (let [request-facts  {:request-method "GET" :request-uri "/sensitive/123" :user-facts {:has-valid-token true :is-service true :service-name "service2"}}
        failing-policy {:matching-rule :all :method "GET" :uri "/sensitive/*" :is-service true :service-name #{"service1"}}
        passing-policy {:matching-rule :all :method "GET" :uri "/sensitive/*" :is-service true :service-name #{"service2"}}]
    (is (= :access-denied (subject/evaluate-policies [failing-policy] request-facts {})))
    (is (= :allow (subject/evaluate-policies [passing-policy] request-facts {})))
    (is (= :allow (subject/evaluate-policies [failing-policy passing-policy] request-facts {})))))


(testing "test where a policy has negative requirement e.g. Must not possess role1: -"
  (let [postive-facts   {:request-method "GET" :request-uri "/sensitive/123" :user-facts {:has-valid-token true :is-service true :role1 true}}
        negative-facts  {:request-method "GET" :request-uri "/sensitive/123" :user-facts {:has-valid-token true :is-service true :role1 false}}
        null-facts      {:request-method "GET" :request-uri "/sensitive/123" :user-facts {:has-valid-token true :is-service true :role1 nil}}
        postive-policy  {:matching-rule :all :method "GET" :uri "/sensitive/*" :is-service true :role1 true}
        negative-policy {:matching-rule :all :method "GET" :uri "/sensitive/*" :is-service true :role1 false}]
    (are [outcome       policy          facts] (= outcome (subject/evaluate-policies [policy] facts {}))
         :allow         postive-policy  postive-facts
         :access-denied postive-policy  negative-facts
         :access-denied postive-policy  null-facts
         :access-denied negative-policy postive-facts
         :allow         negative-policy negative-facts
         :allow         negative-policy null-facts)))