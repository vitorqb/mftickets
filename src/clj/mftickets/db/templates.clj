(ns mftickets.db.templates
  (:require
   [mftickets.db.core :as db.core]
   [conman.core :as conman]
   [mftickets.utils.transform :as utils.transform]))

(conman/bind-connection db.core/*db* "sql/queries/templates.sql")

(defn get-raw-template
  "Get's a raw template."
  [{id :id}]
  (some-> {:id id}
          get-raw-template*
          (utils.transform/remapkey :projectid :project-id)
          (utils.transform/remapkey :creationdate :creation-date)))
