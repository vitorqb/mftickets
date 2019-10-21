(ns mftickets.domain.templates.sections
  (:require
   [mftickets.db.templates.sections :as db.templates.sections]
   [com.rpl.specter :as s]))

(defn get-sections-for-template
  "Returns a list of sections for a template."
  [template]
  (db.templates.sections/get-sections-for-template template))

(defn get-sections-for-templates
  "Returns a list of sections for a list of templates."
  [templates]
  (->> templates (map :id) db.templates.sections/get-sections-for-templates-ids))

(defn sections-getter
  "Given a list of templates, returns a getter fn for the template sections"
  [templates]
  (let [sections (->> templates (map :id) db.templates.sections/get-sections-for-templates-ids)]
    (fn [template] (s/select [s/ALL #(= (:template-id %) (:id template))] sections))))
