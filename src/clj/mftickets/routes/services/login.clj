(ns mftickets.routes.services.login
  (:require
   [ring.util.http-response :as http-response]
   [mftickets.domain.login :as domain.login]
   [mftickets.domain.users :as domain.users]))

(defn handle-send-user-key
  "Handler for sending a key to an user."
  [{{{:keys [email]} :body} :parameters}]
  (let [key-value (domain.login/generate-random-key-value)
        user (domain.users/get-or-create-user! {:email email})
        user-key (domain.login/create-user-key! {:user-id (:id user) :value key-value})]
    (domain.login/send-key! {:email email :user-key user-key}))
  (http-response/no-content))

(def routes
  [["/send-key"
    {:post {:summary "sends key to an user for authentication."
            :parameters {:body {:email string?}}
            :responses {204 {}}
            :handler handle-send-user-key}}]])
