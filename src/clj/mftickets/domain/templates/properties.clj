(ns mftickets.domain.templates.properties
  (:require [com.rpl.specter :as s]
            [mftickets.db.templates.properties :as db.templates.properties]
            [mftickets.domain.templates.properties.get :as properties.get]
            mftickets.domain.templates.properties.types.radio
            [mftickets.domain.templates.properties.update :as update]))

(defn get-property-types
  "Returns a set of all template property types."
  []
  ;; Eventually there may be registration logic here, but for now this is enough!
  #{:templates.properties.types/radio
    :templates.properties.types/text
    :templates.properties.types/date})

(defn get-properties-ids-set-for-template-id [template-id]
  (or (some->> {:id template-id} properties.get/get-properties-for-template (map :id) (into #{}))
      #{}))

(defn delete-property!
  "Deletes a property."
  [property]
  (db.templates.properties/delete-property! property))
