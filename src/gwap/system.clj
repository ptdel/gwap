(ns gwap.system
  (:gen-class)
  (:require
   [aleph.http :as http]
   [manifold.bus :as bus]
   [integrant.core :as ig]
   [overtone.at-at :as at]
   [gwap.primordial :as primordial]
   [gwap.util :as util]
   [gwap.clock :as clock]
   [gwap.game :as game]
   [gwap.router :as router]))


(defmethod ig/init-key ::config
  [_ {}]
  (ig/read-string (slurp "./server.edn")))

(defmethod ig/init-key ::primordial
  [_ {}]
  (ig/read-string (slurp "./config.edn")))

(defmethod ig/init-key ::bus
  [_ {}]
  (bus/event-bus))

(defmethod ig/init-key ::schedule-pool
  [_ {}]
  (at/mk-pool))

(defmethod ig/halt-key! ::schedule-pool
  [_ pool]
  (at/stop-and-reset-pool! pool :strategy :kill))

(defmethod ig/init-key ::diurnal-cycle
  [_ {:keys [soup]}]
  (let [trading-days (:trading-days soup)
        open-percent (:trading-day-open-percent soup)
        trading-day-slices (:trading-day-slices soup)]
    (clock/create-trading-days
     open-percent
     trading-days
     trading-day-slices)))

(defmethod ig/init-key ::companies
  [_ {:keys [soup]}]
  (util/distinct-by :ticker
    (loop [companies []]
      (if (= (count (util/distinct-by :ticker companies)) (:number-of-companies soup))
        companies
        (recur (conj companies (primordial/create-company soup)))))))

(defmethod ig/init-key ::endpoints
  [_ {:keys [config]}]
  (router/game-endpoints config))

(defmethod ig/init-key ::server
  [_ {:keys [endpoints config]}]
  (let [port (:port config)]
    (http/start-server endpoints {:port port})))

(defmethod ig/halt-key! ::server
  [_ server]
  (.close server))

(defmethod ig/init-key ::market-cycle
  [_ {:keys [soup pool schedule companies]}]
  (game/gameloop! soup pool schedule companies))

(def universe {::config        {}
               ::primordial    {}
               ::bus           {}
               ::schedule-pool {:soup (ig/ref ::primordial)}
               ::diurnal-cycle {:soup (ig/ref ::primordial)}
               ::companies     {:soup (ig/ref ::primordial)}
               ::endpoints     {:config {:bus   (ig/ref ::bus)
                                         :topic (:topic-name (ig/ref ::primordial))}}
               ::server        {:endpoints (ig/ref ::endpoints)
                                :config    (ig/ref ::config)}
               ::market-cycle  {:soup      (ig/ref ::primordial)
                                :bus       (ig/ref ::bus)
                                :pool      (ig/ref ::schedule-pool)
                                :schedule  (ig/ref ::diurnal-cycle)
                                :companies (ig/ref ::companies)}})

(comment
  (def running-simulation (ig/init universe))
  (ig/halt! running-simulation))
 
