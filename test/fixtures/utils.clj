(ns fixtures.utils
  (:require [clojure.test :refer :all]
            [clojure.test.check]                            ; required to avoid https://github.com/technomancy/leiningen/issues/2173
            [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as spec-test]
            [expound.alpha :as expound]))

(alias 'stc 'clojure.spec.test.check)

;; Default is 1000 tests
(def ^:private opts {::stc/opts {:num-tests 100}})

(defn passes-generative-testing?
  [fn-sym]
  (let [results    (spec-test/check fn-sym opts)
        no-result? (empty? results)
        passed?    (nil? (:failure (first results)))]
    (cond
      no-result? (println (str "No fdef for symbol: " fn-sym))
      passed? true
      :else (expound/explain-results results))))
