(ns mftickets.db.templates
  (:require [conman.core :as conman]
            [mftickets.db.core :as db.core]
            [mftickets.middleware.pagination :as middleware.pagination]
            [mftickets.utils.date-time :as utils.date-time]
            [mftickets.utils.transform :as utils.transform]))

(conman/bind-connection db.core/*db* "sql/queries/templates.sql")

(defn get-raw-template
  "Get's a raw template."
  [{id :id}]
  (some-> {:id id}
          get-raw-template*
          (utils.transform/remapkey :projectid :project-id)
          (utils.transform/remapkey :creationdate :creation-date)))

(defn get-raw-templates-for-project
  [{:keys [project-id name-like] :as opts}]
  (let [pagination (db.core/parse-pagination-data opts)
        name-like* (db.core/parse-string-match name-like)]
    (some->> {:project-id project-id :pagination pagination :name-like name-like*}
             get-raw-templates-for-project*
             (map #(utils.transform/remapkey % :projectid :project-id))
             (map #(utils.transform/remapkey % :creationdate :creation-date)))))

(defn count-templates-for-project
  [{:keys [name-like] :as data}]

  {:post [(int? %)]}

  (let [name-like* (db.core/parse-string-match name-like)
        data* (assoc data :name-like name-like*)]
    (or (some-> data* count-templates-for-project* :response)
        0)))

(defn update-raw-template!
  [raw-template]
  (update-raw-template!* raw-template))

(defn create-template!
  "Creates a template. Does not create any other related object."
  [raw-template]
  (-> raw-template
      (assoc :creation-date (utils.date-time/now-as-str))
      create-template!*
      db.core/get-id-from-insert
      (as-> x (get-raw-template {:id x}))))

(defn unique-template-name-for-project?
  [name project-id]
  (-> {:name name :project-id project-id}
      unique-template-name-for-project?*
      :response
      (= 0)))

(defn delete-template! [{:keys [id]}] (delete-template* {:id id}))

#_(do (require '[hugsql.core :as h])
      (h/def-sqlvec-fns "sql/queries/templates.sql"))
