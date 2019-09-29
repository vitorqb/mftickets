(ns mftickets.db.projects
  (:require
   [mftickets.db.core :as db.core]
   [conman.core :as conman]))

(conman/bind-connection db.core/*db* "sql/queries/projects.sql")

(defn get-project
  [{:keys [id]}]
  (some->> id (hash-map :id) get-project*))

(defn get-projects-for-user
  [{:keys [user-id]}]
  (some->> user-id (hash-map :user-id) get-projects-for-user*))

(defn create-project!
  [{:keys [name description]}]
  (->> {:name name :description description}
       create-project!*
       db.core/get-id-from-insert
       (hash-map :id)
       get-project))

(defn assign-user!
  "Assigns a project to a user by creating an entry on the usersProjects table.
  Returns the received arguments."
  [{:keys [user-id project-id]}]
  (doto {:user-id user-id :project-id project-id}
    (create-user-project!*)))
