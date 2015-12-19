(ns dfence.policy
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.string :refer [upper-case trim split]]
            [dfence.utils.common-utils :refer [lower-case-keyword remove-kv]]))

(defn- read-csv-file [filepath]
  (with-open [in-file (io/reader filepath)]
    (doall
      (csv/read-csv in-file))))

(defn- enumeration
  "Parses raw string value into a list of value split by ','"
  [required-value]
  (->> (split required-value #",")
       (mapv trim)
       (remove nil?)))

(defn- normalise
  "Normalise raw required value into standardised value(s), which consists of
   three categories: nil, true/false, [name1, name2]"
  [required-value]
  (let [trimmed-value (trim required-value)]
    (cond
      (empty? trimmed-value) nil
      (contains? #{"X" "x"} trimmed-value) true
      (contains? #{"-"} trimmed-value) false
      :else (enumeration trimmed-value))))

(defn- parse-one-policy
  "Parse one policy csv line, matching policy contents with policy header names
  and take into account some custom-defined data facts used as part of policy
  declaration. It should remove those requirement entries with nil value"
  [[_      _   _             & requirement-names]
   [method uri matching-rule & required-values]]
  (-> {:method         (upper-case method)
       :uri            uri
       :matching-rule  (lower-case-keyword matching-rule)}
      (merge (zipmap (map lower-case-keyword requirement-names)
                     (map normalise required-values)))
      (remove-kv identity nil?)))

(defn- parse-policy-csv [[header-names & unparsed-policy-lines]]
  (map (partial parse-one-policy header-names)
       (remove (comp empty? first) unparsed-policy-lines)))

(defn parse-policy-file! [filepath]
  (parse-policy-csv (read-csv-file filepath)))