(ns medusa.re-frame.reg-sub-test
  (:require [clojure.test :refer :all]
            [fixtures.core :as fixtures]
            [medusa.re-frame.core]
            [medusa.dispatch :as dispatch]))

(use-fixtures :once
              fixtures/expound-printer
              fixtures/instrument)

(deftest reg-sub-test
  (testing "subscription to db"
    (let [ctx      {:aliases {'rf 're-frame.core}
                    :file    "some-file"}
          token    '(rf/reg-sub
                      ::foo
                      (fn [db] (:foo db)))
          result   (dispatch/on-token token ctx)
          expected {:reg-sub {::foo {:file "some-file"
                                     :deps #{:db}}}}]
      (is (= expected result))))

  (testing "subscription using :<-"
    (let [ctx      {:aliases {'rf 're-frame.core}
                    :file    "some-file"}
          token    '(rf/reg-sub
                      ::foo
                      :<- [:another.ns/bar]
                      (fn [bar] (str bar)))
          result   (dispatch/on-token token ctx)
          expected {:reg-sub {::foo {:file "some-file"
                                     :deps #{:another.ns/bar}}}}]
      (is (= expected result))))

  (testing "subscription using signal function"
    (let [ctx      {:aliases {'rf 're-frame.core}
                    :file    "some-file"}
          token    '(rf/reg-sub
                      ::foo
                      (fn [[_ some-id _]]
                        [(rf/subscribe [:another.ns/bar some-id])])
                      (fn [[selected-options] [_ _ parameter-id]]
                        (list selected-options parameter-id)))
          result   (dispatch/on-token token ctx)
          expected {:reg-sub {::foo {:file "some-file"
                                     :deps #{:another.ns/bar}}}}]
      (is (= expected result)))))
