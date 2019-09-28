(ns mftickets.routes.services.projects
  (:require
   [mftickets.middleware.context :as middleware.context]
   [mftickets.domain.projects :as domain.projects]))

(defn- handle-get
  "Handler for a get project request"
  [{::middleware.context/keys [project]}]
  {:status 200 :body project})

(defn- handle-get-projects
  "Handler for a list of projects"
  [{:mftickets.auth/keys [user]}]
  {:status 200 :body (or (domain.projects/get-projects-for-user user) [])})

(def ^:private wrap-get-project
  [middleware.context/wrap-get-project {:request->project-id #(-> % :parameters :path :id)}])

(def routes
  [["/:id"
    {:middleware [[wrap-get-project] [middleware.context/wrap-user-has-access-to-project?]]
     :get {:summary "Get a project."
           :parameters {:path {:id int?}}
           :handler handle-get}}]
   [""
    {:middleware []
     :get {:summary "Get's a list of projects for an user."
           :handler handle-get-projects}}]])
