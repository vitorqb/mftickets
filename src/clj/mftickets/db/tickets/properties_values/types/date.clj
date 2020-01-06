(ns mftickets.db.tickets.properties-values.types.date
  (:require clojure.set
            [conman.core :as conman]
            [mftickets.db.core :as db.core]))

(conman/bind-connection db.core/*db* "sql/queries/properties-values/date.sql")

(defn get-date-property-value
  "Returns the date-specific data for a property value."
  [generic-property-value]
  (some-> generic-property-value
          get-date-property-value*
          (clojure.set/rename-keys {:value :templates.properties.types.date/value})))

(defn create-date-property-value
  "Inserts the data specific for date property values into the db."
  [property-value-data property-value-id]
  (create-date-property-value* (assoc property-value-data :id property-value-id)))
