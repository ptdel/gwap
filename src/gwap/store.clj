(ns gwap.store
  (:require [taoensso.carmine :as car]))

;; TODO: move all these persistent objects into the integrant system.
(defonce conn-pool (car/connection-pool {}))
(def     conn-spec {:uri "redis://localhost:6379/"})
(def     wcar-opts {:pool conn-pool :spec conn-spec})

(defmacro wcar* [& body]
  `(car/wcar wcar-opts ~@body))

(defn now [] (long (/ (System/currentTimeMillis) 1000)))

(defn get-names [m vs f]
  (reduce #(update-in % [%2] f) m vs))

(defn company->ts [company]
  (let [c (dissoc company :ticker :share-price :market-cap :dividend :stocks :bonds)]
    (get-names c [:sector :subsector :region] #(:name %))))

(defn map->vec [m]
  (mapv (fn [e] (if (keyword? e) (name e) e))
    (mapcat seq m)))

(defn ts-data [ts data]
  [(str (:ticker data) ":INTRA") ts (double (:prices data))])

(defn ts-create [name labels]
  (car/redis-call
   (vec (concat ["TS.CREATE" (str name)
                 "LABELS"] (map->vec labels)))))

(defn ts-create-rule [source destination aggregation duration]
  (car/redis-call
   (cons "TS.CREATERULE" [source destination
                          "AGGREGATION" aggregation
                          duration])))

(defn ts-delete [time-series]
  (car/del time-series))

(defn ts-add [ts datapoint]
  (car/redis-call
   (cons "TS.ADD" (ts-data ts datapoint))))

(defn ts-add-many [ts datapoints]
  (car/redis-call
   (cons "TS.MADD" (mapcat #(ts-data ts %) datapoints))))

(defn ts-create-company [company aggregation duration]
  (let [intraday (str (:ticker company) ":INTRA")
        closing  (str (:ticker company) ":CLOSE")]
    (ts-create intraday (company->ts company))
    (ts-create closing (company->ts company))
    (ts-create-rule intraday closing aggregation duration)))

(comment
  (dotimes [day 365] (wcar* (doall (map #(ts-add-many (inc (- 365 day)) %) gwap.company/day-of-trades)))))
