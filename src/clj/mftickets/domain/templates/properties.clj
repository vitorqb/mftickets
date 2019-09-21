(ns mftickets.domain.templates.properties
  (:require
   [mftickets.db.templates.properties :as db.templates.properties]
   [com.rpl.specter :as s]))

(defn get-properties-for-template
  "Returns a list with all properties for a template."
  [template]
  (db.templates.properties/get-properties-for-template template))

(defn properties-getter
  "Given a list of templates, returns a getter fn for the template properties."
  [templates]
  (let [ids (map :id templates)
        properties (db.templates.properties/get-properties-for-templates-ids ids)]
    (fn [template]
      (let [sections-ids (set (s/select [:sections s/ALL :id] template))]
        (s/select [s/ALL #(sections-ids (:template-section-id %))] properties)))))
