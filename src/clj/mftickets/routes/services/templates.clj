(ns mftickets.routes.services.templates
  (:require [clojure.core.match :as match]
            clojure.set
            [clojure.spec.alpha :as spec]
            [mftickets.domain.projects :as domain.projects]
            [mftickets.domain.templates :as domain.templates]
            [mftickets.domain.templates.properties :as domain.templates.properties]
            [mftickets.domain.templates.sections :as domain.templates.sections]
            [mftickets.domain.users :as domain.users]
            [mftickets.http.responses :as http.responses]
            [mftickets.inject :refer [inject]]
            [mftickets.middleware.context :as middleware.context]
            [mftickets.middleware.pagination :as middleware.pagination]
            [mftickets.routes.services.templates.data-spec :as templates.data-spec]
            [mftickets.routes.services.templates.validation.create
             :as
             templates.validation.create]
            [mftickets.routes.services.templates.validation.update
             :as
             templates.validation.update]
            [mftickets.validation.core :as validation]
            [spec-tools.data-spec :as ds]))

;; Const
(def err-msg-invalid-project-id "Invalid project-id!")
(def invalid-project-id-response {:status 400 :body {:message err-msg-invalid-project-id}})

;; Helpers
(defn- validate-template-update
  "Validates a new template sent by the user to update an old template.
  Returns either [error-key error-message] or :validation/success."
  [old-template new-template]
  (let [validation-args {:old-template old-template :new-template new-template}]
    (validation/validate templates.validation.update/validations validation-args)))

(defn- validate-new-template
  "Validates a new template sent by the user to be created.
   Returns either [error-key error-message] or :validation/success."
  [new-template]
  (let [validations-args {:new-template new-template}]
    (validation/validate templates.validation.create/validations validations-args)))

(defn- validate-template-delete
  "Validates a template being deleted."
  [template]
  ;; We don't currently validate anything
  :validation/success)

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

(defn- get-template
  "Get's a template from an id."
  [template-id]
  (domain.templates/get-template inject template-id))

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
  [{old-template ::template {new-template :body} :parameters :as r}]

  (validation/if-let-err [err (validate-template-update old-template new-template)]
    (http.responses/validation-error err)
    (do
      (domain.templates/update-template! inject old-template new-template)
      {:status 200 :body (get-template (:id new-template))})))

(defn handle-creation-post
  "Handlers a POST aiming at creating a new template."
  [{{new-template :body} :parameters}]

  (validation/if-let-err [err (validate-new-template new-template)]
    (http.responses/validation-error err)
    {:status 200 :body (domain.templates/create-template! inject new-template)}))

(defn handle-delete
  "Handler for deleting a template."
  [{template ::template}]

  (validation/if-let-err [err (validate-template-delete template)]
    (http.responses/validation-error err)
    (do (domain.templates/delete-template! template)
        {:status 200})))

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
            :handler #'handle-post}
     :delete {:summary "Deletes a template."
              :parameters {:path {:id int?}}
              :handler #'handle-delete}}]
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
                                   :items [templates.data-spec/template]}}}}
     :post {:summary "Creates a template for a project"
            :parameters {:query {:project-id int?}
                         :body templates.data-spec/template}
            :handler #'handle-creation-post}}]])
