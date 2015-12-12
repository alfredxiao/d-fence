(ns dfence.rule
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.string :refer [upper-case lower-case]]))

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

(defn- extract-value [term]
  (when (contains? #{"Y" "YES" "X" "TRUE"} (upper-case (str term)))
    true))

(defn- to-primitive [term-name]
  (-> term-name lower-case keyword))

(defn- parse-rule [[_ _ & names] [method uri & terms]]
  (merge {:method method
          :uri uri}
         (zipmap (map to-primitive names)
                 (map extract-value terms))))

(defn- parse-rule-set [[names & rules]]
  (map (partial parse-rule names)
       (take-while #(-> %
                        first
                        (not= ""))
                   rules)))

(defn parse-rule-set! [filepath]
  (parse-rule-set (read-csv-file filepath)))