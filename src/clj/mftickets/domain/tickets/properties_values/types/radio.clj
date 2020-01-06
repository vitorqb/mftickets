(ns mftickets.domain.tickets.properties-values.types.radio
  (:require [mftickets.domain.tickets.properties-values.create :as properties-values.create]
            [mftickets.domain.tickets.properties-values.get :as properties-values.get]))

(def value-type :templates.properties.types/radio)

;; !!!! TODO
(defmethod properties-values.get/get-type-specific-property-value value-type
  [opts generic-property-value]
  (merge
   generic-property-value
   {:templates.properties.types.radio/chosen-opt-id 1}))

;; !!!! TODO
(defmethod properties-values.create/create-type-specific-property-value! value-type
  [_ property-value-data id]
  {:id id
   :property-id (:id property-value-data)
   :templates.properties.types.radio/chosen-opt-id 1})
