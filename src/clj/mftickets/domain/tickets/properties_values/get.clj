(ns mftickets.domain.tickets.properties-values.get
  (:require [clojure.spec.alpha :as spec]
            [mftickets.db.core :as db.core]
            [mftickets.db.tickets.properties-values :as db.properties-values]
            [mftickets.domain.tickets.properties-values.get.inject
             :as
             properties-values.get.inject]))

(defn- get-generic-property-value
  "Gets a the generic part of a property value, without any type specific info."
  [{:keys [property ticket] :as opts}]
  (db.properties-values/get-generic-property-value opts))

(defmulti get-type-specific-property-value
  "Gets the type specific information for a given property value.
  Dispatches on properties' :value-type."
  (fn [{:keys [property ticket] :as opts} generic-property-value] (:value-type property)))

(defn get-property-value
  "Gets a property value for a given property and ticket."
  [opts]
  (if-let [generic-data (get-generic-property-value opts)]
    (let [type-specific-data (get-type-specific-property-value opts generic-data)]
      (merge generic-data type-specific-data))))
