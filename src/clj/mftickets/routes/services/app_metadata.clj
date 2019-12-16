(ns mftickets.routes.services.app-metadata
  "Provides an endpoint for global metadata that is kept locally in the client memory."
  (:require
   [mftickets.domain.projects :as domain.projects]
   [mftickets.domain.templates.properties :as domain.templates.properties]))

(defn- assoc-projects
  "Given a metadata request, assocs to `m` all available projects for an user."
  [m {:mftickets.auth/keys [user]}]
  (assoc m :projects (or (domain.projects/get-projects-for-user user) [])))

(defn- assoc-template-property-types
  "Given a metadata request, assocs to `m` all available template property types."
  [m _]
  (assoc m :template.properties.types (domain.templates.properties/get-property-types)))

(defn- handle-get
  "Handler for getting the app metadata."
  [request]
  {:status 200
   :body (-> {}
             (assoc-projects request)
             (assoc-template-property-types request))})

(def routes
  [[""
    {:get {:summary "Get global metadata for the app."
           :handler handle-get}}]])
