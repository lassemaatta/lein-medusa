(ns medusa.files
  (:require [clojure.tools.namespace.parse :as ns-parse]
            [clojure.tools.namespace.find :as ns-find]
            [clojure.tools.reader :as r]
            [clojure.stacktrace :as stacktrace]
            [medusa.ns-parse :refer [aliases-from-ns-decl]]
            [clojure.java.io :as io])
  (:import (java.io PushbackReader File Reader)))

(def ^:private pushback-buffer-size 10)

(defn find-cljs-sources [paths]
  (->> paths
       (map io/file)
       (map #(ns-find/find-sources-in-dir % ns-find/cljs))
       (filter not-empty)
       (flatten)
       (into [])))

(defn- ^Reader file->reader
  [^File file]
  (-> file
      (clojure.java.io/reader)
      (PushbackReader. pushback-buffer-size)))

(defn construct-context
  [^File file]
  (try
    (with-open [reader (file->reader file)]
      (let [ns-dec  (ns-parse/read-ns-decl reader ns-parse/cljs-read-opts)
            ns-name (second ns-dec)
            aliases (aliases-from-ns-decl ns-dec)]
        {:file    (str file)
         :ns      ns-name
         :aliases aliases}))
    (catch Exception e
      (stacktrace/print-stack-trace e)
      (println (str "Unable to parse " file ": " e)))))

(defn- read-token-or-nil! [reader]
  (try
    (r/read {:eof nil} reader)
    (catch Exception _)))

(defn- read-tokens [reader]
  (lazy-seq (cons (read-token-or-nil! reader)
                  (read-tokens reader))))

(defn- read-file [on-token-fn file]
  (let [ctx (construct-context file)]
    (binding [r/*alias-map* (:aliases ctx)
              *ns*          (:ns ctx)]
      (with-open [reader (file->reader file)]
        (let [tokens (take-while some? (read-tokens reader))]
          (->> tokens
               (map #(on-token-fn % ctx))
               (apply merge-with into {})))))))

(defn loop-files [files on-token-fn]
  (let [read-file (partial read-file on-token-fn)]
    (->> files
         (map read-file)
         (apply merge-with into {}))))
