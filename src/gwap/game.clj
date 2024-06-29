(ns gwap.game
  (:require
   [gwap.clock :as gc]
   [gwap.company :as c]
   [overtone.at-at :as at]))

(def persisted-companies (atom nil))

(defn gameloop! [pool schedule companies]
  (let [interval (gc/get-period-duration schedule)]
    (reset! persisted-companies companies)
    (at/every interval
              #(reset! persisted-companies
                      (c/market-cycle @persisted-companies schedule @gc/right-now)) pool)))
