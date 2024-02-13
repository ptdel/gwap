(ns gwap.store
  (:require [taoensso.carmine :as car]))

;; TODO: move all these persistent objects into the integrant system.
(defonce conn-pool (car/connection-pool {}))
(def     conn-spec {:uri "redis://localhost:6379/"})
(def     wcar-opts {:pool conn-pool :spec conn-spec})

(defmacro wcar* [& body]
  `(car/wcar wcar-opts ~@body))

(def now #(quot (System/currentTimeMillis) 1000))

(defn get-names [m vs f]
  (reduce #(update-in % [%2] f) m vs))

(defn company->ts [company]
  (let [c (dissoc company :ticker :share-price :market-cap :dividend :stocks :bonds)]
    (get-names c [:sector :subsector :region] #(:name %))))

(defn map->vec [m]
  (mapv (fn [e] (if (keyword? e) (name e) e))
    (mapcat seq m)))

(defn ts-data [timestamp data]
  [(str (:ticker data)) (int timestamp) (double (:prices data))])

(defn ts-create [name labels]
  (car/redis-call
   (vec (concat ["TS.CREATE" (str name) "LABELS"]
                (map->vec labels)))))

(defn ts-delete [time-series]
  (car/del time-series))

(defn ts-add [time datapoint]
  (car/redis-call
   (cons "TS.ADD" (ts-data time datapoint))))

(defn ts-add-many [time datapoints]
  (car/redis-call
   (cons "TS.MADD" (mapcat #(ts-data time %) datapoints))))

(comment
  (wcar*
    (ts-create "QCDW" {:DESC "SHARE_PRICE"
                                :EXCHANGE "GANDYMEDE"})
    (ts-create "BBDD" {:DESC "SHARE_PRICE"
                                :EXCHANGE "GANDYMEDE"})
    (ts-add (now) {:ticker "QCDW" :price 12.22})
    (ts-add (now) {:ticker "BBDD" :price 23.11})
    (ts-add-many (+ 1 (now)) [{:ticker "QCDW" :prices 12.34}
                                   {:ticker "BBDD" :prices 23.56}])))
