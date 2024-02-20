(ns gwap.clock
  (:require [tick.core :as t]))

;; the atomic clock will return the current time whenever it is deref'd (`@`)
(def clock (t/atom))

;; TODO: move the start time into the system
(def start-time (t/now))

(defn elapsed [] (t/between start-time @clock))

;; We can create some sort of formulaic means of deriving a trading day given
;; some set of inputs if we want to scale the day "up or down" for now this
;; will have to work.
(def trading-day-schedule [{:phase          :alpha
                            :stage          :open
                            :tick/beginning (t/midnight)
                            :tick/end       (t/time "05:12")}
                           {:phase          :alpha
                            :stage          :closed
                            :tick/beginning (t/time "05:12")
                            :tick/end       (t/time "06:00")}
                           {:phase          :beta
                            :stage          :open
                            :tick/beginning (t/time "06:00")
                            :tick/end       (t/time "11:12")}
                           {:phase          :beta
                            :stage          :closed
                            :tick/beginning (t/time "11:12")
                            :tick/end       (t/noon)}
                           {:phase          :gamma
                            :stage          :open
                            :tick/beginning (t/noon)
                            :tick/end       (t/time "17:12")}
                           {:phase          :gamma
                            :stage          :closed
                            :tick/beginning (t/time "17:12")
                            :tick/end       (t/time "18:00")}
                           {:phase          :delta
                            :stage          :open
                            :tick/beginning (t/time "18:00")
                            :tick/end       (t/time "23:12")}
                           {:phase          :delta
                            :stage          :closed
                            :tick/beginning (t/time "23:12")
                            :tick/end       (t/midnight)}])

;; we use `compare` rather than numerical operators like `<` and `>` to avoid
;; having clojure try to coerce or inputs to numerical types.
(defn between? [t s e]
  (and (= (compare t s) 1) (= (compare t e) -1)))

;; checks whether or not the current timestamp falls within range of the
;; interval for a stage
(defn in-stage? [ts stage]
  (let [current (map stage [:tick/beginning :tick/end])]
    (between? (t/time ts) (first current) (last current))))

;; iterates over the trading day schedule and returns the current trading
;; phase given the a deref of the clock
(defn get-current-phase [ts schedule]
  (into {} (filter #(in-stage? ts %) schedule)))

(comment
  elapsed
  (get-current-phase @clock trading-day-schedule))
