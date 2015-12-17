(ns dfence.rule-test
  (:require [clojure.test :refer :all]
            [dfence.rule :as subject]))

(def rules-csv [["Http Method"  "uri"         "Is-service"  "has-Valid-Token" "ROLE1"     "Role2"     "username"            "staff-flag"]
                ["*"            "/ip"         ""            "Required"        ""          "Required"  ""                    "TRUE"]
                ["POST"         "/post/typea" "X"           ""                "X"         "X"         ""                    ""]
                ["PUT"          "/put/*/data" "X"           ""                "  "        ""          "service1"            ""]
                ["DELETE"       "/delete/a?"  "X"            ""                "Required"  " "         "service1, Service2"  "FALSE"]])

(def parsed-rules [{:method "*"       :uri "/ip"         :dfence-matcher :all :is-service nil  :has-valid-token true       :role1 nil        :role2 true       :username nil                      :staff-flag ["TRUE"]}
                   {:method "POST"    :uri "/post/typea" :dfence-matcher :any :is-service true :has-valid-token nil        :role1 true       :role2 true       :username nil                      :staff-flag nil}
                   {:method "PUT"     :uri "/put/*/data" :dfence-matcher :any :is-service true :has-valid-token nil        :role1 nil        :role2 nil        :username ["service1"]             :staff-flag nil}
                   {:method "DELETE"  :uri "/delete/a?"  :dfence-matcher :all :is-service true :has-valid-token nil        :role1 true       :role2 nil        :username ["service1" "Service2"]  :staff-flag ["FALSE"]}])

(testing "Parsing rules"
  (is (= parsed-rules (#'subject/parse-rule-set rules-csv))))
