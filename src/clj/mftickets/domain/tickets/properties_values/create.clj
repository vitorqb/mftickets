(ns mftickets.domain.tickets.properties-values.create
  (:require [clojure.spec.alpha :as spec]
            [mftickets.db.core :as db.core]
            [mftickets.db.tickets.properties-values :as db.properties-values]
            [mftickets.domain.tickets.properties-values.create.inject
             :as
             properties-values.create.inject]
            [mftickets.domain.tickets.properties-values.get :as properties-values.get]))

(defmulti create-type-specific-property-value!
  "Runs creation effects for a property value related to it's specific type.
  This is called after handling the creation of the generic property value.
  Dispatches on properties :value-type."
  (fn [value-type property-value-data id] value-type))

(defn create-property-value!
  [property-value-data {:keys [property ticket] :as opts}]

  {:pre [(= (:id property) (:property-id property-value-data))
         (= (:id ticket) (:ticket-id property-value-data))]}
  
  (db.core/run-effects!
   [db.properties-values/create-generic-property-value! property-value-data]
   [create-type-specific-property-value! (:value-type property) property-value-data ::db.core/<])

  (properties-values.get/get-property-value opts))
