(ns gwap.primordial)

(defn derive-period-mean [daily-mean open-periods]
  (- (Math/pow (+ daily-mean 1) (/ 1 open-periods)) 1))

(defn derive-period-volatility [daily-volatility open-periods]
  (/ daily-volatility (Math/sqrt open-periods)))

(defn random-ticker []
  (apply str (repeatedly (rand-nth [3 4]) #(rand-nth "ABCDEFGHIJKLMNOPQRSTUVWXYZ"))))

(defn random-sector [sector-list]
  (rand-nth sector-list))

(defn random-subsector [subsector-list]
  (rand-nth subsector-list))

(defn random-region [region-list]
  (rand-nth region-list))

(defn random-total-shares []
  (int (+ 1000000 (rand (- 5000000 1000000)))))

(defn random-share-price []
  (+ 30 (rand (- 40 30))))

(defn random-dividend []
  (let [dividend (rand 2)]
    (if (< dividend 0.5) 0 dividend)))

(defn seasoning 
  "just a lil' seasonin'"
  []
  (+ 0.9 (rand (- 1.1 0.9))))

(defn derive-security-modifier [sector subsector region]
  (let [sector-modifier (:modifier sector)
        subsector-modifier (:modifier subsector)
        region-modifier (:modifier region)]
    (+ sector-modifier subsector-modifier region-modifier)))

(defn derive-constant [derived-period-constant security-modifier probability]
  (* derived-period-constant (+ 1 (* 0.35 security-modifier)) probability))

(defn derive-market-cap [total-shares share-price]
  (* total-shares share-price))

(defn create-company [primordial-soup]
  (let [sector            (random-sector (:sectors primordial-soup))
        subsector         (random-subsector (:subsectors sector))
        region            (random-region (:regions primordial-soup))
        share-price       (random-share-price)
        total-shares      (random-total-shares)
        market-cap        (derive-market-cap total-shares share-price)
        dividend          (random-dividend)
        security-modifier (derive-security-modifier sector subsector region)
        open-periods      (-> primordial-soup :total-trading-day-periods)
        stock-mean        (-> primordial-soup :stocks :mean (derive-period-mean open-periods))
        stock-volatility  (-> primordial-soup :stocks :volatility (derive-period-volatility open-periods))
        probability       (seasoning)]
    {:ticker       (random-ticker)
     :sector       (select-keys sector [:name :modifier])
     :subsector    subsector
     :region       region
     :share-price  share-price
     :total-shares total-shares
     :market-cap   market-cap
     :dividend     dividend
     :stocks       {:mean       (derive-constant stock-mean security-modifier probability)
                    :volatility (derive-constant stock-volatility security-modifier probability)}}))

(comment create-company)
