(ns mftickets.domain.templates.properties
  (:require
   [mftickets.db.templates.properties :as db.templates.properties]))

(defn get-properties-for-template
  "Returns a list with all properties for a template."
  [template]
  (db.templates.properties/get-properties-for-template template))
