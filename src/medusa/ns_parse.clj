(ns medusa.ns-parse
  "Based on clojure.tools.namespace.parse but unlike deps-from-ns-decl
  returns deps as a map"
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
    (match-lib+opts opts)))

(defn- match-prefix-list
  [prefix-list]
  (core-match/match [prefix-list]
    [{:prefix ns :libspecs libspecs}]
    (->> libspecs
         (map match-libspec)
         (map (partial add-prefix ns))
         (into {}))))

(defn match-body
  [body]
  (core-match/match [body]
    [([:libspec libspec] :seq)]
    (match-libspec libspec)

    [([:prefix-list prefix-list] :seq)]
    (match-prefix-list prefix-list)))

(defn match-clauses
  [clause]
  (core-match/match [clause]
    [([:require {:clause :require
                 :body   body}] :seq)]
    (mapcat match-body body)))

(defn match-ns-form
  [form]
  (core-match/match [form]
    [{:name _ :clauses clauses}]
    (mapcat match-clauses clauses)

    :else nil))

(defn aliases-from-ns-decl [decl]
  (let [form (rest decl)]
    (if-not (s/valid? ::core-specs/ns-form form)
      {}
      (->> form
           (s/conform ::core-specs/ns-form)
           (match-ns-form)
           (merge-with into {})))))
