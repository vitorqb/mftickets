(ns mftickets.domain.templates
  (:require [clojure.spec.alpha :as spec]
            [com.rpl.specter :as s]
            [mftickets.db.core :as db.core]
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
  [{{:keys [id]} :project
    :keys [name-like]
    ::middleware.pagination/keys [page-number page-size]}]
  (let [opts {:project-id id
              :name-like name-like
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

(defn- ensure-template-properties-are-vectors
  [template]
  (s/transform [:sections s/ALL :properties] vec template))

(defn assoc-properties-to-template
  "Assocs a sequence of properties to a template's sections."
  [template properties]
  (->> properties
       (reduce assoc-property-to-template template)
       (ensure-template-properties-are-vectors)))

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
  [{:keys [project name-like]}]
  (let [opts {:project-id (:id project) :name-like name-like}]
    (db.templates/count-templates-for-project opts)))

(defn get-templates-for-project
  "Returns a list of templates for a project."
  [{::domain.templates.inject/keys [get-properties-for-templates get-sections-for-templates]
    :as inject}
   {:keys [project name-like] ::middleware.pagination/keys [page-number page-size]}]

  {:pre [(fn? get-properties-for-templates) (fn? get-sections-for-templates)]}

  (let [raw-templates-opts
        {:project project
         :name-like name-like
         ::middleware.pagination/page-number page-number
         ::middleware.pagination/page-size page-size}

        raw-templates
        (get-raw-templates-for-project raw-templates-opts)

        properties
        (get-properties-for-templates raw-templates)

        sections
        (get-sections-for-templates raw-templates)]
    
    (map #(raw-template->template % properties sections) raw-templates)))

(defn- update-raw-template!
  "Updates a raw template (without sections)"
  [template]
  (db.templates/update-raw-template! template))

(defn- compare-template-sections
  "Compares sections of old and new templates, and returns the sections that must
  be actioned with `action`. For `:update`, returns a seq of [old-section new-section]."
  [action old-template new-template]
  {:pre [(#{:delete :create :update} action)]}
  (let [old-sections-ids (->> old-template :sections (map :id) (into #{}))
        new-sections-ids (->> new-template :sections (map :id) (into #{}))
        find-old-section (fn [new-s]
                           (some (fn [old-s] (and (= (:id old-s) (:id new-s)) old-s))
                                 (:sections old-template)))]
    (case action
      :delete (filter #(-> % :id new-sections-ids not) (:sections old-template))
      :create (filter #(-> % :id old-sections-ids not) (:sections new-template))
      :update (let [to-update (filter #(-> % :id old-sections-ids) (:sections new-template))]
                (map (fn [new-s] [(find-old-section new-s) new-s]) to-update)))))

(defn update-template!
  "Updates a template."
  [{::domain.templates.inject/keys [update-section! delete-section! create-section!] :as inject}
   old-template
   new-template]

  {:pre [(fn? update-section!) (fn? delete-section!) (fn? create-section!)]}

  (let [sections-to-delete
        (compare-template-sections :delete old-template new-template)

        sections-to-delete-ef
        (mapv (fn [s] [delete-section! inject s]) sections-to-delete)

        sections-to-create
        (compare-template-sections :create old-template new-template)

        sections-to-create-ef
        (mapv (fn [s] [create-section! s]) sections-to-create)

        sections-to-update
        (compare-template-sections :update old-template new-template)

        sections-to-update-ef
        (mapv (fn [[old-section new-section]] [update-section! inject old-section new-section])
              sections-to-update)]

    (apply db.core/run-effects!
           [update-raw-template! new-template]
           (concat sections-to-create-ef sections-to-delete-ef sections-to-update-ef))))
