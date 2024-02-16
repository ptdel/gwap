(ns gwap.company
  (:require
   [clojure.edn :as edn]
   [fastmath.random :as fr]))

(defn change-share-price [share-price distribution]
  (* share-price (+ 1 (fr/icdf distribution (rand)))))

(defn generate-company-trading-day [company config]
  (let [mean         (-> company :stocks :mean)
        volatility   (-> company :stocks :volatility)
        distribution (fr/distribution :normal {:mu mean :sd volatility})] 
    (loop [trading-intervals (:trading-day-interval config)
           accumulator       [(:share-price company)]] 
      (if (= trading-intervals 0)
        {:ticker (:ticker company) :prices accumulator}
        (recur (dec trading-intervals)
               (conj accumulator (change-share-price (last accumulator) distribution)))))))

(defn generate-market-trading-day [companies config]
  (map #(generate-company-trading-day % config) companies))

(defn market-pulse [company index]
  {:ticker (:ticker company)
   :prices (nth (:prices company) index)})

(defn emit-market-pulses [companies config]
  (loop [pulse 0 accumulator []]
    (if (= pulse (:trading-day-interval config))
      accumulator
      (recur (inc pulse) 
             (conj accumulator (map #(market-pulse % pulse) companies))))))

(def test-config (edn/read-string (slurp "./config.edn")))

(comment
  (def day-of-trades 
    (emit-market-pulses
     (generate-market-trading-day
      (:gwap.system/companies gwap.system/running-simulation)
      test-config)
     test-config)))
