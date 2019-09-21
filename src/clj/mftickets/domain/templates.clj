(ns mftickets.domain.templates
  (:require
   [mftickets.db.templates :as db.templates]
   [com.rpl.specter :as s]))

(defn get-raw-template
  "Get's a raw template from id."
  [id]
  (some->> id (hash-map :id) db.templates/get-raw-template))

(defn get-raw-templates-for-project
  "Returns a list of raw templates for a project."
  [{:keys [id]}]
  (some->> id (hash-map :project-id) db.templates/get-raw-templates-for-project))

(defn get-projects-ids-for-template
  "Get's a set of projects ids for a given template."
  [{:keys [id]}]
  ;; As of now, one template <-> one project.
  (or (some-> id get-raw-template :project-id hash-set)
      #{}))

(defn assoc-property-to-template
  "Assocs a single property to a template, inside it's correct section."
  [template {:keys [template-section-id] :as property}]
  (s/transform
   [:sections (s/filterer :id #(= % template-section-id)) s/FIRST :properties]
   #(conj % property)
   template))

(defn assoc-properties-to-template
  "Assocs a sequence of properties to a template's sections."
  [template properties]
  (into {} (reduce assoc-property-to-template template properties)))
