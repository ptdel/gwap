(ns gwap.util)

(defn distinct-by [f coll]
  (let [groups (group-by f coll)]
    (map #(first (groups %)) (distinct (map f coll)))))

(defn split-open-close [coll day open]
    [(map #(assoc % :day day :open true) (take open coll))
     (map #(assoc % :day day :open false) (drop open coll))])

(defn filter-tickers [tickers companies]
  (cond (string? tickers)
        (filter (fn [v] (= (:ticker v) tickers)) companies)
        (coll? tickers)
        (let [t (set tickers)]
          (filter (fn [v] (t (:ticker v))) companies))))
