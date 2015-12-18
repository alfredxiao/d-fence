(ns dfence.evaluate
  (:require [dfence.utils :as utils]
            [clj-http.client :as client]
            [clojure.set :refer [intersection]]))

(defn- convert-to-rule-with-params [request-method request-uri rule]
  (let [[uri-matches? uri-params] (utils/uri-match (:uri rule) request-uri)]
    (when (and (utils/matches-pattern? (:method rule) request-method)
               uri-matches?)
      {:rule rule
       :params uri-params})))

(defn- applicable-rule-and-params [rules request-method request-uri]
  (remove nil? (map #(convert-to-rule-with-params request-method request-uri %) rules)))

(defn- rule-asserts [rule]
  (dissoc rule :method :uri :matching-rule))

(defn- is-data-fact? [term]
  (.startsWith (name term) "data:"))

(defn- apply-params [params uri-part]
  (if-let [matched-param (first (filter #(= (str %) uri-part) (keys params)))]
    (params matched-param)
    uri-part))

(defn- replace-params [template params]
  (let [temp-parts (clojure.string/split template #"/")]
    (clojure.string/join "/" (map #(apply-params params %) temp-parts))))

(defn- fetch-fact! [term fact-params api-server-config]
  (let [configured-term (keyword (subs (str term) 6))
        url-template (str (:scheme api-server-config) "://"
                          (:host api-server-config) ":"
                          (:port api-server-config)
                          (-> api-server-config :facts configured-term))
        url (replace-params url-template fact-params)]
    (:body (client/get url))))

(defn- assert-is-satisfied? [user-asserts fact-params api-server-config [term required-value]]
  (let [with-more-asserts (merge user-asserts
                                 (when (is-data-fact? term)
                                   {term (fetch-fact! term fact-params api-server-config)}))]
    (cond
      (true? required-value) (= required-value (get with-more-asserts term))
      (sequential? required-value) (contains? (set required-value) (get with-more-asserts term))
      )))

(defn- match-asserts [user-asserts api-server-config fact-params rule-asserts matcher]
  (let [pass-access-check (partial assert-is-satisfied? user-asserts fact-params api-server-config)]
    (case matcher
      :any (some pass-access-check rule-asserts)
      :all (every? pass-access-check rule-asserts))))

(defn eval-rule [facts api-server-config {:keys [rule params]}]
  (let [user-asserts (:asserts facts)]
    (if (not (:has-valid-token user-asserts))
      :authentication-required
      (if (match-asserts user-asserts
                         api-server-config
                         params
                         (rule-asserts rule)
                         (:matching-rule rule))
        :allow
        :access-denied))))

(defn evaluate-rules [rules {:keys [request-method request-uri] :as facts} api-server-config]
  (let [rules-to-eval (applicable-rule-and-params rules request-method request-uri)]
    (if (empty? rules-to-eval)
      :allow
      (some #(eval-rule facts api-server-config %) rules-to-eval))))