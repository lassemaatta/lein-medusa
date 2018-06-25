(ns medusa.files-test
  (:require [clojure.test :refer :all]
            [fixtures.core :as fixtures]
            [medusa.files :as files]
            [medusa.dispatch :as dispatch]
            [medusa.re-frame.core]
            [clojure.java.io :as io]))

(use-fixtures :once
              fixtures/expound-printer
              fixtures/instrument)

(deftest read-file-with-layer-1-subscriptions
  (testing "parsing layer 1 subscriptions"
    (let [file     "test/resources/subs_layer_1.cljs"
          files    [(io/file file)]
          results  (files/loop-files files dispatch/on-token)
          expected {:reg-sub {:app.subs-layer-1/age  {:deps #{:db}
                                                      :file file}
                              :app.subs-layer-1/name {:deps #{:db}
                                                      :file file}}}]
      (is (= results expected)))))

(deftest read-file-with-layer-2-subscriptions
  (testing "parsing layer 2 subscriptions"
    (let [file     "test/resources/subs_layer_2.cljs"
          files    [(io/file file)]
          results  (files/loop-files files dispatch/on-token)
          expected {:reg-sub {:app.subs-layer-2/name-and-age {:deps #{:app.subs-layer-1/age
                                                                      :app.subs-layer-1/name}
                                                              :file file}
                              :app.subs-layer-2/age-and-name {:deps #{:app.subs-layer-1/age
                                                                      :app.subs-layer-1/name}
                                                              :file file}}}]
      (is (= results expected)))))
