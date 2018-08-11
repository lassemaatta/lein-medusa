(ns medusa.ns-parse
  (:require [clojure.spec.alpha :as s]
            [clojure.core.specs.alpha :as core-specs]
            [clojure.core.match :as core-match]))

(defn- add-prefix [prefix mapping]
  (let [lib   (first (vals mapping))
        alias (first (keys mapping))
        ns    (symbol (str prefix "." lib))]
    [alias ns]))

(defn- match-lib+opts
  [lib+opts]
  (core-match/match [lib+opts]
    [{:lib lib :options {:as alias}}]
    {alias lib}

    :else nil))

(defn- match-libspec
  [libspec]
  (core-match/match [libspec]
    [([:lib+opts opts] :seq)]
    (match-lib+opts opts)

    :else nil))

(defn- match-prefix-list
  [prefix-list]
  (core-match/match [prefix-list]
    [{:prefix ns :libspecs libspecs}]
    (->> libspecs
         (map match-libspec)
         (map (partial add-prefix ns))
         (into {}))

    :else nil))

(defn- match-body
  [body]
  (core-match/match [body]
    [([:libspec libspec] :seq)]
    (match-libspec libspec)

    [([:prefix-list prefix-list] :seq)]
    (match-prefix-list prefix-list)

    :else nil))

(defn- match-clauses
  [clause]
  (core-match/match [clause]
    [([:require {:clause :require
                 :body   body}] :seq)]
    (mapcat match-body body)

    :else nil))

(defn match-ns-form
  [form]
  (let [conformed (s/conform ::core-specs/ns-form form)]
    (core-match/match [conformed]
      [{:name _ :clauses clauses}]
      (->> clauses
           (mapcat match-clauses)
           (merge {}))

      :else nil)))

(s/fdef match-ns-form
  :args (s/cat :form (s/spec ::core-specs/ns-form)))

(defn aliases-from-ns-decl [decl]
  (let [ns   (first decl)
        form (rest decl)]
    (if (and (= 'ns ns) (s/valid? ::core-specs/ns-form form))
      (->> form
           (match-ns-form)
           (merge {}))
      {})))
