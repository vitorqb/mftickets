(ns mftickets.db.tickets
  (:require clojure.set
            [conman.core :as conman]
            [mftickets.db.core :as db.core]
            [mftickets.utils.date-time :as utils.date-time]))

(conman/bind-connection db.core/*db* "sql/queries/tickets.sql")

;; Private
(defn- parse-ticket-from-db
  [data]
  (clojure.set/rename-keys
   data
   {:templateid :template-id
    :createdat :created-at
    :createdbyuserid :created-by-user-id}))

;; Public API
(defn get-raw-ticket
  "Get's a raw ticket (without depending objects)"
  [id]
  (some-> {:id id} get-raw-ticket* parse-ticket-from-db))

(defn create-raw-ticket!
  "Creates a raw ticket (without depending objects)"
  [ticket-data]
  (-> ticket-data
      (assoc :created-at (utils.date-time/now-as-str))
      create-raw-ticket!*
      db.core/get-id-from-insert
      get-raw-ticket))
