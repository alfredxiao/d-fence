(ns dfence.evaluate
  (:require [dfence.utils :as utils]
            [clojure.set :refer [intersection]]))

(defn- is-request-related-to-rule? [request-method request-uri rule]
  (and (utils/matches-pattern? (:method rule) request-method)
       (utils/matches-pattern? (:uri rule) request-uri)))

(defn- applicable-rules [rules request-method request-uri]
  (filter #(is-request-related-to-rule? request-method request-uri %) rules))

(defn- required-asserts [rule]
  (-> rule
      (dissoc :method :uri)
      (utils/filter-kv keyword? true?)))

(defn- has-common-asserts? [user-asserts required-asserts]
  (not (empty? (intersection (set user-asserts)
                             (set required-asserts)))))

(defn eval-rule [facts rule]
  (let [user-asserts (:asserts facts)]
    (prn "user-asserts" user-asserts)
    (if (not (:has-valid-token user-asserts))
      :authentication-required
      (if (has-common-asserts? user-asserts
                               (required-asserts rule))
        :allow
        :access-denied))))

(defn evaluate-rules [rules {:keys [request-method request-uri] :as facts}]
  (let [rules-to-eval (applicable-rules rules request-method request-uri)]
    (if (empty? rules-to-eval)
      :allow
      (some #(eval-rule facts %) rules-to-eval))))