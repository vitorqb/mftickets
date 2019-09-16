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

(defn get-user-by-id
  [params]
  (get-user-by-id* params))

(defn get-projects-ids-for-user
  [params]
  (or (some->> (get-projects-ids-for-user* params) (map :projectid) set)
      #{}))
