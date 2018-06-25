(ns app.subs-layer-2
  (:require [re-frame.core :as re-frame]
            [app.subs-layer-1 :as layer-1]))

(re-frame/reg-sub
  ::name-and-age
  :<- [::layer-1/name]
  :<- [::layer-1/age]
  (fn [[name age]]
      (str name ":" age)))

(re-frame/reg-sub
  ::age-and-name
  (fn [[_]]
      [(re-frame/subscribe [::layer-1/name])
       (re-frame/subscribe [::layer-1/age])])
  (fn [[name age]]
      (str name ":" age)))
