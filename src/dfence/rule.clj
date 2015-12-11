(ns dfence.rule
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]))

; logically, a rule set consists of
; an ordered list of rules, each of which consists of
;   http-method
;   uri
;   an unordered list of terms, each of which consists of
;     name of the term, e.g. "ROLE1"
;     requirement of this term, e.g. "Y" meaning the user with "ROLE1" is allowed access


(defn- read-csv-file [filepath]
  (with-open [in-file (io/reader filepath)]
    (doall
      (csv/read-csv in-file))))

(defn- parse-rule [term-names rule]
  (zipmap (assoc term-names
            0 :method
            1 :uri)
          rule))

(defn- parse [[term-names & rules]]
  (map (partial parse-rule term-names)
       (take-while #(-> %
                        first
                        (not= ""))
                   rules)))

(defn parse! [filepath]
  (parse (read-csv-file filepath)))