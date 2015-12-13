(ns dfence.utils
  (:require [clojure.string :as string]
            [clojure.string :refer [lower-case]])
  (:import (java.net URL)))

(defn capitalise-all-words
  [s]
  (->> (string/split (str s) #"-")
       (map string/capitalize)
       (string/join "-")))

(defn update-when [m k pred f]
  (if (pred (get m k))
    (update m k f)
    m))

(defn- replace-first-with [url to-replace replace-with]
  (if (not (empty? to-replace))
    (clojure.string/replace-first url
                                  (re-pattern to-replace)
                                  replace-with)
    url))

(defn- parse-url [url]
  (let [url-obj (URL. url)]
    {:protocl (.getProtocol url-obj)
     :host    (.getHost url-obj)}
     :port    (.getPort url-obj)))

(defn location-matches? [test-scheme test-host test-port location]
  (when (not (empty? location))
    (let [{:keys [protocol host port]} (parse-url location)]
      (and (= (lower-case test-scheme) protocol)
           (= (lower-case test-host) host)
           (or (= test-port port)
               (and (= -1 port)
                    (= 80 test-port)))))))

(defn generate-new-location [location new-protocol new-host new-port]
  (let [{:keys [protocol host port]} (parse-url location)]
    (cond-> location
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

(def lower-case-keyword (comp keyword lower-case))