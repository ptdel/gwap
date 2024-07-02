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
   [com.brunobonacci.mulog :as log]
   [gwap.util :as u]
   [gwap.game :as gg]))

(def invalid-request
  {:status 400
   :headers {"content-type" "application/text"}
   :body "Invalid Connection"})

(def headers
  {:headers {"Sec-Websocket-Protocol" "gwap"}})

(defn market-handler [req]
  (defer/let-flow [socket (http/websocket-connection req)
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

(defn ping-handler [req]
  (log/log ::new-connection :data req)
  (-> (defer/let-flow [socket (http/websocket-connection req)]
        (stream/connect (stream/periodically 1000 #(str "hello")) socket))
      (defer/catch (fn [_] invalid-request))))

(defn wrap [handler dep]
  (fn [request] (handler (merge request dep))))

(defn game-endpoints [config]
  (ring/ring-handler
   (ring/router
    ["/api"
     ["/ping" {:name ::ping
               :get  ping-handler}]
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
