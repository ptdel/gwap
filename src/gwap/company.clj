(ns gwap.company
  (:require
   [fastmath.random :as fr]
   [gwap.clock :as gc]))

(defn update-share-price [share-price distribution]
  (* share-price (+ 1 (fr/icdf distribution (rand)))))

;; TODO: write a function that takes in a vector of modifiers, and returns a
;; partial function that applies all of the modifiers associatively to input

;; TODO: update-company needs to take in a vector of modifiers that are applied
;; to the mean and volatility of the company before a new distribution is made.
(defn update-company [company]
  (let [mean (-> company :stocks :mean)
        volatility (-> company :stocks :volatility)
        distribution (fr/distribution :normal {:mu mean :sd volatility})]
    (update company :share-price #(update-share-price % distribution))))

;; TODO: update market needs to accept a vector of modifiers that are passed
;; through to update-company.
(defn update-market [companies]
  (into [] (pmap #(update-company %) companies)))

;; TODO: market-cycle needs to accept a vector of modifiers that are passed
;; through into update-company.
(defn market-cycle [companies schedule ts]
  (let [market-open? (:open (gc/get-current-phase schedule ts))]
    (if-not market-open?
      companies
      (update-market companies))))

(comment market-cycle)
