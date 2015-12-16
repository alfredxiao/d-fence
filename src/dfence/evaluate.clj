(ns dfence.evaluate
  (:require [dfence.utils :as utils]
            [clojure.set :refer [intersection]]))

(defn- is-request-related-to-rule? [request-method request-uri rule]
  (and (utils/matches-pattern? (:method rule) request-method)
       (utils/matches-pattern? (:uri rule) request-uri)))

(defn- applicable-rules [rules request-method request-uri]
  (filter #(is-request-related-to-rule? request-method request-uri %) rules))

(defn- required-asserts [rule]
  (let [terms (dissoc rule :method :uri)]
    (into #{} (apply concat (for [[k v] terms :when (not (nil? v))]
                              (cond
                                (true? v) [[k v]]
                                (sequential? v) (mapv #(identity [k %]) v)))))))

(defn- has-common-asserts? [user-asserts required-asserts]
  (not (empty? (intersection user-asserts
                             required-asserts))))

(defn eval-rule [facts rule]
  (let [user-asserts (:asserts facts)]
    (if (not (:has-valid-token user-asserts))
      :authentication-required
      (if (has-common-asserts? (set user-asserts)
                               (required-asserts rule))
        :allow
        :access-denied))))

(defn evaluate-rules [rules {:keys [request-method request-uri] :as facts}]
  (let [rules-to-eval (applicable-rules rules request-method request-uri)]
    (if (empty? rules-to-eval)
      :allow
      (some #(eval-rule facts %) rules-to-eval))))