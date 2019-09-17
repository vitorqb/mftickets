(ns mftickets.routes.services.templates
  (:require
   [mftickets.domain.templates :as domain.templates]
   [mftickets.domain.templates.sections :as domain.templates.sections]
   [mftickets.domain.templates.properties :as domain.templates.properties]
   [mftickets.domain.users :as domain.users]))

(defn- user-has-access?
  "Does a user has access to a template given it's id?"
  [user template]
  (let [user-groups (domain.users/get-projects-ids-for-user user)
        template-groups (domain.templates/get-projects-ids-for-template template)]
    (boolean (some user-groups template-groups))))

(defn- wrap-user-has-access?
  [handler]
  (fn [{::keys [template] :mftickets.auth/keys [user] :as request}]
    (if (or (not template) (user-has-access? user template))
      (handler request)
      {:status 404})))

(defn- assoc-sections
  "Assocs `:sections` for a template."
  [template]
  (let [sections (domain.templates.sections/get-sections-for-template template)]
    (assoc template :sections sections)))

(defn- assoc-properties
  "Assocs `:properties` for all template `:sections`."
  [template]
  (let [properties (domain.templates.properties/get-properties-for-template template)]
    (domain.templates/assoc-properties-to-template template properties)))

(defn- get-template
  "Get's a template from an id."
  [template-id]
  (some-> template-id domain.templates/get-raw-template assoc-sections assoc-properties))

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

(def routes
  [[""
    {:middleware [[wrap-get-template] [wrap-user-has-access?]]}
    ["/:id"
     {:get {:summary "Get a template."
            :parameters {:path {:id int?}}
            :handler handle-get}}]]])
