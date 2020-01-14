(ns mftickets.domain.templates.properties.types.radio
  (:require [mftickets.db.templates.properties :as db.templates.properties]
            [mftickets.domain.templates.properties.create :as properties.create]
            [mftickets.domain.templates.properties.get :as properties.get]
            [mftickets.domain.templates.properties.update :as properties.update]))

(def ^:private value-type :templates.properties.types/radio)

(defmethod properties.create/create-type-specific-property! value-type
  [{options :templates.properties.types.radio/options} created-generic-property]

  (let [property-id (:id created-generic-property)
        options* (map #(assoc % :property-id property-id) options)
        _ (db.templates.properties/create-radio-options! options*)
        created-options (db.templates.properties/get-radio-options created-generic-property)]
    {:templates.properties.types.radio/options created-options}))

(defmethod properties.get/get-type-specific-property value-type
  [property]
  {:templates.properties.types.radio/options (db.templates.properties/get-radio-options property)})

(defmethod properties.update/update-property-type-data! value-type
  [property]
  (db.templates.properties/update-radio-options! property))
