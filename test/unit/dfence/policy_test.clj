(ns unit.dfence.policy-test
  (:require [clojure.test :refer :all]
            [dfence.policy :as subject]))

(def rules-csv [["Http Method"  "uri"         "Matching-Rule" "Is-service"  "has-Valid-Token" "ROLE1"     "Role2"     "username"            "staff-flag"]
                ["*"            "/ip"         "ALL"           ""            "x"               ""          "x"         ""                    "TRUE"]
                ["POST"         "/post/typea" "ANY"           "X"           ""                "X"         "X"         ""                    ""]
                ["PUT"          "/put/*/data" "ANY"           "X"           ""                "  "        ""          "service1"            ""]
                ["DELETE"       "/delete/a"   "ALL"           "X"           ""                "X"         " "         "service1, Service2"  "FALSE"]])

(def parsed-rules [{:method "*"       :uri "/ip"         :matching-rule :all                     :has-valid-token true                      :role2 true                                          :staff-flag ["TRUE"]}
                   {:method "POST"    :uri "/post/typea" :matching-rule :any :is-service true                             :role1 true       :role2 true                                                                }
                   {:method "PUT"     :uri "/put/*/data" :matching-rule :any :is-service true                                                                 :username ["service1"]                                   }
                   {:method "DELETE"  :uri "/delete/a"   :matching-rule :all :is-service true                             :role1 true                         :username ["service1" "Service2"]  :staff-flag ["FALSE"]}])

(testing "Parsing rules"
  (is (= parsed-rules (#'subject/parse-policy-csv rules-csv))))
