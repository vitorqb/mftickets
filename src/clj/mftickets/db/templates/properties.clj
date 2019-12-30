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
      (utils.transform/remapkey :orderindex :order)
      (update :value-type keyword)
      (update :is-multiple (comp not zero?))))

(defn- serialize-property-to-db
  "Serializes a property to the db representation."
  [property]
  (-> property
      (update :value-type #(str (namespace %) "/" (name %)))
      (update :is-multiple #(if % 1 0))
      (update :order #(or % nil))))

(defn get-properties-for-template
  "Returns all properties for a template."
  [template]
  (some-> {:template-id (:id template) :select (select-snip {})}
          get-properties-for-template*
          (->> (map parse-raw-property))))

(defn get-properties-for-ticket
  "Returns all properties for a ticket."
  [ticket]
  (some-> {:ticket-id (:id ticket) :select (select-snip {})}
          get-properties-for-ticket*
          (->> (map parse-raw-property))))

(defn get-properties-for-templates-ids
  "Returns all properties for a list of templates ids"
  [templates-ids]
  (some-> {:templates-ids templates-ids :select (select-snip {})}
          get-properties-for-templates-ids*
          (->> (map parse-raw-property))))

(defn get-properties-for-section [{id :id}]
  (some-> {:section-id id :select (select-snip {})}
          get-properties-for-section*
          (->> (map parse-raw-property))))

(defn get-property
  [id]
  (some-> {:id id :select (select-snip {})}
          get-property*
          parse-raw-property))

(defn delete-property!
  "Deletes a property from the db."
  [property]
  (delete-property!* {:id (:id property)}))

(defn update-property!
  [property]
  (-> property serialize-property-to-db update-property!*))

(defn create-property!
  [property]
  (-> property serialize-property-to-db create-property!* db.core/get-id-from-insert get-property))
