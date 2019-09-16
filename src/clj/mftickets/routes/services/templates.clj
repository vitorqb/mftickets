(ns mftickets.routes.services.templates
  (:require
   [mftickets.domain.templates :as domain.templates]
   [mftickets.domain.templates.sections :as domain.templates.sections]
   [mftickets.domain.templates.properties :as domain.templates.properties]
   [mftickets.domain.users :as domain.users]))

(defn- user-has-access?
  "Does a user has access to a template given it's id?"
  [user template-id]
  (let [user-groups (domain.users/get-projects-ids-for-user user)
        template-groups (domain.templates/get-projects-ids-for-template {:id template-id})]
    (boolean (some user-groups template-groups))))

(defn- wrap-user-has-access?
  [handler]
  (fn [{{{template-id :id} :path} :parameters :mftickets.auth/keys [user] :as request}]
    (if (or (not template-id) (user-has-access? user template-id))
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

(defn handle-get
  [{{{template-id :id} :path} :parameters :as request}]
  (if-let [template (get-template template-id)]
    {:status 200 :body template}
    {:status 404}))

(def routes
  [["/:id"
    {:get {:summary "Get a template."
           :parameters {:path {:id int?}}
           :handler handle-get}}]])
