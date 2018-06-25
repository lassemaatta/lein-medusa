(ns medusa.dispatch
  (:require [clojure.spec.alpha :as s]))

(s/def ::token sequential?)
(s/def ::aliases (s/map-of symbol? symbol?
                           :gen-max 5))

(s/def ::context (s/keys :req-un [::aliases]))

(defn- extract-fq-ns [ns aliases]
  (let [s (symbol ns)]
    (get aliases s s)))

(s/fdef extract-fq-ns
        :args (s/cat :ns string?
                     :aliases ::aliases)
        :ret symbol?)

(defn unalias-symbol
  [s aliases]
  (if-let [ns (namespace s)]
    (-> ns
        (extract-fq-ns aliases)
        (str)
        (symbol (name s)))
    s))

(s/fdef unalias-symbol
        :args (s/cat :s symbol?
                     :aliases ::aliases)
        :ret symbol?)

(defn unalias
  [item aliases]
  (if (symbol? item)
    (unalias-symbol item aliases)
    item))

(defn dispatch-token
  [token {:keys [aliases] :as ctx}]
  (let [item (first token)]
    (unalias item aliases)))

(s/fdef dispatch-token
        :args (s/cat :token ::token
                     :ctx ::context)
        :ret (s/nilable symbol?))

(defmulti on-token dispatch-token)

(defmethod on-token :default [_ _] nil)

(s/fdef on-token
        :args (s/cat :token ::token
                     :ctx ::context))
