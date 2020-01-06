(ns mftickets.db.tickets.properties-values
  (:require [mftickets.db.core :as db.core]
            [conman.core :as conman]
            clojure.set))

(conman/bind-connection db.core/*db* "sql/queries/properties-values.sql")

(defn create-generic-property-value!
  "Creates a new entry on the table for property values that contains common
  values for all property. Is unaware of any type-specific property value."
  [data]
  (-> data create-generic-property-value!* db.core/get-id-from-insert))

(defn get-generic-property-value
  "Gets the generic part of a property value, without any type specific info."
  [{:keys [property ticket]}]
  (some-> {:property-id (:id property) :ticket-id (:id ticket)}
          get-generic-property-value*
          (clojure.set/rename-keys {:propertyid :property-id :ticketid :ticket-id})))
