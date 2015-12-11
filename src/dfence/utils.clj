(ns dfence.utils
  (:require [clojure.string :as string]
            [cemerick.url :refer [url]]))

(defn capitalise-all-words
  [s]
  (->> (string/split (str s) #"-")
       (map string/capitalize)
       (string/join "-")))

(defn update-when [m k f]
  (if (get m k)
    (update m k f)
    m))


(defn- replace-first-with [url to-replace replace-with]
  (if (not (empty? to-replace))
    (clojure.string/replace-first url
                                  (re-pattern to-replace)
                                  replace-with)
    url))

(defn replace-url-parts [url new-protocol new-host new-port]
  (let [{:keys [protocol host port]} (url url)]
    (cond-> url
            (empty? protocol)         #(str new-protocol %)
            (not (empty? protocol))   (replace-first-with protocol new-protocol)
            true                      (replace-first-with host new-host)
            (not (nil? port))         (replace-first-with (str port) new-port)
            (or (nil? port)
                (< port 0))           (replace-first-with new-host (str new-host
                                                                        (when (not (= 80 new-port))
                                                                              (str ":" new-port)))))))

(defn- wildcard-to-re-pattern [wildcard-str]
  (-> wildcard-str
      (clojure.string/replace #"\?" ".?")
      (clojure.string/replace #"\*" ".*")
      re-pattern))

(defn matches-pattern? [wildcard-str actual-str]
  (re-matches (wildcard-to-re-pattern wildcard-str) actual-str))

(defn filter-kv
  "Filter a map by key pred and value pred"
  [m kp vp]
  (into {}
        (for [[k v] m]
          (when (and (kp k)
                     (vp v))
            [k v]))))