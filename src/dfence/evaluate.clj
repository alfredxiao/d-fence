(ns dfence.evaluate
  (:require [dfence.utils.url-utils :refer [match-uri]]
            [clj-http.client :as client]
            [clojure.string :refer [upper-case]]
            [clojure.set :refer [intersection]]))

(defn- enhance-policy-if-appropriate
  "When request method and URI matches a policy, return the policy with
  potential enhancement of data fact parameters. e.g. with uri pattern
  /update/:id and incoming uri as /update/123, enhance policy with a
  parameter map {:id '123'}"
  [request-method request-uri {:keys [method uri] :as policy}]
  (let [[uri-matches? params] (match-uri uri request-uri)]
    (when (and (contains? #{"ANY" request-method} method)
               uri-matches?)
      [policy params])))

(defn- relevant-policies-and-params
  "Filter policies that matches request method and URI, while destructuring
  data fact parameters if there are any"
  [policies request-method request-uri]
  (remove nil? (map (partial enhance-policy-if-appropriate request-method request-uri)
                    policies)))

(defn- is-data-fact? [condition api-server-config]
  (contains? (set (-> api-server-config :facts keys)) condition))

(defn- apply-params [params uri-part]
  (if-let [matched-param (first (filter #(= (str %) uri-part) (keys params)))]
    (params matched-param)
    uri-part))

(defn- replace-params [template params]
  (let [temp-parts (clojure.string/split template #"/")]
    (clojure.string/join "/" (map #(apply-params params %) temp-parts))))

(defn- fetch-data-fact! [term fact-params api-server-config]
  (let [url-template (str (:scheme api-server-config) "://"
                          (:host api-server-config) ":"
                          (:port api-server-config)
                          (-> api-server-config :facts term))
        url (replace-params url-template fact-params)]
    (:body (client/get url))))

(defn- condition-check [user-facts fact-params api-server-config [condition-name required-value]]
  (let [all-facts (merge user-facts
                         (when (is-data-fact? condition-name api-server-config)
                           {condition-name (fetch-data-fact! condition-name fact-params api-server-config)}))]
    (cond
      (true? required-value) (= required-value (get all-facts condition-name))
      (sequential? required-value) (contains? (set required-value) (get all-facts condition-name)))))

(defn evaluate-policy [user-facts api-server-config [policy fact-params]]
  (let [required-conditions (dissoc policy :method :uri :matching-rule)
        condition-is-met? (partial condition-check user-facts fact-params api-server-config)]
    (case (:matching-rule policy)
      :any (some condition-is-met? required-conditions)
      :all (every? condition-is-met? required-conditions))))

(defn evaluate-policies
  "Given user facts/asserts, and given custom-defined data-facts, evaluate all
  policies and decide whether to 'allow' access or not."
  [policies
   {:keys [request-method request-uri user-facts]}
   api-server-config]
  (let [policy-and-fact-params (relevant-policies-and-params policies request-method request-uri)]
    (cond
      (empty? policy-and-fact-params) :allow
      (not (:has-valid-token user-facts)) :authentication-required
      (some (partial evaluate-policy user-facts api-server-config) policy-and-fact-params) :allow
      :else :access-denied)))