(ns mftickets.routes.services.templates
  (:require [clojure.core.match :as match]
            clojure.set
            [clojure.spec.alpha :as spec]
            [mftickets.domain.projects :as domain.projects]
            [mftickets.domain.templates :as domain.templates]
            [mftickets.domain.templates.properties :as domain.templates.properties]
            [mftickets.domain.templates.sections :as domain.templates.sections]
            [mftickets.domain.users :as domain.users]
            [mftickets.inject :refer [inject]]
            [mftickets.middleware.context :as middleware.context]
            [mftickets.middleware.pagination :as middleware.pagination]
            [mftickets.routes.services.templates.data-spec :as templates.data-spec]
            [mftickets.routes.services.templates.validation :as validation]
            [spec-tools.data-spec :as ds]))

;; Const
(def err-msg-invalid-project-id "Invalid project-id!")
(def invalid-project-id-response {:status 400 :body {:message err-msg-invalid-project-id}})

;; Helpers
(defn- validate-raw-new-template
  "Validates a new template sent by the user to update an old template.
  Returns either [error-key error-message] or the template."
  [old-template raw-new-template]
  (validation/validate-template old-template raw-new-template))

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

(defn- wrap-get-template
  "Wrapper that assocs ::template to the request, if found."
  [handler]
  (fn [{{{template-id :id} :path} :parameters :as request}]
    (let [template (get-template template-id)]
      (cond-> request
        template (assoc ::template template)
        :always  handler))))

(defn handle-get
  [{::keys [template]}]
  (if template {:status 200 :body template} {:status 404}))

(defn handle-get-project-templates
  [{::middleware.context/keys [project]
    ::middleware.pagination/keys [page-number page-size]
    {{:keys [name-like]} :query} :parameters
    :as request}]

  (let [opts
        (-> request
            (select-keys [::middleware.pagination/page-number ::middleware.pagination/page-size])
            (assoc :project project :name-like name-like))

        templates (domain.templates/get-templates-for-project inject opts)

        templates-count (domain.templates/count-templates-for-project opts)]

    {:status 200
     ::middleware.pagination/items templates
     ::middleware.pagination/total-items-count templates-count}))

(defn handle-post
  [{old-template ::template {raw-new-template :body} :parameters :as r}]

  (match/match (validate-raw-new-template old-template raw-new-template)
    [err-k err-msg]
    {:status 400 :body {:error-message err-msg :error-key err-k}}

    new-template
    (do
      (domain.templates/update-template! inject old-template new-template)
      {:status 200 :body (get-template (:id new-template))})))

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
           :handler #'handle-get
           :responses {200 {:body templates.data-spec/template}}}
     :post {:summary "Post (edit) a template."
            :parameters {:path {:id int?}
                         :body templates.data-spec/template}
            :handler #'handle-post}}]
   [""
    {:middleware [[middleware.pagination/wrap-pagination-data]
                  [wrap-get-project]
                  [wrap-user-has-access-to-project?]]
     :get {:summary "Get's a list of templates for a project."
           :parameters {:query {:project-id int?
                                (ds/opt :pageSize) int?
                                (ds/opt :pageNumber) int?
                                (ds/opt :name-like) string?}}
           :handler #'handle-get-project-templates
           :responses {200 {:body {:page-number int?
                                   :page-size int?
                                   :total-items-count int?
                                   :items [templates.data-spec/template]}}}}}]])
