(ns gwap.game
  (:require
   [tick.core :as t]
   [gwap.clock :as gc]
   [gwap.company :as c]
   [overtone.at-at :as at]))

(def persisted-companies (atom nil))

(defn gameloop! [config pool schedule companies]
  (let [interval (t/millis (t/of-minutes (:trading-day-slices config)))]
    (reset! persisted-companies companies)
    (at/every interval
              #(reset! persisted-companies
                      (c/market-cycle @persisted-companies schedule @gc/right-now)) pool)))
