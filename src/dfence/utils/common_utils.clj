(ns dfence.utils.common-utils
  (:require [clojure.string :as string]
            [clojure.string :refer [lower-case]]))

(defn capitalise-all-words
  [s]
  (->> (string/split (str s) #"-")
       (map string/capitalize)
       (string/join "-")))

(defn update-when [m k pred f]
  (if (pred (get m k))
    (update m k f)
    m))

(defn- wildcard-to-re-pattern [wildcard-str]
  (-> wildcard-str
      (clojure.string/replace #"\?" ".?")
      (clojure.string/replace #"\*" ".*")
      re-pattern))

(defn matches-pattern? [wildcard-str actual-str]
  (re-matches (wildcard-to-re-pattern wildcard-str) actual-str))

(defn remove-kv
  "Remove map entries by key predicate and value predicate"
  [m key-pred value-pred]
  (into {} (for [[k v] m]
             (when-not (and (key-pred k)
                            (value-pred v))
               [k v]))))

(def lower-case-keyword (comp keyword lower-case))

; /update/123   /update/:id     -> True, {:id "123"}
; /update/123   /update/123     -> True, {}
; /update/123   /update/abc     -> False, {}
; /update/123   /update/abc/def -> False, {}

(defn- part-matched [a-part p-part]
  (or (= a-part p-part)
      (.startsWith p-part ":")
      (= "*" p-part)))

(defn- param-part [a-part p-part]
  (when (.startsWith p-part ":")
    [(keyword (subs p-part 1)) a-part]))

(defn uri-match [uri-pattern actual-uri]
  (let [actual-parts (clojure.string/split (subs actual-uri 1) #"/")
        pattern-parts (clojure.string/split (subs uri-pattern 1) #"/")]

    (let [matched? (and (= (count actual-parts)
                           (count pattern-parts))
                        (every? #(part-matched (first %) (second %)) (zipmap actual-parts pattern-parts)))
          params (if matched?
                   (into {} (for  [[k v] (zipmap actual-parts pattern-parts)]
                              (param-part k v)))
                   {})]
      [matched? params])))