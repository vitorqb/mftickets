(ns mftickets.domain.templates
  (:require
   [mftickets.db.templates :as db.templates]))

(defn get-raw-template
  "Get's a raw template from id."
  [id]
  (some->> id (hash-map :id) db.templates/get-raw-template))

(defn get-projects-ids-for-template
  "Get's a set of projects ids for a given template."
  [{:keys [id]}]
  ;; As of now, one template <-> one project.
  (or (some-> id get-raw-template :project-id hash-set)
      #{}))
