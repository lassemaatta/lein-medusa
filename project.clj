(defproject org.clojars.lassemaatta/lein-medusa "0.1.2-SNAPSHOT"
  :description "Parse and export re-frame subscription graph"
  :url "https://github.com/lassemaatta/lein-medusa"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/core.specs.alpha "0.2.36"]
                 [org.clojure/core.match "0.3.0-alpha5"]
                 [org.clojure/tools.cli "0.3.7"]
                 [org.clojure/tools.reader "1.3.0"]
                 [org.clojure/tools.namespace "0.3.0-alpha4"]
                 [macroz/tangle "0.2.0"]]

  :profiles {:dev {:dependencies   [[org.clojure/clojure "1.9.0"]
                                    [org.clojure/test.check "0.9.0"]
                                    [org.clojure/tools.nrepl "0.2.13"]
                                    [leiningen-core "2.8.1"]
                                    [expound "0.7.1"]]
                   :resource-paths ["test/resources"]}})
