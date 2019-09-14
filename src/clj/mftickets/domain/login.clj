(ns mftickets.domain.login
  (:require
   [mftickets.db.core :as db.core]
   [mftickets.db.login :as db.login]
   [mftickets.utils.emails :as utils.emails]))

(def uppercase-letters (map char (range 65 91)))
(def lowercase-letters (map char (range 97 123)))
(def numbers (map char (range 48 58)))
(def chars-sample (-> (concat uppercase-letters lowercase-letters numbers)))
(def key-value-length 30)
(def token-value-length 80)

(defn send-key-email-text-body
  "The text body to be sent in an email."
  [value]
  (str "Your key is: \n \n" value))

(defn generate-random-key-value
  "Generates a new random key value."
  []
  (->> #(rand-nth chars-sample)
       (repeatedly key-value-length)
       (apply str)))

(defn generate-random-token-value
  "Generates a new random token value"
  []
  (->> #(rand-nth chars-sample)
       (repeatedly token-value-length)
       (apply str)))

(defn create-user-key!
  "Creates a new key for a user, invalidating all other user keys."
  [{:keys [user-id value]}]
  (db.core/run-effects!
   [db.login/invalidate-user-keys! {:user-id user-id}]
   [db.login/create-user-key! {:user-id user-id :value value}]))

(defn send-key!
  "Sends a key to an email"
  [{:keys [email user-key]}]
  (let [text-body (-> user-key :value send-key-email-text-body)]
    (utils.emails/send-email! {:email email :text-body text-body})))

(defn is-valid-user-key?
  "Returns a boolean indicating if a given user-key is valid."
  [user-key]
  (db.login/is-valid-user-key? user-key))

(defn create-user-token!
  "Creates a new token for a given user and key.
  May return ::invalid-user-key if the user-key is invalid.
  Invalidates user-key when creating the token."
  [{:keys [user-key token-value]}]
  (if-not (is-valid-user-key? user-key)
    ::invalid-user-key
    (let [user-id (:user-id user-key)]
      (db.core/run-effects!
       [db.login/invalidate-user-keys! {:user-id user-id}]
       [db.login/create-user-token! {:user-id user-id :value token-value}]))))

(defn is-valid-token-value?
  "Returns a boolean indicating if a given token value is valid"
  [token-value]
  (db.login/is-valid-token-value? token-value))

(defn get-user-id-from-token-value
  "Retrieves the user-id given a token value, or nil if there is not valid token."
  [token-value]
  (db.login/get-user-id-from-token-value token-value))
