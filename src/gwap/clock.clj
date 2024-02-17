(ns gwap.clock
  (:require [tick.core :as t]))

(def tick-rate 1000)

(defn time-delta [a b]
  (t/millis (t/between a b)))

(defn game-loop [start]
  (loop [current (t/now)
         elapsed current
         lag (time-delta start current)]
    (if (>= lag tick-rate)
      (println "took this long to fuck up: " elapsed)
      (do (Thread/sleep (int (* (rand) 1000))) 
          (recur (t/now) (time-delta start current) (time-delta current (t/now)))))))

(comment  (game-loop (t/now)))
