(ns mftickets.db.projects
  (:require
   [mftickets.db.core :as db.core]
   [conman.core :as conman]))

(conman/bind-connection db.core/*db* "sql/queries/projects.sql")

(defn get-project
  [{:keys [id]}]
  (some->> id (hash-map :id) get-project*))