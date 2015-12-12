(ns dfence.rule-test
  (:require [clojure.test :refer :all]
            [dfence.rule :as subject]))

(def rules-csv [["Http Method"  "uri"         "Is-service"  "has-Valid-Token" "ROLE1" "Role2"]
                ["*"            "/ip"         ""            "Y"               ""      "Y"]
                ["POST"         "/post/typea" "X"           ""                "X"     "Y"]
                ["PUT"          "/put/*/data" "x"           ""                ""      ""]
                ["DELETE"       "/delete/a?"  "true"        ""                "Yes"   ""]])

(def parsed-rules [{:method "*"       :uri "/ip"           :is-service nil  :has-valid-token true :role1 nil  :role2 true}
                   {:method "POST"    :uri "/post/typea"   :is-service true :has-valid-token nil  :role1 true :role2 true}
                   {:method "PUT"     :uri "/put/*/data"   :is-service true :has-valid-token nil  :role1 nil  :role2 nil}
                   {:method "DELETE"  :uri "/delete/a?"    :is-service true :has-valid-token nil  :role1 true :role2 nil}])

(testing "Parsing rules"
  (is (= parsed-rules (#'subject/parse-rule-set rules-csv))))
