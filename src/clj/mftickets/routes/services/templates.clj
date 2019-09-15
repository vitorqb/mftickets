(ns mftickets.routes.services.templates
  (:require
   [mftickets.domain.templates :as domain.templates]
   [mftickets.domain.templates.sections :as domain.templates.sections]
   [mftickets.domain.templates.properties :as domain.templates.properties]
   [com.rpl.specter :as specter]))

(defn- assoc-sections
  "Assocs `:sections` for a template."
  [template]
  (let [sections (domain.templates.sections/get-sections-for-template template)]
    (assoc template :sections sections)))

(defn- assoc-property-to-sections
  "Assocs a single property to it's section in a list of sections."
  [{:keys [template-section-id] :as property} sections]
  (specter/transform
   [(specter/filterer :id #(= % template-section-id)) specter/FIRST :properties]
   #(conj % property)
   sections))

(defn- assoc-property-to-template
  "Assocs a single property to a template, inside it's correct section."
  [template property]
  (update template :sections #(assoc-property-to-sections property %)))

(defn- assoc-properties
  "Assocs `:properties` for all template `:sections`."
  [template]
  (let [properties (domain.templates.properties/get-properties-for-template template)]
    (into {} (reduce assoc-property-to-template template properties))))

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
           :responses {200 (constantly true)}
           :handler handle-get}}]])
