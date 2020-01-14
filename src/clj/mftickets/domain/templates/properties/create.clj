(ns mftickets.domain.templates.properties.create
  (:require [mftickets.db.core :as db.core]
            [mftickets.db.templates.properties :as db.templates.properties]))

(defmulti create-type-specific-property!
  "Creates all value-type dependent objects for a property."
  (fn [property-data created-generic-property] (:value-type property-data)))
(defmethod create-type-specific-property! :default [_ _] {})

(defn- create-and-merge-type-specific-property!
  [property-data created-generic-property]
  (merge
   created-generic-property
   (create-type-specific-property! property-data created-generic-property)))

(defn create-property!
  "Creates a property"
  [property]
  (db.core/run-effects!
   [db.templates.properties/create-generic-property! property]
   [create-and-merge-type-specific-property! property ::db.core/<]))
