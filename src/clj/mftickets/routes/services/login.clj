(ns mftickets.routes.services.login
  (:require
   [ring.util.http-response :as http-response]
   [mftickets.routes.services.helpers :as services.helpers :refer [if-let-user]]
   [mftickets.domain.login :as domain.login]
   [mftickets.domain.users :as domain.users]
   [clojure.core.match :refer [match]]))

(defn- invalid-key-bad-request
  "Helper function returning a bad request for an invalid key."
  []
  (http-response/bad-request {:message "Invalid key!"}))

(defn- handle-send-user-key
  "Handler for sending a key to an user."
  [{{{:keys [email]} :body} :parameters}]
  (let [key-value (domain.login/generate-random-key-value)
        user (domain.users/get-or-create-user! {:email email})
        user-key (domain.login/create-user-key! {:user-id (:id user) :value key-value})]
    (domain.login/send-key! {:email email :user-key user-key}))
  (http-response/no-content))

(defn- handle-get-token
  "Handler for getting a tokey for an user."
  [{{{:keys [email keyValue]} :body} :parameters}]
  (if-let-user [user (domain.users/get-user {:email email})]
    (let [given-user-key {:user-id (:id user) :value keyValue}
          token-value (domain.login/generate-random-token-value)
          token-or-err (domain.login/create-user-token!
                        {:user-key given-user-key
                         :token-value token-value})]
      (match token-or-err
        ::domain.login/invalid-user-key
        (invalid-key-bad-request)
        
        token
        (http-response/ok {:token (:value token)})))))

(def routes
  [["/send-key"
    {:post {:summary "sends key to an user for authentication."
            :parameters {:body {:email string?}}
            :responses {204 {}}
            :handler handle-send-user-key}}]
   ["/get-token"
    {:post {:summary "returns a token given an user key and an email"
            :parameters {:body {:email string? :keyValue string?}}
            :responses {200 {:token {:value string?}}
                        400 {:message string?}}
            :handler handle-get-token}}]])
