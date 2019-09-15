(ns mftickets.test-utils
  (:require
   [mftickets.db.core :as db.core]
   [conman.core :as conman]
   [mount.core :as mount]
   [mftickets.config :as config]
   [luminus-migrations.core :as migrations]
   [muuntaja.core :as muuntaja]
   [mftickets.middleware.formats :as middleware.formats]))

(def test-db "jdbc:sqlite:mftickets_test.db")

(defmacro with-db
  "A function to be used to tests that demand db access."
  [& body]
  `(do
     (binding [mftickets.db.core/*db* (conman/connect! {:jdbc-url ,test-db})] 
       (migrations/migrate ["migrate"] {:database-url ,test-db})
       (db.core/with-rollback
         ~@body)
       (conman/disconnect! mftickets.db.core/*db*))))

(defmacro with-app
  "A function to be used to tests that deman app access."
  [& body]
  `(do
     (mount/start #'mftickets.config/env
                  #'mftickets.handler/init-app
                  #'mftickets.handler/app-routes)
     ~@body
     (mount/stop #'mftickets.config/env
                 #'mftickets.handler/init-app
                 #'mftickets.handler/app-routes)))

(defn decode-response-body
  "Parses and returns the body of a request"
  [r]
  (muuntaja/decode-response-body r))
