(ns mftickets.domain.users
  (:require
   [mftickets.db.users :as db.users]))

(defn get-user
  "Retrieves an user from the db by email."
  [params]
  (db.users/get-user params))

(defn get-user-by-id
  "Retrieves an user from the db by id."
  [params]
  (db.users/get-user-by-id params))

(defn create-user!
  "Creates an user in the database."
  [params]
  (db.users/create-user! params)
  (get-user params))


(defn get-or-create-user!
  "Gets an user from db, creating if needed."
  [params]
  (if-let [user (get-user params)]
    user
    (create-user! params)))
