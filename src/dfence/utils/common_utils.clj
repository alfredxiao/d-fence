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

(defn remove-kv
  "Remove map entries by key predicate and value predicate"
  [m key-pred value-pred]
  (into {} (for [[k v] m]
             (when-not (and (key-pred k)
                            (value-pred v))
               [k v]))))

(def lower-case-keyword (comp keyword lower-case))