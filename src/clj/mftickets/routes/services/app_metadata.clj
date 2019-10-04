(ns mftickets.routes.services.app-metadata
  "Provides an endpoint for global metadata that is kept locally in the client memory."
  (:require
   [mftickets.domain.projects :as domain.projects]))

(defn- handle-get
  "Handler for getting the app metadata."
  [{:mftickets.auth/keys [user]}]
  {:status 200
   :body {:projects (or (domain.projects/get-projects-for-user user) [])}})

(def routes
  [[""
    {:get {:summary "Get global metadata for the app."
           :handler handle-get}}]])
