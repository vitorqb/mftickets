(ns mftickets.domain.templates.sections.update
  (:require [clojure.set :as set]
            [mftickets.db.core :as db.core]
            [mftickets.db.templates.sections :as db.templates.sections]
            [mftickets.domain.templates.sections.inject
             :as
             domain.templates.sections.inject]))

(defn update-section-properties!
  "Update the properties for a given section, comparing the old and new properties."
  [{::domain.templates.sections.inject/keys [create-property! update-property!
                                             delete-property!]}
   old-section
   new-section]
  {:pre (fn? create-property!) (fn? update-property!) (fn? delete-property!)}
  (let [new-properties-ids (->> new-section :properties (map :id) (into #{}))
        old-properties-ids (->> old-section :properties (map :id) (into #{}))
        
        update-properties-ids (set/intersection new-properties-ids old-properties-ids)
        delete-properties-ids (set/difference old-properties-ids new-properties-ids)

        create-properties (filter #(-> % :id nil?) (:properties new-section))
        update-properties (filter #(-> % :id update-properties-ids) (:properties new-section))
        delete-properties (filter #(-> % :id delete-properties-ids) (:properties old-section))

        create-effects (mapv (fn [p] [create-property! p]) create-properties)
        update-effects (mapv (fn [p] [update-property! p]) update-properties)
        delete-effects (mapv (fn [p] [delete-property! p]) delete-properties)

        effects (concat create-effects update-effects delete-effects)]
    
    (apply db.core/run-effects! effects)))

(defn- update-raw-section!
  "Updates the section, ignoring it's dependencies (like properties)"
  [section]
  (db.templates.sections/update-raw-section! section))

(defn update-section!
  "Updates a section, given an old and a new one."
  [inject old-section new-section]
  (db.core/run-effects!
   [update-raw-section! new-section]
   [update-section-properties! inject old-section new-section]))
