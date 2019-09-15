(ns mftickets.domain.templates
  (:require
   [mftickets.db.templates :as db.templates]))

(defn get-raw-template
  "Get's a raw template from id."
  [id]
  (some->> id (hash-map :id) db.templates/get-raw-template))
