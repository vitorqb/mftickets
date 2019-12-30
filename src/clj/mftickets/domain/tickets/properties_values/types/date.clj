(ns mftickets.domain.tickets.properties-values.types.date
  (:require [mftickets.domain.tickets.properties-values.create :as properties-values.create]
            [mftickets.domain.tickets.properties-values.get :as properties-values.get]))

(def value-type :templates.properties.types/date)

;; !!!! TODO
(defmethod properties-values.get/get-type-specific-property-value value-type
  [opts generic-property-value]
  {:templates.properties.types.date/value "2019-01-0100:00:00"})

;; !!!! TODO
(defmethod properties-values.create/create-type-specific-property-value! value-type
  [_ property-value-data id]
  {:id id
   :property-id (:id property-value-data)
   :templates.properties.types.date/value "2019-01-0100:00:01"})
