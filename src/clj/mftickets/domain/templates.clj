(ns mftickets.domain.templates
  (:require [clojure.spec.alpha :as spec]
            [com.rpl.specter :as s]
            [mftickets.db.templates :as db.templates]
            [mftickets.domain.templates.inject :as domain.templates.inject]
            [mftickets.middleware.pagination :as middleware.pagination]))

;; Fns
(defn get-raw-template
  "Get's a raw template from id."
  [id]
  (some->> id (hash-map :id) db.templates/get-raw-template))

(defn- get-raw-templates-for-project
  "Returns a list of raw templates for a project."
  [{{:keys [id]} :project ::middleware.pagination/keys [page-number page-size]}]
  (let [opts {:project-id id
              ::middleware.pagination/page-number page-number
              ::middleware.pagination/page-size page-size}]
    (db.templates/get-raw-templates-for-project opts)))

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

(defn assoc-sections-to-template
  "Assocs a sequence of sections to a template."
  [{:keys [id] :as template} sections]
  (let [sections* (s/select [(s/filterer [:template-id #(= % id)]) s/ALL] sections)]
    (assoc template :sections sections*)))

(defn raw-template->template
  "Transforms a raw template into a template."
  [template properties sections]
  (-> template
      (assoc-sections-to-template sections)
      (assoc-properties-to-template properties)))

(defn count-templates-for-project
  "Counts the number of templates for a project."
  [{:keys [id]}]
  (->> id (hash-map :project-id) db.templates/count-templates-for-project))

(defn get-templates-for-project
  "Returns a list of templates for a project."
  [{::domain.templates.inject/keys [get-properties-for-templates get-sections-for-templates]
    :as inject}
   {:keys [project] ::middleware.pagination/keys [page-number page-size]}]

  {:pre [(fn? get-properties-for-templates) (fn? get-sections-for-templates)]}

  (let [raw-templates-opts
        {:project project
         ::middleware.pagination/page-number page-number
         ::middleware.pagination/page-size page-size}

        raw-templates
        (get-raw-templates-for-project raw-templates-opts)

        properties
        (get-properties-for-templates raw-templates)

        sections
        (get-sections-for-templates raw-templates)]
    
    (map #(raw-template->template % properties sections) raw-templates)))
