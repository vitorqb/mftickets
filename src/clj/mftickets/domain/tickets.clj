(ns mftickets.domain.tickets
  (:require [clojure.spec.alpha :as spec]
            [mftickets.db.core :as db.core]
            [mftickets.db.tickets :as db.tickets]
            [mftickets.domain.tickets.inject :as inject]
            [mftickets.domain.tickets.properties-values :as properties-values]))

(defn get-ticket
  "Get's a full ticket!"
  [{::inject/keys [get-properties-for-ticket] :as inject} id]

  {:pre [(spec/assert ::inject/get-properties-for-ticket get-properties-for-ticket)]}

  (let [raw-ticket (db.tickets/get-raw-ticket id)
        properties (get-properties-for-ticket raw-ticket)
        opts {:properties properties :ticket raw-ticket}
        properties-values (properties-values/get-properties-values opts)]
    (assoc raw-ticket :properties-values properties-values)))

(defn- create-ticket-properties-values!
  [{::inject/keys [create-property-value!] :as inject} properties-values ticket-data]

  {:pre [(spec/assert ::inject/create-property-value! create-property-value!)]}

  (->> properties-values
       (map #(assoc % :ticket-id (:id ticket-data)))
       (map #(vector create-property-value! inject %))
       (apply db.core/run-effects!))
  
  (get-ticket inject (:id ticket-data)))

(defn create-ticket!
  "Creates a new ticket and stores in the db."
  [inject ticket-data]
  (db.core/run-effects!
   [db.tickets/create-raw-ticket! ticket-data]
   [create-ticket-properties-values! inject (:properties-values ticket-data) ::db.core/<]))
