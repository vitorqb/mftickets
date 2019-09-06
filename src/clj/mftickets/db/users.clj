(ns mftickets.db.users
  (:require
   [conman.core :as conman]
   [mftickets.db.core :as db.core]))

(conman/bind-connection db.core/*db* "sql/queries/users.sql")

(defn get-user
  [params]
  (get-user* params))

(defn create-user!
  [params]
  (create-user!* params))
