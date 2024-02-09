(ns gwap.primordial)

(defn random-ticker []
  (apply str (repeatedly 4 #(rand-nth "ABCDEFGHIJKLMNOPQRSTUVWXYZ"))))

(defn random-sector [sector-list]
  (rand-nth sector-list))

(defn random-subsector [subsector-list]
  (rand-nth subsector-list))

(defn random-region [region-list]
  (rand-nth region-list))

(defn random-market-cap []
  (* 1e9 (+ 1 (rand-int (- 250 1)))))
;; 0.9 1.1
(defn random-share-price []
  (+ 25 (rand (- 50 25))))

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

(defn derive-constant [universal-constant security-modifier probability]
  (* universal-constant (+ 1 (* 0.35 security-modifier)) probability))

(defn derive-total-shares [share-price market-cap]
  (/ market-cap share-price))

(defn derive-real-market-cap [share-price market-cap]
  (let [total-shares (derive-total-shares share-price market-cap)]
    (* total-shares share-price)))

(defn create-company [primordial-soup]
  (let [sector            (random-sector (:sectors primordial-soup))
        subsector         (random-subsector (:subsectors sector))
        region            (random-region (:regions primordial-soup))
        share-price       (random-share-price)
        market-cap        (derive-real-market-cap share-price (random-market-cap))
        dividend          (random-dividend)
        security-modifier (derive-security-modifier sector subsector region)
        stock-mean        (-> primordial-soup :stocks :mean)
        stock-volatility  (-> primordial-soup :stocks :volatility)
        bonds-mean        (-> primordial-soup :bonds :mean)
        bonds-volatilty   (-> primordial-soup :bonds :volatility)
        probability (seasoning)]
    {:ticker      (random-ticker)
     :sector      (select-keys sector [:name :modifier])
     :subsector   subsector
     :region      region
     :share-price share-price
     :market-cap  market-cap
     :dividend    dividend
     :stocks      {:mean       (derive-constant stock-mean security-modifier probability)
                   :volatility (derive-constant stock-volatility security-modifier probability)}
     :bonds       {:mean       (derive-constant bonds-mean security-modifier probability)
                   :volatility (derive-constant bonds-volatilty security-modifier probability)}}))

(comment create-company)
