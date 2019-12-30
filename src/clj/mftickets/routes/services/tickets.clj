(ns mftickets.routes.services.tickets
  (:require [clojure.core.match :as match]
            [mftickets.domain.tickets :as domain.tickets]
            [mftickets.http.responses :as http.responses]
            [mftickets.inject :as inject]
            [mftickets.routes.services.tickets.validation.create
             :as
             tickets.validation.create]
            [mftickets.validation.core :as validation]
            [spec-tools.data-spec :as ds]))

;; Helpers
(defn- request->ticket-data [{{ticket-data :body} :parameters user :mftickets.auth/user}]
  (update ticket-data :created-by-user-id #(or % (:id user))))

(defn- validate-ticket-create
  [request]
  (let [data {:old-ticket nil :new-ticket (request->ticket-data request)}]
    (validation/validate tickets.validation.create/validations data)))

(defn- create-ticket!
  "Small wrapper around `create-ticket` that extracts info from the request"
  [request]
  (->> request request->ticket-data (domain.tickets/create-ticket! inject/inject)))

;; Handlers
(defn- handle-create
  "Handler for creating a new ticket."
  [request]
  (validation/if-let-err [err (validate-ticket-create request)]
    (http.responses/validation-error err)
    {:status 200 :body (create-ticket! request)}))

(def routes
  [[""
    {:post {:summary "Creates a ticket"
            :parameters {:body {(ds/opt :id) nil?
                                :template-id int?
                                (ds/opt :created-at) nil?
                                (ds/opt :created-by-user-id) nil?
                                :properties-values
                                [{(ds/opt :id) nil?
                                  (ds/opt :ticket-id) nil?
                                  :property-id int?
                                  (ds/opt :templates.properties.types.text/value) string?}]}}
            :handler #'handle-create}}]])

