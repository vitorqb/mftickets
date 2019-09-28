(ns mftickets.routes.services.templates
  (:require
   [mftickets.domain.templates :as domain.templates]
   [mftickets.domain.templates.sections :as domain.templates.sections]
   [mftickets.domain.templates.properties :as domain.templates.properties]
   [mftickets.domain.projects :as domain.projects]
   [mftickets.domain.users :as domain.users]
   [mftickets.middleware.context :as middleware.context]
   [clojure.set]))

(def err-msg-invalid-project-id "Invalid project-id!")
(def invalid-project-id-response {:status 400 :body {:message err-msg-invalid-project-id}})

(defn- user-has-access-to-template?
  "Does a user has access to a template?"
  [user template]
  (let [user-groups (domain.users/get-projects-ids-for-user user)
        template-groups (domain.templates/get-projects-ids-for-template template)]
    (boolean (some user-groups template-groups))))

(defn- wrap-user-has-access-to-template?
  [handler]
  (fn [{::keys [template] :mftickets.auth/keys [user] :as request}]
    (if (or (not template) (user-has-access-to-template? user template))
      (handler request)
      {:status 404})))

(defn- assoc-sections
  "Assocs `:sections` for a template."
  [template sections-getter]
  (assoc template :sections (sections-getter template)))

(defn- assoc-properties
  "Assocs `:properties` for all template `:sections`."
  [template properties-getter]
  (domain.templates/assoc-properties-to-template template (properties-getter template)))

(defn- get-template
  "Get's a template from an id."
  [template-id]
  (some-> template-id
          domain.templates/get-raw-template
          (assoc-sections domain.templates.sections/get-sections-for-template)
          (assoc-properties domain.templates.properties/get-properties-for-template)))

(defn- get-project-templates
  "Get's all templates for a project."
  [project]
  (let [templates (domain.templates/get-raw-templates-for-project project)
        properties-getter (domain.templates.properties/properties-getter templates)
        sections-getter (domain.templates.sections/sections-getter templates)]
    (map #(-> % (assoc-sections sections-getter) (assoc-properties properties-getter))
         templates)))

(defn- wrap-get-template
  "Wrapper that assocs ::template to the request, if found."
  [handler]
  (fn [{{{template-id :id} :path} :parameters :as request}]
    (let [assoc-template (if-let [template (get-template template-id)] 
                           #(assoc % ::template template)
                           identity)]
      (-> request assoc-template handler))))

(defn handle-get
  [{::keys [template]}]
  (if template {:status 200 :body template} {:status 404}))

(defn handle-get-project-templates
  [{::middleware.context/keys [project]}]
  {:status 200 :body (get-project-templates project)})

(def ^:private wrap-get-project
  [middleware.context/wrap-get-project {:not-found invalid-project-id-response}])

(def ^:private wrap-user-has-access-to-project?
  [middleware.context/wrap-user-has-access-to-project?
   {:no-access invalid-project-id-response}])

(def routes
  [["/:id"
    {:middleware [[wrap-get-template] [wrap-user-has-access-to-template?]]
     :get {:summary "Get a template."
           :parameters {:path {:id int?}}
           :handler handle-get}}]
   [""
    {:middleware [[wrap-get-project] [wrap-user-has-access-to-project?]]
     :get {:summary "Get's a list of templates for a project."
           :parameters {:query {:project-id int?}}
           :handler handle-get-project-templates}}]])
