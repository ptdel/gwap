(ns gwap.util)

(defn distinct-by [f coll]
  (let [groups (group-by f coll)]
    (map #(first (groups %)) (distinct (map f coll)))))

(defn split-open-close-by-percent [coll index percent]
  (let [c (count coll)
        t (* c percent)]
    [(map #(assoc % :index index :open true) (take t coll))
     (map #(assoc % :index index :open false) (drop t coll))]))
