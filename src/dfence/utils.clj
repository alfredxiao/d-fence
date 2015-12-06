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


(defn- replace-first-url-part [url to-replace replace-with]
  (if (not (empty? to-replace))
    (clojure.string/replace-first url
                                  (re-pattern to-replace)
                                  replace-with)
    url))

(defn new-url [old-location new-protocol new-host new-port]
  (let [{:keys [protocol host port]} (url old-location)]
    (cond-> old-location
            (empty? protocol)         #(str new-protocol %)
            (not (empty? protocol))   (replace-first-url-part protocol new-protocol)
            true                      (replace-first-url-part host new-host)
            (not (nil? port))         (replace-first-url-part (str port) new-port)
            (or (nil? port)
                (< port 0))           (replace-first-url-part new-host (str new-host
                                                                            (when (not (= 80 new-port))
                                                                              (str ":" new-port)))))))