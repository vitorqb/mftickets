(ns mftickets.domain.tickets.properties-values
  (:require [clojure.spec.alpha :as spec]
            [mftickets.domain.tickets.properties-values.get
             :as
             properties-values.get]
            ;; Requires for reading the multimethods
            mftickets.domain.tickets.properties-values.types.date
            mftickets.domain.tickets.properties-values.types.radio
            mftickets.domain.tickets.properties-values.types.text))

(defn get-properties-values
  "Gets all properties values from `properties` for `ticket`."
  [{:keys [ticket properties]}]
  (->> properties
       (map #(properties-values.get/get-property-value {:ticket ticket :property %}))
       (filter identity)))
