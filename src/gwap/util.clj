(ns gwap.util)

(defn distinct-by [f coll]
  (let [groups (group-by f coll)]
    (map #(first (groups %)) (distinct (map f coll)))))
