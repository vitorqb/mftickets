(ns mftickets.db.templates
  (:require
   [mftickets.db.core :as db.core]
   [conman.core :as conman]
   [mftickets.utils.transform :as utils.transform]
   [mftickets.middleware.pagination :as middleware.pagination]))

(conman/bind-connection db.core/*db* "sql/queries/templates.sql")

(defn get-raw-template
  "Get's a raw template."
  [{id :id}]
  (some-> {:id id}
          get-raw-template*
          (utils.transform/remapkey :projectid :project-id)
          (utils.transform/remapkey :creationdate :creation-date)))

(defn get-raw-templates-for-project
  [{:keys [project-id] :as opts}]
  (let [pagination (db.core/parse-pagination-data opts)]
    (some->> {:project-id project-id :pagination pagination}
             get-raw-templates-for-project*
             (map #(utils.transform/remapkey % :projectid :project-id))
             (map #(utils.transform/remapkey % :creationdate :creation-date)))))

(defn count-templates-for-project
  [data]
  {:post [(int? %)]}
  (or (some-> data count-templates-for-project* :response)
      0))
