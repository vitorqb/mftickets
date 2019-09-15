(ns mftickets.db.templates.properties
  (:require
   [mftickets.utils.transform :as utils.transform]
   [mftickets.db.core :as db.core]
   [conman.core :as conman]))

(conman/bind-connection db.core/*db* "sql/queries/properties.sql")

(defn get-properties-for-template
  "Returns all properties for a template."
  [template]
  (some-> template
          (select-keys [:id])
          (utils.transform/remapkey :id :template-id)
          get-properties-for-template*
          (->> (map #(utils.transform/remapkey % :templatesectionid :template-section-id))
               (map #(utils.transform/remapkey % :ismultiple :is-multiple))
               (map #(utils.transform/remapkey % :valuetype :value-type))
               (map #(update % :value-type keyword))
               (map #(update % :is-multiple (comp not zero?))))))
