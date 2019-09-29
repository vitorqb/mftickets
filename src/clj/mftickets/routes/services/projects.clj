(ns mftickets.routes.services.projects
  (:require
   [mftickets.middleware.context :as middleware.context]
   [mftickets.domain.projects :as domain.projects]))

(defn- handle-get
  "Handler for a get project request"
  [{::middleware.context/keys [project]}]
  {:status 200 :body project})

(defn- handle-put
  "Handler for a put project request"
  [{::middleware.context/keys [project] {{:keys [name description]} :body} :parameters}]
  {:status 200
   :body (domain.projects/update-project! project {:name name :description description})})

(defn- handle-post
  "Handler for a post project request"
  [{:mftickets.auth/keys [user] {{:keys [name description]} :body} :parameters}]
  {:pre [(not (nil? user)) (not (nil? name)) (not (nil? description))]}
  {:status 200
   :body (domain.projects/create-project! {:user user :name name :description description})})

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
           :handler handle-get}
     :put {:summary "Put for a rpoject"
           :parameters {:path {:id int?}
                        :body {:name string? :description string?}}
           :handler handle-put}}]
   [""
    {:middleware []
     :get {:summary "Get's a list of projects for an user."
           :handler handle-get-projects}
     :post {:summary "Creates a project."
            :parameters {:body {:name string? :description string?}}
            :handler handle-post}}]])
