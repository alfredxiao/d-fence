(ns dfence.evaluate
  (:require [dfence.utils :as utils]
            [clojure.set :refer [intersection]]))

(defn- is-request-related-to-rule? [request-method request-uri rule]
  (and (utils/matches-pattern? (:method rule) request-method)
       (first (utils/uri-match (:uri rule) request-uri))))

(defn- applicable-rules [rules request-method request-uri]
  (filter #(is-request-related-to-rule? request-method request-uri %) rules))

(defn- rule-asserts [rule]
  (let [asserts (dissoc rule :method :uri :dfence-matcher)]
    asserts
    #_(into #{} (apply concat (for [[k v] asserts :when (not (nil? v))]
                              (cond
                                (true? v) [[k v]]
                                (sequential? v) (mapv #(identity [k %]) v)))))))

(defn- assert-is-satisfied? [user-asserts [term required-value]]
  (cond
        (nil? required-value) false
        (true? required-value) (= required-value (get user-asserts term))
        (sequential? required-value) (contains? (set required-value) (get user-asserts term))
        ))

(defn- match-asserts [user-asserts rule-asserts matcher]
  ;(prn "user-asserts" user-asserts)
  ;(prn "rule-asserts" rule-asserts)
  ;(prn "matcher" matcher)
  (let [pass-access-check (partial assert-is-satisfied? user-asserts)]
    ;(prn "any ha" (some pass-access-check rule-asserts))
    (case matcher
      :any (some pass-access-check rule-asserts)
      :all (every? pass-access-check rule-asserts))))

  #_(not (empty? (intersection user-asserts
                             rule-asserts)))

(defn eval-rule [facts rule]
  (let [user-asserts (:asserts facts)]
    (if (not (:has-valid-token user-asserts))
      :authentication-required
      (if (match-asserts user-asserts
                         (rule-asserts rule)
                         (:dfence-matcher rule))
        :allow
        :access-denied))))

(defn evaluate-rules [rules {:keys [request-method request-uri] :as facts}]
  (let [rules-to-eval (applicable-rules rules request-method request-uri)]
    (if (empty? rules-to-eval)
      :allow
      (some #(eval-rule facts %) rules-to-eval))))