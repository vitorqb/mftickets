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

