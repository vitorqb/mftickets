(ns mftickets.domain.projects
  (:require [clojure.spec.alpha :as spec]
            [mftickets.db.core :as db.core]
            [mftickets.db.projects :as db.projects]))

(defn get-project [id] (db.projects/get-project {:id id}))

(defn get-projects-for-user [user] (db.projects/get-projects-for-user {:user-id (:id user)}))

(defn create-project!
  "Creates a project."
  [{:keys [name description user]}]
  (db.core/run-effects!
   [db.projects/create-project! {:name name :description description}]
   [:id ::db.core/<]
   [db.projects/assign-user! {:user-id (:id user) :project-id ::db.core/<}]
   [:project-id ::db.core/<]
   [db.projects/get-project {:id ::db.core/<}]))

(defn update-project!
  "Updates a project."
  [{:keys [id]} {:keys [name description]}]
  (db.projects/update-project! {:id id :name name :description description}))

(defn delete-project!
  "Deletes a project."
  [{::keys [count-templates]} project]
  {:pre [(spec/valid? fn? count-templates)]}
  (if (-> project count-templates (> 0))
    ::error-project-has-templates
    (db.projects/delete-project project)))
