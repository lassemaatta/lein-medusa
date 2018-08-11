(ns leiningen.medusa
  (:require [clojure.tools.cli :as cli]
            [leiningen.core.eval :as lein]
            [leiningen.core.project :as project]))

(def ^:private medusa-profile
  {:dependencies '[[org.clojars.lassemaatta/lein-medusa "0.1.2-SNAPSHOT"]]})

(def ^:private cli-options
  [["-H" "--help"
    "Show help"
    :default false]
   ["-g" "--graph"
    "Output subscription hierarchy as a .dot graph to stdout"
    :default false]
   ["-t" "--title TITLE"
    "Title for the graph"
    :default "Graph"]
   ["-d" "--depth DEPTH"
    "Cluster subscriptions which share the first N elements of their namespace"
    :parse-fn #(Integer/parseInt %)]
   ["-p" "--plantuml"
    "Wrap .dot output with @startuml & @enduml for integrating with plantuml"]
   ["-o" "--output FILE"
    "Write output to FILE instead of stdout"]])

(defn- display-summary
  [summary]
  (println summary))

(defn- display-errors
  [errors]
  (println "Error parsing arguments:")
  (run! println errors))

(defn medusa
  "I parse re-frame sources and provide graphs"
  [project & args]
  (let [{:keys [options summary errors]} (cli/parse-opts args cli-options)]
    ;(println errors)
    (cond
      ; Check for errors
      (some? errors)
      (display-errors errors)

      ; Print help
      (:help options)
      (display-summary summary)

      ; Run program
      :else
      (lein/eval-in-project
        (project/merge-profiles project [medusa-profile])
        `(if (medusa.core/go
               '~project
               {:output-as-dot? (:graph ~options)
                :plantuml?      (:plantuml ~options)
                :cluster-depth  (:depth ~options)
                :title          (:title ~options)
                :output-file    (:output ~options)})
           (System/exit -1)
           (System/exit 0))
        '(require 'medusa.core)))))
