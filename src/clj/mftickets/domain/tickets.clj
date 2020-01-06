(ns mftickets.domain.tickets
  (:require [clojure.spec.alpha :as spec]
            [com.rpl.specter :as s]
            [mftickets.db.core :as db.core]
            [mftickets.db.tickets :as db.tickets]
            [mftickets.domain.tickets.properties-values :as properties-values]
            [mftickets.domain.tickets.properties-values.create
             :as
             properties-values.create]))

(defn get-ticket
  "Get's a full ticket. `properties` must contain all properties for that ticket."
  [id {:keys [properties]}]
  (let [raw-ticket (db.tickets/get-raw-ticket id)
        opts {:properties properties :ticket raw-ticket}
        properties-values (properties-values/get-properties-values opts)]
    (assoc raw-ticket :properties-values properties-values)))

(defn- create-ticket-properties-values!
  "Creates all `properties-values` for a given `raw-ticket`.
  `properties` must contain all properties for the tickets' template."
  [properties-values raw-ticket {:keys [properties] :as opts}]
  (let [get-property #(s/select-one [(s/filterer :id (s/pred= (:property-id %))) s/ALL] properties)
        get-opts #(do {:ticket raw-ticket :property (get-property %)})]
    (->> properties-values
         (map #(assoc % :ticket-id (:id raw-ticket)))
         (map #(vector properties-values.create/create-property-value! % (get-opts %)))
         (apply db.core/run-effects!)))
  (get-ticket (:id raw-ticket) opts))

(defn create-ticket!
  "Creates a new ticket and stores in the db."
  [ticket-data opts]
  (db.core/run-effects!
   [db.tickets/create-raw-ticket! ticket-data]
   [create-ticket-properties-values! (:properties-values ticket-data) ::db.core/< opts]))
