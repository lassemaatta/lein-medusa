(ns medusa.re-frame.reg-sub
  (:require [medusa.dispatch :as dispatch]))

(defn- dispatch-reg-sub [_ input-args]
  (count input-args))

(defmulti get-subscriptions
  "Parse the body of a reg-sub registration and determine which
  subscriptions (or the app db) form the dependencies for this subscription"
  dispatch-reg-sub)

(defmethod get-subscriptions 0
  ^{:doc "A subscription which targets the app db has no extra arguments,
          as the reg-sub body consists of just the query id and function:

          (rf/reg-sub
            ::foo
            (fn [db]
              ...))"}
  [_ _]
  #{:db})

(defmethod get-subscriptions 1
  ^{:doc "A subscription using signal functions has a single argument,
          in the form of:

          (rf/reg-sub
            ::foo
            (fn [[_ some-id _]]
              [(rf/subscribe [:another.ns/bar some-id])
               (rf/subscribe [:another.ns/zed some-id])])
            (fn [...]
              ...)) "}
  [ctx input-args]

  (let [signal-fn (first input-args)
        sub-vec   (last signal-fn)]
    (when (vector? sub-vec)
      (->> sub-vec
           (map #(dispatch/on-token % ctx))
           (map :subscribe)
           (into #{})))))

(defmethod get-subscriptions :default
  ^{:doc "A subscription using the :<- sugar has an even number
          of arguments:

          (rf/reg-sub
            ::foo
            :<- [:another.ns/bar]
            :<- [:another.ns/zed]
            (fn [...]
              ...))"}
  [_ input-args]
  (when (even? (count input-args))
    (let [pairs   (partition 2 input-args)
          markers (map first pairs)
          vecs    (map last pairs)]
      (when (and (every? #{:<-} markers)
                 (every? vector? vecs))
        (->> vecs
             (map first)
             (into #{}))))))

(defn- on-reg-sub [token {:keys [file] :as ctx}]
  (let [args          (rest token)
        query-id      (first args)
        input-args    (butlast (rest args))
        subscriptions (get-subscriptions ctx input-args)]
    (when (some? subscriptions)
      {:reg-sub {query-id {:file file
                           :deps subscriptions}}})))

(defmethod dispatch/on-token 're-frame.core/reg-sub
  [token ctx]
  (on-reg-sub token ctx))

(defmethod dispatch/on-token 're-frame.subs/reg-sub
  [token ctx]
  (on-reg-sub token ctx))
