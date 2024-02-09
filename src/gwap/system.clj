(ns gwap.system
  (:gen-class)
  (:require [integrant.core :as ig]
            [aleph.http :as http]
            [gwap.primordial :as primordial]))

(defmethod ig/init-key ::config
  [_ {}]
  (ig/read-string (slurp "./server.edn")))

(defmethod ig/init-key ::primordial
  [_ {}]
  (ig/read-string (slurp "./config.edn")))

(defmethod ig/init-key ::companies
  [_ {:keys [soup]}]
  (let [number-of-companies (:number-of-companies soup)]
    (into []
          (repeatedly number-of-companies
                      #(primordial/create-company soup)))))

(defmethod ig/init-key ::server
  [_ {:keys [app config]}]
  (let [port (:port config)]
    (http/start-server app {:port port})))

(defmethod ig/halt-key! ::server
  [_ server]
  (.close server))

(def universe {::config     {}
               ::primordial {}
               ::companies  {:soup (ig/ref ::primordial)}
               ::app        {}
               ::server     {:app    (ig/ref ::app)
                             :config (ig/ref ::config)}})

(comment
  (def running-simulation (ig/init universe))
  (ig/halt! running-simulation))
