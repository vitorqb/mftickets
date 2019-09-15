(ns mftickets.domain.templates.sections
  (:require
   [mftickets.db.templates.sections :as db.templates.sections]))

(defn get-sections-for-template
  "Returns a list of sections for a template."
  [template]
  (db.templates.sections/get-sections-for-template template))
