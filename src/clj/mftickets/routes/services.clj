(ns mftickets.routes.services
  (:require
    [reitit.swagger :as swagger]
    [reitit.swagger-ui :as swagger-ui]
    [reitit.ring.coercion :as coercion]
    [reitit.coercion.spec :as spec-coercion]
    [reitit.ring.middleware.muuntaja :as muuntaja]
    [reitit.ring.middleware.multipart :as multipart]
    [reitit.ring.middleware.parameters :as parameters]
    [mftickets.middleware.formats :as formats]
    [mftickets.middleware.exception :as exception]
    [mftickets.middleware.auth :as middleware.auth]
    [mftickets.routes.services.app-metadata :as routes.services.app-metadata]
    [mftickets.routes.services.login :as routes.services.login]
    [mftickets.routes.services.helpers :as routes.services.helpers]
    [mftickets.routes.services.templates :as routes.services.templates]
    [mftickets.routes.services.projects :as routes.services.projects]
    [ring.util.http-response :refer :all]
    [clojure.java.io :as io]))

(def wrap-auth
  "A wrapper that authenticates using the user token."
  [middleware.auth/wrap-auth routes.services.helpers/token->user-or-err])

(defn service-routes []
  ["/api"
   {:coercion spec-coercion/coercion
    :muuntaja formats/instance
    :swagger {:id ::API :tags ["API"]}
    :middleware [;; query-params & form-params
                 parameters/parameters-middleware
                 ;; content-negotiation
                 muuntaja/format-negotiate-middleware
                 ;; encoding response body
                 muuntaja/format-response-middleware
                 ;; exception handling
                 exception/exception-middleware
                 ;; decoding request body
                 muuntaja/format-request-middleware
                 ;; coercing response bodys
                 coercion/coerce-response-middleware
                 ;; coercing request parameters
                 coercion/coerce-request-middleware
                 ;; multipart
                 multipart/multipart-middleware]}

   ;; swagger documentation
   ["" {:swagger {:info {:title "MfTickets" :description "https://github.com/vitorqb/mftickets/"}}
        :no-doc true}

    ["/swagger.json"
     {:get (swagger/create-swagger-handler)}]

    ["/api-docs/*"
     {:get (swagger-ui/create-swagger-ui-handler
             {:url "/api/swagger.json"
              :config {:validator-url nil}})}]]

   (into ["/app-metadata" {:middleware [wrap-auth] :swagger {:tags ["METADATA"]}}]
         routes.services.app-metadata/routes)

   (into ["/login" {:swagger {:tags ["LOG IN"]}}] routes.services.login/routes)

   (into
    ["/templates"
     {:middleware [wrap-auth]
      :parameters {:header {:authorization string?}}
      :swagger {:tags ["TEMPLATES"]}}]
    routes.services.templates/routes)

   (into
    ["/projects"
     {:middleware [wrap-auth]
      :parameters {:header {:authorization string?}}
      :swagger {:tags ["PROJECTS"]}}]
    routes.services.projects/routes)

   ["/ping"
    {:middleware [wrap-auth]
     :parameters {:header {:authorization string?}}
     :swagger {:tags ["PING"]}
     :get (constantly (ok {:message "pong"}))}]])
