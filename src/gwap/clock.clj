(ns gwap.clock
  (:require [tick.core :as t]
            [tick.alpha.interval :as ti]
            [gwap.util :as u]))

;; the atomic clock will return the current time whenever it is deref'd (`@`)
(def right-now (t/atom))

;; TODO: move the start time into the system
(def start-time (t/now))

(defn elapsed [] (t/between start-time @right-now))

(defn to-interval [duration]
  (let [dur (sort duration)]
    (ti/new-interval (first dur) (last dur))))

(defn to-intervals [durations]
  (map (fn [d] (map #(to-interval %) d)) durations))

(defn derive-day-phases [divisor]
  (into [] (map #(to-interval %)
                (ti/divide-by-divisor (ti/bounds (t/today)) divisor))))

(defn derive-phase-durations [phases duration]
  (map #(ti/divide-by-duration % duration) phases))

;; we use `compare` rather than numerical operators like `<` and `>` to avoid
;; having clojure try to coerce or inputs to numerical types.
(defn between? [t s e]
  (and (= (compare t s) 1) (= (compare t e) -1)))

;; checks whether or not the current timestamp falls within range of the
;; interval for a stage
(defn in-stage? [stage ts]
  (let [current (map #(t/time %) (map stage [:tick/beginning :tick/end]))]
    (between? (t/time ts) (first current) (last current))))

;; iterates over the trading day schedule and returns the current trading
;; phase given the a deref of the clock
(defn get-current-phase [schedule ts]
  (into {} (filter #(in-stage? % ts) schedule)))

(defn create-trading-days [open-percent phases duration]
  (into [] (flatten (map-indexed
    (fn [i x] (u/split-open-close-by-percent x i open-percent))
      (to-intervals (derive-phase-durations (derive-day-phases phases)
                                            (t/of-minutes duration)))))))

(comment
  (def trading-days (create-trading-days 0.75 3 5)))
