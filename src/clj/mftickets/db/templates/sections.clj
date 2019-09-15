(ns mftickets.db.templates.sections
  (:require
   [mftickets.utils.transform :as utils.transform]
   [conman.core :as conman]
   [mftickets.utils.transform :as utils.transform]
   [mftickets.db.core :as db.core]))

(conman/bind-connection db.core/*db* "sql/queries/sections.sql")

(defn get-sections-for-template
  [template]
  (some-> template
          (select-keys [:id])
          (utils.transform/remapkey :id :template-id)
          get-sections-for-template*
          (->> (map #(utils.transform/remapkey % :templateid :template-id)))))
