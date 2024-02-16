(ns gwap.clock
  (:require [cljc.java-time.instant :as i]))

(def quit (atom false))

(defn frametime [newtime current]
  (if (> (- newtime current) 0.25) 0.25 (- newtime current)))

(while (false? quit)
  (let [t 0.0
        dt 0.01
        current (System/currentTimeMillis)
        newtime (System/currentTimeMillis)
        frametime (frametime newtime current)
        accumulator frametime]))
