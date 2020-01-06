(ns mftickets.domain.templates.properties.update
  (:require [mftickets.db.templates.properties :as db.properties]))

(defmulti update-property-type-data!
  "Updates the type-specific data for a property."
  :value-type)
(defmethod update-property-type-data! :default [_] {})

(defn update-property! [property]
  (db.properties/update-property-generic-data! property)
  (update-property-type-data! property)
  nil)
