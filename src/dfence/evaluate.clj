(ns dfence.evaluate
  (:require [dfence.utils :as utils]
            [clojure.set :refer [intersection]]))

(defn- is-request-related-to-rule? [request-method request-uri rule]
  (and (utils/matches-pattern? (:method rule) request-method)
       (utils/matches-pattern? (:uri rule) request-uri)))

(defn- relevant-rules [rules request-method request-uri]
  (filter #(is-request-related-to-rule? request-method request-uri %) rules))

(defn- required-rule-primitives [rule]
  (-> rule
      (dissoc :method :uri)
      (utils/filter-kv keyword? true?)))

(defn- has-common-primitives [fact-primitives rule-primitives]
  (not (empty? (intersection (set fact-primitives)
                             (set rule-primitives)))))

(defn eval-rule [facts rule]
  (let [fact-primitives (:primitives facts)]
    (if (not (:has-valid-token fact-primitives))
      :authentication-required
      (if (has-common-primitives fact-primitives
                                 (required-rule-primitives rule))
        :allow
        :access-denied))))

(defn evaluate-rules [rules {:keys [request-method request-uri] :as facts}]
  (let [applicable-rules (relevant-rules rules request-method request-uri)]
    (if (empty? applicable-rules)
      :allow
      (some #(eval-rule facts %) applicable-rules))))