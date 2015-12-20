(ns unit.dfence.utils.url-utils-test
  (:require [clojure.test :refer :all]
            [dfence.utils.url-utils :as subject]))

(testing "URI parsing"
  (are [uri           parsed-uri] (= parsed-uri (subject/parse-uri uri))
       "/update"      ["/" "update"]
       "/update/"     ["/" "update" "/"]
       "/"            ["/"]
       ""             []
       "/update/id"   ["/" "update" "/" "id"]
       "/update/:id"  ["/" "update" "/" ":id"]))

(testing "URI Matching"
  (are [uri-pattern         actual-uri        outcome] (= outcome (subject/match-uri uri-pattern actual-uri))
       "/update/:id"        "/update/123"     [true {:id "123"}]
       "/update/:type/:id"  "/update/abc/123" [true {:id "123" :type "abc"}]
       "/update/123"        "/update/123"     [true {}]
       "/update/:type/:id"  "/update/abc"     nil
       "/update/abc"        "/update/123"     [false {}]
       "/update/123/ab"     "/update/123"     nil
       "/update/:type/:id"  "/update/abc/"    [true {:id "" :type "abc"}]
                                               ))
