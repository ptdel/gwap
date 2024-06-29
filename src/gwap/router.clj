(ns gwap.router
  (:require
   [aleph.http :as http]
   [manifold.stream :as stream]
   [manifold.deferred :as defer]
   [muuntaja.core :as m]
   [reitit.dev.pretty :as pretty]
   [reitit.ring :as ring]
   [reitit.ring.coercion :as rrc]
   [reitit.middleware :as middleware]
   [reitit.ring.middleware.exception :as exception]
   [reitit.ring.middleware.parameters :as parameters]
   [reitit.ring.middleware.muuntaja :as muuntaja]
   [reitit.coercion.malli]
   [gwap.util :as u]
   [gwap.game :as gg]))

(def invalid-request
  {:status 400
   :headers {"content-type" "application/text"}
   :body "Invalid Connection"})

(def headers
  {:headers {"Sec-Websocket-Protocol" "demo-chat"}})

(defn market-handler [req]
  (defer/let-flow [socket (http/websocket-connection req headers)
                   tickers (-> req :parameters :query :ticker)
                   period (:period req)]
    (if-not socket invalid-request
            (stream/connect
             (stream/periodically period
               #(->> @gg/persisted-companies
                     (u/filter-tickers tickers)
                     (into [])
                     (m/encode "application/json")
                     (slurp)))
             socket))))

(defn wrap [handler dep]
  (fn [request] (handler (merge request dep))))

(defn game-endpoints [config]
  (ring/ring-handler
   (ring/router
    ["/api"
     ["/market" {:name       ::market-handler
                 :parameters {:query {:ticker [:or [:vector string?] string?]}}
                 :middleware [:wrap]
                 :get        market-handler}]]
    {:exception            pretty/exception
     ::middleware/registry {:wrap [wrap config]}
     :data                 {:coercion   reitit.coercion.malli/coercion
                            :muuntaja   m/instance
                            :middleware [muuntaja/format-middleware
                                         parameters/parameters-middleware
                                         exception/exception-middleware
                                         rrc/coerce-request-middleware]}})
   (ring/create-default-handler)))
