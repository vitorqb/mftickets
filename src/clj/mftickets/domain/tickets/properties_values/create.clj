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

;; !!!! TODO -> Pass property as param, not getter.
(defn create-property-value!
  [{::properties-values.create.inject/keys [get-property] :as inject} property-value-data]
  (let [property (-> property-value-data :property-id get-property)
        value-type (:value-type property)]
    (db.core/run-effects!
     [db.properties-values/create-generic-property-value! property-value-data]
     [create-type-specific-property-value! value-type property-value-data ::db.core/<])
    (let [ticket {:id (:ticket-id property-value-data)}
          get-opts {:ticket ticket :property property}]
      (properties-values.get/get-property-value get-opts))))
