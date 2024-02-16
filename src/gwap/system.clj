(ns gwap.system
  (:gen-class)
  (:require [integrant.core :as ig]
            [gwap.primordial :as primordial]
            [gwap.store :as store]
            [gwap.util :as util]))

(defmethod ig/init-key ::config
  [_ {}]
  (ig/read-string (slurp "./server.edn")))

(defmethod ig/init-key ::primordial
  [_ {}]
  (ig/read-string (slurp "./config.edn")))

(defmethod ig/init-key ::companies
  [_ {:keys [soup]}]
  (util/distinct-by :ticker
    (loop [companies []]
      (if (= (count (util/distinct-by :ticker companies)) (:number-of-companies soup))
        companies
        (recur (conj companies (primordial/create-company soup)))))))

(defmethod ig/init-key ::time-series
  [_ {:keys [companies]}]
  (store/wcar*
    (doall (map
            #(store/ts-create-company % "last" 60000)
            companies)))
  (map #(:ticker %) companies))

(defmethod ig/halt-key! ::time-series
  [_ companies]
  (store/wcar* (doall (map #(store/ts-delete %) companies))))

(def universe {::config     {}
               ::primordial {}      
               ::companies  {:soup (ig/ref ::primordial)}
               ::time-series {:companies (ig/ref ::companies)}})

(comment
  (def running-simulation (ig/init universe))
  (ig/halt! running-simulation))
