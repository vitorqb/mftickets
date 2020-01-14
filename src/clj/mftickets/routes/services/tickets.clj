(ns mftickets.routes.services.tickets
  (:require [clojure.core.match :as match]
            [mftickets.domain.templates :as domain.templates]
            [mftickets.domain.templates.properties :as domain.properties]
            [mftickets.domain.templates.properties.get :as domain.properties.get]
            [mftickets.domain.tickets :as domain.tickets]
            [mftickets.http.responses :as http.responses]
            [mftickets.inject :as inject]
            [mftickets.routes.services.tickets.validation.create
             :as
             tickets.validation.create]
            [mftickets.utils.date-time :as utils.date-time]
            [mftickets.validation.core :as validation]
            [spec-tools.data-spec :as ds]))

;; Helpers
(defn- request->ticket-data [{{ticket-data :body} :parameters user :mftickets.auth/user}]
  (update ticket-data :created-by-user-id #(or % (:id user))))

(defn- validate-ticket-create
  [request]
  (let [data {:old-ticket nil :new-ticket (request->ticket-data request)}]
    (validation/validate tickets.validation.create/validations data)))

(defn- get-properties-for-ticket-data [ticket-data]
  (->> ticket-data
       :template-id
       (domain.templates/get-template inject/inject)
       domain.properties.get/get-properties-for-template))

(defn- create-ticket!
  "Small wrapper around `create-ticket` that extracts info from the request"
  [request]
  (let [ticket-data (request->ticket-data request)
        properties (get-properties-for-ticket-data ticket-data)
        create-opts {:properties properties}]
    (domain.tickets/create-ticket! ticket-data create-opts)))

;; Handlers
(defn- handle-create
  "Handler for creating a new ticket."
  [request]
  (validation/if-let-err [err (validate-ticket-create request)]
    (http.responses/validation-error err)
    {:status 200 :body (create-ticket! request)}))

(def routes
  [[""
    {:post {:summary
            "Creates a ticket"

            ;; !!!! TODO -> Proper checkixong
            :parameters
            {:body {(ds/opt :id) nil?
                    :template-id int?
                    (ds/opt :created-at) nil?
                    (ds/opt :created-by-user-id) nil?
                    :properties-values
                    [{(ds/opt :id) nil?
                      (ds/opt :ticket-id) nil?
                      :property-id int?
                      (ds/opt :templates.properties.types.text/value) string?
                      (ds/opt :templates.properties.types.date/value) utils.date-time/date-str?}]}}
            
            :handler
            #'handle-create}}]])

