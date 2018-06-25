(ns medusa.core
  (:require [clojure.pprint :as pprint]
            [clojure.string :as string]
            [medusa.files :as files]
            [medusa.dot :as dot]
            [medusa.dispatch :as dispatch]
            [medusa.re-frame.core]))

(defn- project->sources [project]
  (->> project
       (:source-paths)
       (files/find-cljs-sources)))

(defn- write-output
  [output-file data]
  (if output-file
    (spit output-file data)
    (println data)))

(defn- output-as-map
  [output-fn results]
  (let [data   (into (sorted-map) results)
        output (with-out-str (pprint/pprint data))]
    (output-fn output)))

(defn- wrap-for-plantuml
  [graph]
  (str "@startuml\n"
       ; Plantuml doesn't accept quotes in the graph declaration, e.g.:
       ; digraph "some graph" { ..
       (string/replace-first graph #"^digraph \".+\" \{" "digraph title {")
       "@enduml"))

(defn- output-as-dot
  [output-fn results {:keys [plantuml?] :as opts}]
  (let [lift-deps (fn [m k v] (assoc m k (:deps v)))
        data      (reduce-kv lift-deps {} (:reg-sub results))
        dot-graph (dot/hierarchy->dot data opts)]
    (output-fn (if plantuml?
                 (wrap-for-plantuml dot-graph)
                 dot-graph))))

(defn go [project {:keys [output-as-dot? output-file] :as opts}]
  (let [files (project->sources project)]
    (when (seq files)
      (let [output-fn (partial write-output output-file)
            results   (files/loop-files files dispatch/on-token)]
        (cond
          output-as-dot? (output-as-dot output-fn results opts)
          :else (output-as-map output-fn results))))))
