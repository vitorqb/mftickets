(ns mftickets.db.tickets.properties-values.types.text
  (:require clojure.set
            [conman.core :as conman]
            [mftickets.db.core :as db.core]))

(conman/bind-connection db.core/*db* "sql/queries/properties-values/text.sql")

(defn get-text-property-value [generic-property-value]
  "Returns the type-specific info for a generic property value."
  (some-> generic-property-value
          get-text-property-value*
          (update :value #(or % ""))
          (clojure.set/rename-keys {:value :templates.properties.types.text/value})))

(defn create-text-property-value! [property-value-data property-value-id]
  (create-text-property-value!* (assoc property-value-data :id property-value-id))
  nil)
