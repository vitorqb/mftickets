(ns mftickets.domain.templates.sections.create
  (:require [com.rpl.specter :as s]
            [mftickets.db.core :as db.core]
            [mftickets.db.templates.sections :as db.templates.sections]
            [mftickets.domain.templates.sections.inject
             :as
             domain.templates.sections.inject]
            [mftickets.domain.templates.sections.update :as templates.sections.update]))

(defn- create-properties-for-new-section!
  "Create the properties for a new created section."
  [{::domain.templates.sections.inject/keys [get-properties-for-section] :as inject}
   new-section
   properties]
  {:pre [(-> new-section :id nil? not) (ifn? get-properties-for-section)]}
  (->> new-section
       (s/setval [:properties] properties)
       (s/setval [:properties s/ALL :template-section-id] (:id new-section))
       (templates.sections.update/update-section-properties! inject {}))
  (assoc new-section :properties (get-properties-for-section new-section)))

(defn create-section!
  "Creates a new section."
  [inject section]
  (db.core/run-effects!
   [db.templates.sections/create-section! section]
   [create-properties-for-new-section! inject ::db.core/< (:properties section)]))
