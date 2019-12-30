(ns mftickets.domain.tickets.properties-values.types.text
  (:require [mftickets.db.core :as db.core]
            [mftickets.db.tickets.properties-values :as db.properties-values]
            [mftickets.db.tickets.properties-values.types.text
             :as
             db.properties-values.types.text]
            [mftickets.domain.tickets.properties-values.create
             :as
             properties-values.create]
            [mftickets.domain.tickets.properties-values.create.inject
             :as
             properties-values.create.inject]
            [mftickets.domain.tickets.properties-values.get
             :as
             properties-values.get]))

(def value-type :templates.properties.types/text)

(defmethod properties-values.get/get-type-specific-property-value value-type
  [_ generic-property-value]
  (db.properties-values.types.text/get-text-property-value generic-property-value))

(defmethod properties-values.create/create-type-specific-property-value! value-type
  [_ property-value-data id]
  (db.properties-values.types.text/create-text-property-value! property-value-data id))
