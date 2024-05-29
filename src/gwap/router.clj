(ns gwap.router
  (:require
   [aleph.http :as http]
   [manifold.stream :as stream]
   [manifold.deferred :as defer]
   [muuntaja.core :as m]
   [reitit.dev.pretty :as pretty]
   [reitit.ring :as ring]
   [reitit.middleware :as middleware]
   [gwap.game :as gg]))

(def invalid-request
  {:status 400
   :headers {"content-type" "application/text"}
   :body "Invalid Connection"})

(defn market-handler [req]
  (defer/let-flow [socket (http/websocket-connection req {:headers {"Sec-Websocket-Protocol" "demo-chat"}})
                   ticker (-> req :path-params :ticker)]
    (if-not socket invalid-request
            (stream/connect
             (stream/periodically 10000
               #(->> @gg/persisted-companies
                     (filter (fn [e] (= (:ticker e) ticker)))
                     (into {})
                     (m/encode "application/json")
                     (slurp))) socket))))

(defn wrap [handler dep]
  (fn [request] (handler (merge request dep))))

(defn game-endpoints [config]
  (ring/ring-handler
   (ring/router
    ["/api"
     ["/market/:ticker" {:name       ::market-handler
                         :parameters {:path {:topic string?}}
                         :middleware [:wrap]
                         :get        market-handler}]]
    {:exception            pretty/exception
     ::middleware/registry {:wrap [wrap config]}})
   (ring/create-default-handler)))
