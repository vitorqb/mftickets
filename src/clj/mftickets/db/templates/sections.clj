(ns mftickets.db.templates.sections
  (:require
   [mftickets.utils.transform :as utils.transform]
   [conman.core :as conman]
   [mftickets.db.core :as db.core]))

(conman/bind-connection db.core/*db* "sql/queries/sections.sql")

(defn get-sections-for-template
  [template]
  (some-> template
          (select-keys [:id])
          (utils.transform/remapkey :id :template-id)
          get-sections-for-template*
          (->> (map #(utils.transform/remapkey % :templateid :template-id)))))

(defn get-sections-for-templates-ids
  [templates-ids]
  (->> templates-ids
       (hash-map :templates-ids)
       get-sections-for-templates-ids*
       (map #(utils.transform/remapkey % :templateid :template-id))))

(defn get-section
  [id]
  (some-> (get-section* {:id id})
          (utils.transform/remapkey :templateid :template-id)))

(defn delete-section!
  [section]
  (delete-section!* {:id (:id section)}))

(defn update-raw-section!
  [section]
  (update-raw-section!* section))

(defn create-section!
  [section]
  (some-> section create-section!* db.core/get-id-from-insert get-section))
