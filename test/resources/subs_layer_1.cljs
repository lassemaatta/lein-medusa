(ns app.subs-layer-1
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub
  ::name
  (fn [db]
      (:name db)))

(re-frame/reg-sub
  ::age
  (fn [db]
      (:age db)))
