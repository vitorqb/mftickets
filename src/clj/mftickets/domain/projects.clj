(ns mftickets.domain.projects
  (:require
   [mftickets.db.projects :as db.projects]))

(defn get-project [id] (db.projects/get-project {:id id}))
