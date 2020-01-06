(ns mftickets.domain.tickets
  (:require [clojure.spec.alpha :as spec]
            [com.rpl.specter :as s]
            [mftickets.db.core :as db.core]
            [mftickets.db.tickets :as db.tickets]
            [mftickets.domain.tickets.inject :as inject]
            [mftickets.domain.tickets.properties-values :as properties-values]
            [mftickets.domain.tickets.properties-values.create
             :as
             properties-values.create]))

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
  "Creates all `properties-values` for a given `raw-ticket`."
  [{::inject/keys [get-properties-for-ticket] :as inject} properties-values raw-ticket]

  {:pre [(ifn? get-properties-for-ticket)]}
  
  (let [properties (get-properties-for-ticket raw-ticket)
        get-property #(s/select-one [(s/filterer :id (s/pred= (:property-id %))) s/ALL] properties)
        get-opts #(do {:ticket raw-ticket :property (get-property %)})]
    (->> properties-values
         (map #(assoc % :ticket-id (:id raw-ticket)))
         (map #(vector properties-values.create/create-property-value! % (get-opts %)))
         (apply db.core/run-effects!)))
  (get-ticket inject (:id raw-ticket)))

(defn create-ticket!
  "Creates a new ticket and stores in the db."
  [inject ticket-data]
  (db.core/run-effects!
   [db.tickets/create-raw-ticket! ticket-data]
   [create-ticket-properties-values! inject (:properties-values ticket-data) ::db.core/<]))
