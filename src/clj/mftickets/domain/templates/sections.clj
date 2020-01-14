(ns mftickets.domain.templates.sections
  (:require [clojure.set :as set]
            [com.rpl.specter :as s]
            [mftickets.db.core :as db.core]
            [mftickets.db.templates.sections :as db.templates.sections]
            [mftickets.domain.templates.sections.inject
             :as
             domain.templates.sections.inject]))

(defn get-sections-for-template
  "Returns a list of sections for a template."
  [template]
  (db.templates.sections/get-sections-for-template template))

(defn get-sections-for-templates
  "Returns a list of sections for a list of templates."
  [templates]
  (->> templates (map :id) db.templates.sections/get-sections-for-templates-ids))

(defn delete-properties-for-section!
  "Deletes all properties for a section."
  [{::domain.templates.sections.inject/keys [delete-property!]} section]
  {:pre [(fn? delete-property!)]}
  (apply db.core/run-effects! (mapv (fn [p] [delete-property! p]) (:properties section))))

(defn delete-section!
  "Deletes an existing section."
  [inject section]
  (db.core/run-effects!
   [delete-properties-for-section! inject section]
   [db.templates.sections/delete-section! section]))
