(ns medusa.ns-parse-test
  (:require [clojure.test :refer :all]
            [fixtures.core :as fixtures]
            [medusa.ns-parse :as ns-parse]))

(use-fixtures :once
              fixtures/expound-printer
              fixtures/instrument)

(deftest namespace-aliases-test
  (testing "empty declaration provides no aliases"
    (let [decl     '()
          aliases  (ns-parse/aliases-from-ns-decl decl)
          expected {}]
      (is (= aliases expected))))

  (testing "empty ns declaration provides no aliases"
    (let [decl     '(ns some.namespace.core)
          aliases  (ns-parse/aliases-from-ns-decl decl)
          expected {}]
      (is (= aliases expected))))

  (testing "plain [lib] provides no aliases"
    (let [decl     '(ns some.namespace.core
                      (:require [namespace.without.alias.core]))
          aliases  (ns-parse/aliases-from-ns-decl decl)
          expected {}]
      (is (= aliases expected))))

  (testing "[lib :refer [foo]] provides no aliases"
    (let [decl     '(ns some.namespace.core
                      (:require [some.namespace.core :refer [foo]]))
          aliases  (ns-parse/aliases-from-ns-decl decl)
          expected {}]
      (is (= aliases expected))))

  (testing "[lib :refer :all] provides no aliases"
    (let [decl     '(ns some.namespace.core
                      (:require [some.namespace.core :refer :all]))
          aliases  (ns-parse/aliases-from-ns-decl decl)
          expected {}]
      (is (= aliases expected))))

  (testing "[lib :as alias] provides an alias"
    (let [decl     '(ns some.namespace.core
                      (:require [some.namespace.core :as foo]))
          aliases  (ns-parse/aliases-from-ns-decl decl)
          expected {'foo 'some.namespace.core}]
      (is (= aliases expected))))

  (testing "[lib :as alias] provides an alias"
    (let [decl     '(ns some.namespace.core
                      (:require [some.namespace [core :as foo]]))
          aliases  (ns-parse/aliases-from-ns-decl decl)
          expected {'foo 'some.namespace.core}]
      (is (= aliases expected))))

  (testing "mixing different styles provides aliases"
    (let [decl     '(ns some.namespace.core
                      (:require [some.namespace.core :as foo]
                                [some.namespace.with.refer :refer [zed]]
                                [namespace.without.alias.core]
                                [referring.namespace.core :refer :all]
                                [some.prefixing.namespace [core :as bob]]
                                [another.namespace :as bar]))
          aliases  (ns-parse/aliases-from-ns-decl decl)
          expected {'foo 'some.namespace.core
                    'bar 'another.namespace
                    'bob 'some.prefixing.namespace.core}]
      (is (= aliases expected)))))
