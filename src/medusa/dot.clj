(ns medusa.dot
  (:require [tangle.core :as tangle]
            [clojure.string :as string]
            [clojure.set :as set]
            [clojure.spec.alpha :as s]))

(def ^:private invalid-id-char #"[^_0-9a-zA-Z\0200-\0377]")

(def ^:private id-regex #"[_a-zA-Z\0200-\0377][_0-9a-zA-Z\0200-\0377]*")

(s/def ::id (s/and string?
                   #(re-matches id-regex %)))

(s/def ::node (s/with-gen
                keyword?
                #(s/gen #{:a.some-ns/foo :a.some-ns/bar :db})))
(s/def ::nodes (s/coll-of ::node :kind set?))

(s/def ::hierarchy (s/map-of ::node ::nodes))

(s/def ::cluster (s/with-gen
                   (s/nilable (s/and not-empty string?))
                   #(s/gen #{"a.some-ns", "b.another-ns"})))

(s/def ::edge (s/coll-of ::id :kind vector? :count 2))

(defn- ->dot-id [kw]
  (-> kw
      (str)
      (string/replace invalid-id-char "_")))

(defn node->id [node]
  (->dot-id node))

(s/fdef node->id
        :args (s/cat :node ::node)
        :ret ::id)

(defn hierarchy->nodes [hierarchy]
  (let [parents  (->> hierarchy
                      (keys)
                      (set))
        children (->> hierarchy
                      (vals)
                      (apply set/union))]
    (set/union parents children)))

(s/fdef hierarchy->nodes
        :args (s/cat :hierarchy ::hierarchy)
        :ret (s/coll-of ::node :kind set?))

(defn- item->edge [[parent children]]
  (->> children
       (map #(vector (node->id parent) (node->id %)))
       (into [])))

(defn hierarchy->edges [hierarchy]
  (->> hierarchy
       (map item->edge)
       (apply concat)
       (into [])))

(s/fdef hierarchy->edges
        :args (s/cat :hierarchy ::hierarchy)
        :ret (s/nilable (s/coll-of ::edge)))

(defn- split-ns
  [ns]
  (string/split ns #"\."))

(defn node->descriptor [count node]
  (let [name    (name node)
        ns      (namespace node)
        ns-part (some->> ns
                         (split-ns)
                         (drop (or count 0))
                         (seq)
                         (string/join "."))
        no-ns?  (nil? ns-part)]
    {:label
     (if no-ns?
       name
       (str ":" ns-part "/" name))}))

(s/fdef node->descriptor
        :args (s/cat :count (s/nilable pos-int?)
                     :node ::node))

(defn node->cluster [count node]
  (if-let [ns (some->> node
                       (namespace)
                       (split-ns)
                       (take count)
                       (string/join "."))]
    ns))

(s/fdef node->cluster
        :args (s/cat :count pos-int?
                     :node ::node)
        :ret ::cluster)

(defn cluster->id [cluster]
  (->dot-id cluster))

(s/fdef cluster->id
        :args (s/cat :cluster ::cluster)
        :ret (s/nilable ::id))

(defn cluster->descriptor [cluster]
  {:label   (str ":" cluster)
   :bgcolor "#C8C8C8"})

(s/fdef cluster->descriptor
        :args (s/cat :cluster ::cluster))

(defn hierarchy->dot [hierarchy {:keys [cluster-depth title] :as opts}]
  (let [nodes               (hierarchy->nodes hierarchy)
        edges               (hierarchy->edges hierarchy)
        clustering-enabled? (some? cluster-depth)
        opts                {:directed?           true
                             :node                {:shape     :box
                                                   :style     :filled
                                                   :fillcolor :white}
                             :node->id            node->id
                             :node->descriptor    (partial node->descriptor cluster-depth)
                             :node->cluster       (if clustering-enabled?
                                                    (partial node->cluster cluster-depth)
                                                    (constantly nil))
                             :cluster->id         cluster->id
                             :cluster->descriptor cluster->descriptor
                             :graph               {:rankdir :LR
                                                   :label   (or title "Graph")}}]
    (tangle/graph->dot nodes edges opts)))

(s/fdef hierarchy->dot
        :args (s/cat :hierarchy ::hierarchy))
