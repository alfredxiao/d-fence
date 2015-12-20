(ns dfence.utils.url-utils
  (:require [clojure.string :refer [lower-case split join]])
  (:import (java.net URL)))


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

(defn- replace-first-with [url to-replace replace-with]
  (if (not (empty? to-replace))
    (clojure.string/replace-first url
                                  (re-pattern to-replace)
                                  replace-with)
    url))

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

(defn- separator? [ch]
  (= ch \/))

(defn parse-uri [uri]
  (map join (partition-by separator? uri)))

(defn- is-param? [pattern]
  (re-matches #":[a-zA-Z]+" pattern))

(defn- match-uri-part [pattern part]
  (cond
    (= "*" pattern) {}
    (is-param? pattern) {(keyword (subs pattern 1)) part}
    (= pattern part) {}))

(defn- amend-uri [parsed-uri parsed-pattern]
  (concat parsed-uri (when (and (= 1 (- (count parsed-pattern)
                                        (count parsed-uri)))
                                (is-param? (last parsed-pattern))
                                (= "/" (last parsed-uri)))
                       [""])))

(defn match-uri
  "Matches a URI against a pattern. It returns two outcomes: matches or not, and
  destructured parameters.
  E.g. uri-pattern     uri          -> [matches?  params]
       /update/:id     /update/123  -> true, {:id '123'}
       /update/123     /update/123  -> true, {}
       /update/abc     /update/123  -> false, {}
       /update/abc/def /update/123  -> nil"
  [uri-pattern uri]
  (let [parsed-pattern (parse-uri uri-pattern)
        amended-uri (amend-uri (parse-uri uri) parsed-pattern)
        match-outcome (map match-uri-part parsed-pattern amended-uri)]
    (when (= (count amended-uri)
             (count parsed-pattern))
      [(every? map? match-outcome) (apply merge match-outcome)])))