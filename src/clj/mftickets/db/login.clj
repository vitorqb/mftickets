(ns mftickets.db.login
  (:require
   [mftickets.db.core :as db.core]
   [conman.core :as conman]
   [mftickets.utils.date-time :as utils.date-time]
   [mftickets.utils.transform :as utils.transform]))

(conman/bind-connection db.core/*db* "sql/queries/login.sql")

(defn get-user-key
  "Retrieves an user key from the database."
  [id]
  (some-> {:id id}
          get-user-key*
          (utils.transform/remapkey :createdat :created-at)
          (utils.transform/remapkey :isvalid :is-valid )
          (utils.transform/remapkey :userid :user-id )))

(defn create-user-key!
  "Creates and returns a new user-key."
  [user-key]
  (-> user-key
      (assoc :created-at (utils.date-time/now-as-str))
      (assoc :is-valid? true)
      (create-user-key!*)
      (db.core/get-id-from-insert)
      get-user-key))

(defn get-user-token
  "Retrieves an user token from the database."
  [id]
  (some-> {:id id}
          get-user-token*
          (utils.transform/remapkey :createdat :created-at)
          (utils.transform/remapkey :hasbeeninvalidated :has-been-invalidated)
          (update :has-been-invalidated #(not (= 0 %)))
          (utils.transform/remapkey :userid :user-id )))

(defn get-user-id-from-token-value
  "Retrieves the user-id given a token value, or nil if there is not valid token."
  [token-value]
  (some-> {:token-value token-value} get-user-id-from-token-value* :userid))

(defn create-user-token!
  "Creates and returns a token for an user."
  [user-token]
  (-> user-token
      (assoc :created-at (utils.date-time/now-as-str))
      (create-user-token!*)
      (db.core/get-id-from-insert)
      get-user-token))

(defn is-valid-token-value?
  [token-value]
  (some-> {:value token-value} is-valid-token-value?* :response zero? not))

(defn is-valid-user-key?
  "Returns a boolean (true or nil) indicating if a given user-key is valid."
  [user-key]
  (some-> user-key user-key-exists?* :response zero? not))
