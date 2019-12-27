(ns mftickets.db.templates.sections
  (:require
   [mftickets.utils.transform :as utils.transform]
   [conman.core :as conman]
   [mftickets.db.core :as db.core]))

(conman/bind-connection db.core/*db* "sql/queries/sections.sql")

(defn- parse-to-db [section]
  (update section :order #(or % nil)))

(defn get-sections-for-template
  [template]
  (some-> template
          (select-keys [:id])
          (assoc :select (select-snip {}))
          (utils.transform/remapkey :id :template-id)
          get-sections-for-template*
          (->> (map #(utils.transform/remapkey % :templateid :template-id)))))

(defn get-sections-for-templates-ids
  [templates-ids]
  (->> {:templates-ids templates-ids :select (select-snip {})}
       get-sections-for-templates-ids*
       (map #(utils.transform/remapkey % :templateid :template-id))))

(defn get-section
  [id]
  (some-> (get-section* {:id id :select (select-snip {})})
          (utils.transform/remapkey :templateid :template-id)))

(defn delete-section!
  [section]
  (delete-section!* {:id (:id section)}))

(defn update-raw-section!
  [section]
  (-> section parse-to-db update-raw-section!*))

(defn create-section!
  [section]
  (some-> section parse-to-db create-section!* db.core/get-id-from-insert get-section))
