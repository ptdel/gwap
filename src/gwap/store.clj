(ns gwap.store
  (:require [clojure.java.io :as io])
  (:import [java.io PushbackReader]))

(def db (sorted-map-by >))

;; timestamp [{:ticker :price}, ..., N]
(def data (atom {}))

(defn with [db data] (merge db data))

(defn before [db ts] (into {} (subseq db > ts)))

(defn after [db ts] (into {} (subseq db < ts)))

;; big-write! and big-read provide a bit more stability for reading and writing
;; large data structures to and from files that spit and slurp lack.

(defn big-write! [filename data]
  (with-open [w (io/writer filename)]
    (binding [*out* w]
      (pr data))))

(defn big-read [filename]
  (with-open [r (PushbackReader. (io/reader filename))]
    (binding [*read-eval* false]
      (read r))))
