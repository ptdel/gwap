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
  (into [] (map (fn [d] (map #(to-interval %) d)) durations)))

(defn derive-in-game-days [divisor]
  (into [] (map #(to-interval %)
                (ti/divide-by-divisor (ti/bounds (t/today)) divisor))))

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
 
(defn divide-in-game-days [days period]
  (into [] (map #(ti/divide-by-divisor % period) days)))

(defn create-trading-days [days total-periods open-periods]
  {:pre [(< open-periods total-periods)]}
  (let [in-game-days         (derive-in-game-days days)
        divided-in-game-days (divide-in-game-days in-game-days total-periods)]
   (into []
         (flatten
          (map-indexed
           (fn [day value] (u/split-open-close value day open-periods))
           (to-intervals divided-in-game-days))))))

(defn get-period-duration [schedule]
  (t/millis (t/duration (first schedule))))

(comment
  (def days (create-trading-days 4 20 16)))
