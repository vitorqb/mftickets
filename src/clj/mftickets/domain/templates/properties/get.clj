(ns mftickets.domain.templates.properties.get
  (:require [mftickets.db.templates.properties :as db.templates.properties]))

(defmulti get-type-specific-property
  "Get and returns the property data that depends on it's value-type."
  :value-type)
(defmethod get-type-specific-property :default [_] {})

(defn get-property [id]
  (as-> id x
    (db.templates.properties/get-generic-property x)
    (merge x (get-type-specific-property x))))

(defn get-properties-for-section
  "Returns a list with all properties for a section."
  [section]
  (for [property (db.templates.properties/get-generic-properties-for-section section)]
    (merge property (get-type-specific-property property))))

(defn get-properties-for-template
  "Returns a list with all properties for a template."
  [template]
  (for [property (db.templates.properties/get-generic-properties-for-template template)]
    (merge property (get-type-specific-property property))))

(defn get-properties-for-templates
  "Returns a list with all properties for a list of templates."
  [templates]
  (let [templates-ids
        (map :id templates)
        
        properties
        (db.templates.properties/get-generic-properties-for-templates-ids templates-ids)]
    (for [property properties]
      (merge property (get-type-specific-property property)))))
