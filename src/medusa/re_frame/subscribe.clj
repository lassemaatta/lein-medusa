(ns medusa.re-frame.subscribe
  (:require [medusa.dispatch :as dispatch]))

(defn- on-subscribe [token _]
  (let [query (first (rest token))]
    {:subscribe (first query)}))

(defmethod dispatch/on-token 're-frame.core/subscribe
  [token ctx]
  (on-subscribe token ctx))

(defmethod dispatch/on-token 're-frame.subs/subscribe
  [token ctx]
  (on-subscribe token ctx))
