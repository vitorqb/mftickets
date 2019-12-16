(ns mftickets.domain.templates.properties
  (:require
   [mftickets.db.templates.properties :as db.templates.properties]
   [com.rpl.specter :as s]))

(defn get-property-types
  "Returns a set of all template property types."
  []
  ;; Eventually there may be registration logic here, but for now this is enough!
  #{:domain.templates.properties/radio
    :domain.templates.properties/text
    :domain.templates.properties/date})

(defn get-properties-for-template
  "Returns a list with all properties for a template."
  [template]
  (db.templates.properties/get-properties-for-template template))

(defn get-properties-for-templates
  "Returns a list with all properties for a list of templates."
  [template]
  (db.templates.properties/get-properties-for-templates-ids (map :id template)))

(defn get-properties-for-section [section]
  (db.templates.properties/get-properties-for-section section))

(defn properties-getter
  "Given a list of templates, returns a getter fn for the template properties."
  [templates]
  (let [ids (map :id templates)
        properties (db.templates.properties/get-properties-for-templates-ids ids)]
    (fn [template]
      (let [sections-ids (set (s/select [:sections s/ALL :id] template))]
        (s/select [s/ALL #(sections-ids (:template-section-id %))] properties)))))

(defn delete-property!
  "Deletes a property."
  [property]
  (db.templates.properties/delete-property! property))

(defn update-property!
  "Updates a property"
  [property]
  (db.templates.properties/update-property! property))

(defn create-property!
  "Creates a property"
  [property]
  (db.templates.properties/create-property! property))
