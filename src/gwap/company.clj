(ns gwap.company
  (:require
   [fastmath.random :as fr]
   [gwap.clock :as gc]))

(defn update-share-price [share-price distribution]
  (* share-price (+ 1 (fr/icdf distribution (rand)))))

(defn update-company [company]
  (let [mean (-> company :stocks :mean)
        volatility (-> company :stocks :volatility)
        distribution (fr/distribution :normal {:mu mean :sd volatility})]
    (update company :share-price #(update-share-price % distribution))))

(defn update-market [companies]
  (into [] (pmap #(update-company %) companies)))

(defn market-cycle [companies schedule ts]
  (let [market-open? (:open (gc/get-current-phase schedule ts))]
    (if-not market-open?
      companies
      (update-market companies))))

(comment market-cycle)
