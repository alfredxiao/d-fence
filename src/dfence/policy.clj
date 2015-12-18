(ns dfence.policy
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.string :refer [upper-case trim split]]
            [dfence.utils :refer [lower-case-keyword]]))

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

(defn- normalize-term-name [term-name data-fact-terms]
  (if (contains? (set data-fact-terms) term-name)
    (keyword (str "data:" (name term-name)))
    term-name))

(defn- parse-one-policy
  "Parse one policy csv line, matching policy contents with policy header names
  and take into account some custom-defined data facts used as part of policy
  declaration"
  [[_ _ _ & header-names]
   defined-data-facts
   [method uri matching-rule & asserts]]
  (merge {:method         method
          :uri            uri
          :matching-rule  (lower-case-keyword matching-rule)}
         (let [all (zipmap (map lower-case-keyword header-names)
                           (map assert-value asserts))]
           (into {} (for [[k v] all]
                      (when v [(normalize-term-name k defined-data-facts)
                               (if (= :matching-rule k)
                                 (lower-case-keyword (first v))
                                 v)]))))))

(defn- parse-policy-csv [[header-names & unparsed-policy-lines] defined-data-facts]
  (map (partial parse-one-policy header-names defined-data-facts)
       (remove (comp empty? first) unparsed-policy-lines)))

(defn parse-policy-file! [filepath defined-data-facts]
  (parse-policy-csv (read-csv-file filepath) defined-data-facts))