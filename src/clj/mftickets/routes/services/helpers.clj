(ns mftickets.routes.services.helpers
  (:require
   [ring.util.http-response :as http-response]
   [mftickets.domain.login :as domain.login]
   [mftickets.domain.users :as domain.users]))

(def raw-token-regexp #"^Bearer (.*)$")

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

(defn parse-raw-token-value
  "Parses the raw token value from the req header into the token value"
  [raw-token-value]
  (some->> raw-token-value (re-find raw-token-regexp) (second)))

(defn token->user-or-err
  "Function retrieving an user from a token value, as expected by
  `mftickets.middleware.auth/wrap-auth`"
  [raw-token-value]
  (let [token-value (parse-raw-token-value raw-token-value)
        user-id (some-> token-value domain.login/get-user-id-from-token-value)
        user (some->> user-id (hash-map :id) domain.users/get-user-by-id)]
    (if (nil? user)
      :mftickets.auth/invalid
      [:mftickets.auth/valid user])))
