(ns fixtures.core
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as test]
            [expound.alpha :as expound]))

(defn instrument
  "Enable instrumentation during tests"
  [f]
  (test/instrument)
  (try
    (f)
    (finally
      (test/unstrument))))

(defn expound-printer
  "Use expound to pretty-print spec errors"
  [f]
  (binding [s/*explain-out* expound/printer]
    (f)))
