(ns mftickets.routes.services.templates
  (:require
   [mftickets.domain.templates :as domain.templates]
   [mftickets.domain.templates.sections :as domain.templates.sections]))

(defn- assoc-sections
  "Assocs `:sections` for a template."
  [template]
  (let [sections (domain.templates.sections/get-sections-for-template template)]
    (assoc template :sections sections)))

(defn- get-template
  "Get's a template from an id."
  [template-id]
  (some-> template-id domain.templates/get-raw-template assoc-sections))

(defn handle-get
  [{{{template-id :id} :path} :parameters :as request}]
  (if-let [template (get-template template-id)]
    {:status 200 :body template}
    {:status 404}))

(def routes
  [["/:id"
    {:get {:summary "Get a template."
           :parameters {:path {:id int?}}
           :responses {200 (constantly true)}
           :handler handle-get}}]])
