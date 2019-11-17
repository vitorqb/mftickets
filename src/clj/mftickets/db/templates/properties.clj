(ns mftickets.db.templates.properties
  (:require
   [mftickets.utils.transform :as utils.transform]
   [mftickets.db.core :as db.core]
   [conman.core :as conman]))

(conman/bind-connection db.core/*db* "sql/queries/properties.sql")

(defn- parse-raw-property
  "Parses a raw property from the db."
  [raw-property]
  (-> raw-property
      (utils.transform/remapkey :templatesectionid :template-section-id)
      (utils.transform/remapkey :ismultiple :is-multiple)
      (utils.transform/remapkey :valuetype :value-type)
      (update :value-type keyword)
      (update :is-multiple (comp not zero?))))

(defn get-properties-for-template
  "Returns all properties for a template."
  [template]
  (some-> template
          (select-keys [:id])
          (utils.transform/remapkey :id :template-id)
          get-properties-for-template*
          (->> (map parse-raw-property))))

(defn get-properties-for-templates-ids
  "Returns all properties for a list of templates ids"
  [templates-ids]
  (some-> templates-ids
          (->> (hash-map :templates-ids))
          get-properties-for-templates-ids*
          (->> (map parse-raw-property))))

(defn get-property
  [id]
  (some-> {:id id}
          get-property*
          (utils.transform/remapkey :templatesectionid :template-section-id)
          (utils.transform/remapkey :ismultiple :is-multiple)
          (utils.transform/remapkey :valuetype :value-type)
          (update :value-type keyword)
          (update :is-multiple (comp not zero?))))

(defn delete-property!
  "Deletes a property from the db."
  [property]
  (delete-property!* {:id (:id property)}))

(defn update-property!
  [property]
  (-> property
      (update :value-type #(str (namespace %) "/" (name %)))
      (update :is-multiple #(if % 1 0))
      update-property!*))

(defn create-property!
  [property]
  (-> property
      (update :value-type #(str (namespace %) "/" (name %)))
      (update :is-multiple #(if % 1 0))
      create-property!*
      db.core/get-id-from-insert
      get-property))
