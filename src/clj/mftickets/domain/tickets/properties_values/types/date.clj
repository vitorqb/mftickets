(ns mftickets.domain.tickets.properties-values.types.date
  (:require [mftickets.db.tickets.properties-values.types.date
             :as
             db.properties-values.types.date]
            [mftickets.domain.tickets.properties-values.create
             :as
             properties-values.create]
            [mftickets.domain.tickets.properties-values.get
             :as
             properties-values.get]))

(def value-type :templates.properties.types/date)

(defmethod properties-values.get/get-type-specific-property-value value-type
  [_ generic-property-value]
  (db.properties-values.types.date/get-date-property-value generic-property-value))

(defmethod properties-values.create/create-type-specific-property-value! value-type
  [_ property-value-data id]
  (db.properties-values.types.date/create-date-property-value property-value-data id))
