(ns medusa.dispatch-test
  (:require [clojure.test :refer :all]
            [fixtures.core :as fixtures]
            [fixtures.utils :as utils]
            [medusa.dispatch :as dispatch]))

(use-fixtures :once
              fixtures/expound-printer
              fixtures/instrument)

(deftest unaliasing-test
  (testing "no-op on simple items"
    (is (= '[] (dispatch/unalias '[] {})))
    (is (= '() (dispatch/unalias '() {})))
    (is (= 'foo (dispatch/unalias 'foo {})))
    (is (= "foo" (dispatch/unalias "foo" {}))))

  (testing "namespaced symbols are unaliased"
    (let [aliases {'bar 'some.ns.bar}]
      (is (= 'zed/foo (dispatch/unalias 'zed/foo aliases)))
      (is (= 'some.ns.bar/foo (dispatch/unalias 'bar/foo aliases))))))

(defmethod dispatch/on-token 'foo [_ _] 1)
(defmethod dispatch/on-token 'some.ns.bar/foo [_ _] 2)

(deftest dispatching-test
  (testing "dispatching unaliases symbols"
    (let [ctx {:aliases {'bar 'some.ns.bar}}]
      (is (= 1 (dispatch/on-token '(foo) ctx)))
      (is (= 2 (dispatch/on-token '(bar/foo) ctx))))))

(deftest unalias-symbol-generative-tests
  (testing "unalias-symbol generatively"
    (is (utils/passes-generative-testing? `#'dispatch/unalias-symbol))))
