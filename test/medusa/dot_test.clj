(ns medusa.dot-test
  (:require [clojure.test :refer :all]
            [fixtures.core :as fixtures]
            [fixtures.utils :as utils]
            [medusa.dot :as dot]))

(use-fixtures :once
              fixtures/expound-printer
              fixtures/instrument)

(deftest subscription-test
  (let [subs {:a.some-ns/foo #{:db}
              :b.some-ns/bar #{:db}
              :c.some-ns/zed #{:a.some-ns/foo :b.some-ns/bar}}]

    (testing "subs->nodes"
      (let [nodes    (dot/hierarchy->nodes subs)
            expected #{:db
                       :a.some-ns/foo
                       :b.some-ns/bar
                       :c.some-ns/zed}]
        (is (= expected nodes))))

    (testing "subs->edges"
      (let [nodes    (dot/hierarchy->edges subs)
            expected [["_a_some_ns_foo" "_db"]
                      ["_b_some_ns_bar" "_db"]
                      ["_c_some_ns_zed" "_a_some_ns_foo"]
                      ["_c_some_ns_zed" "_b_some_ns_bar"]]]
        (is (= (set expected) (set nodes)))))))

(deftest node-test
  (testing "node->descriptor"
    (let [count nil]
      (is (= (dot/node->descriptor count :db)
             {:label "db"}))
      (is (= (dot/node->descriptor count :a.some-ns/foo)
             {:label ":a.some-ns/foo"})))
    (let [count 1]
      (is (= (dot/node->descriptor count :db)
             {:label "db"}))
      (is (= (dot/node->descriptor count :a.some-ns/foo)
             {:label ":some-ns/foo"})))
    (let [count 2]
      (is (= (dot/node->descriptor count :db)
             {:label "db"}))
      (is (= (dot/node->descriptor count :a.some-ns/foo)
             {:label "foo"}))))

  (testing "node->cluster"
    (let [node->cluster (partial dot/node->cluster 3)]
      (is (= (node->cluster :db)
             nil))
      (is (= (node->cluster :a.some-ns/foo)
             "a_some-ns")))))

(deftest cluster-test
  (testing "cluster->descriptor"
    (is (= (dot/cluster->descriptor "a.some-ns")
           {:label   ":a.some-ns"
            :bgcolor "#C8C8C8"}))))

(deftest hierarchy-generative-tests
  (testing "hierarchy related method generatively"
    (is (utils/passes-generative-testing? `#'dot/hierarchy->nodes))
    (is (utils/passes-generative-testing? `#'dot/hierarchy->edges))))

(deftest node-generative-tests
  (testing "node related method generatively"
    (is (utils/passes-generative-testing? `#'dot/node->id))
    (is (utils/passes-generative-testing? `#'dot/node->cluster))))

(deftest cluster-generative-tests
  (testing "cluster related method generatively"
    (is (utils/passes-generative-testing? `#'dot/cluster->id))))
