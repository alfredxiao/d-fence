(ns dfence.utils
  (:require [clojure.string :as string]))

(defn capitalise-all-words
  [s]
  (->> (string/split (str s) #"-")
       (map string/capitalize)
       (string/join "-")))

(defn dissoc-if [m k p]
  (if (p (get m k))
    (dissoc m k)
    m))