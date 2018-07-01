(ns medusa.ns-parse
  "Based on clojure.tools.namespace.parse but unlike deps-from-ns-decl
  returns deps as a map")

(defn- prefix-spec?
  "Returns true if form represents a libspec prefix list like
  (prefix name1 name1) or [com.example.prefix [name1 :as name1]]"
  [form]
  (and (sequential? form)                                   ; should be a list, but often is not
       (symbol? (first form))
       (not-any? keyword? form)
       (< 1 (count form))))                                 ; not a bare vector like [foo]

(defn- alias-spec?
  "Returns true if form represents a libspec like [namespace :as alias]"
  [form]
  (and (sequential? form)
       (symbol? (first form))
       (= :as (second form))))

(defn- js-spec?
  [form]
  "Returns true if form represents a libspec like [\"some-js-lib\" :as alias]"
  (and (sequential? form)
       (string? (first form))
       (< 1 (count form))))

(defn- kw-spec?
  "Returns true if form represents a libspec vector containing optional
  keyword arguments like [namespace :as alias] or
  [namespace :refer (x y)] or just [namespace]"
  [form]
  (and (sequential? form)
       (symbol? (first form))
       (or (= 1 (count form))
           (keyword? (second form)))))

(defn- lib-name [prefix form]
  (if prefix
    (symbol (str prefix "." (first form)))
    (first form)))

(defn- deps-from-libspec [prefix form]
  (cond (prefix-spec? form)
        (into [] (mapcat (fn [f] (deps-from-libspec
                                   (lib-name prefix form)
                                   f))
                         (rest form)))
        (alias-spec? form)
        (let [lib-name (lib-name prefix form)
              alias    (nth form 2)]
          [alias lib-name])
        (js-spec? form) nil
        (kw-spec? form) nil
        (symbol? form) nil
        (keyword? form) nil
        :else
        (throw (ex-info "Unparsable namespace form"
                        {:reason ::unparsable-ns-form
                         :form   form}))))

(def ^:private ns-clause-head-names
  "Set of symbol/keyword names which can appear as the head of a
  clause in the ns form."
  #{"use" "require"})

(def ^:private ns-clause-heads
  "Set of all symbols and keywords which can appear at the head of a
  dependency clause in the ns form."
  (set (mapcat (fn [name] (list (keyword name)
                                (symbol name)))
               ns-clause-head-names)))

(defn- deps-from-ns-form [form]
  (when (and (sequential? form)                             ; should be list but sometimes is not
             (contains? ns-clause-heads (first form)))
    (map #(deps-from-libspec nil %) (rest form))))

(defn aliases-from-ns-decl
  "Given an (ns...) declaration form (unevaluated), returns a map of
  symbols naming the dependencies of that namespace.  Handles :use and
  :require clauses but not :load."
  [decl]
  (into {} (mapcat deps-from-ns-form decl)))
