(ns mftickets.routes.services.helpers
  (:require
   [ring.util.http-response :as http-response]))

(defn unknown-user-bad-request
  "A bad request response when an user was not found."
  []
  (http-response/bad-request {:message "Unknown user."}))

(defmacro if-let-user
  "Expands to if-let, but returns an error message of user not found on the false branch."
  {:style/indent 1}
  [binding & body-true]
  `(if-let ~binding
     ~@body-true
     (unknown-user-bad-request)))
