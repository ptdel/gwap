(ns gwap.kafka
  (:require [jackdaw.admin :as ja]
            [jackdaw.serdes :refer [string-serde edn-serde]]))

(defn create-topic-config [name]
  {:topic-name         name
   :partition-count    1
   :replication-factor 1
   :key-serde          (string-serde)
   :value-serde        (edn-serde)})

(defn create-topics [config topics]
  (let [client-config (:client-config config)]
    (with-open [admin (ja/->AdminClient client-config)]
      (ja/create-topics! admin topics))))

(defn delete-topics [config topics]
  (let [client-config (:client-config config)]
    (with-open [admin (ja/->AdminClient client-config)]
      (ja/delete-topics! admin topics))))
