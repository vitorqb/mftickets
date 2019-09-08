(ns mftickets.middleware.auth
  (:require
   [ring.util.http-response :as http-response]
   [clojure.core.match :refer [match]]))

(defn unauthorized-response
  "Http response for unauthorized access attempt."
  []
  (http-response/unauthorized))

(defn wrap-auth
  "An authentication middleware for mftickets."
  [handler token->user-or-err]
  (fn [request]
    (let [token (get-in request [:headers "authorization"])]
      (match (token->user-or-err token)
        :mftickets.auth/invalid
        (unauthorized-response)

        [:mftickets.auth/valid user]
        (-> request (assoc :mftickets.auth/user user) handler)))))
