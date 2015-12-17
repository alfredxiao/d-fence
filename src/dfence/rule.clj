(ns dfence.rule
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.string :refer [upper-case trim split]]
            [dfence.utils :refer [lower-case-keyword]]))

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

(defn- assert-value [raw-string]
  (let [assert (trim raw-string)]
    (cond
      (empty? assert) nil
      (contains? #{"X" "x"} (upper-case assert)) true
      (.equalsIgnoreCase assert "Required") true
      :else (remove nil? (mapv trim (split assert #","))))))

(defn- parse-rule [[_ _ & assert-names] [method uri & asserts]]
  (let [parsed (merge {:method method
                       :uri uri}
                      (let [all (zipmap (map lower-case-keyword assert-names)
                                       (map assert-value asserts))]
                        (into {} (for [[k v] all]
                                   (when v [k v])))))]
    (assoc parsed :dfence-matcher
                  (if (some #(.equalsIgnoreCase "Required" %) asserts)
                    :all
                    :any))))

(defn- parse-rule-set [[assert-names & rules]]
  (map (partial parse-rule assert-names)
       (take-while #(-> %
                        first
                        (not= ""))
                   rules)))

(defn parse-rule-set! [filepath]
  (parse-rule-set (read-csv-file filepath)))