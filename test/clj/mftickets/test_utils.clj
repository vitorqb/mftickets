(ns mftickets.test-utils
  (:require
   [mftickets.db.core :as db.core]
   [mount.core :as mount]
   [mftickets.config :as config]
   [luminus-migrations.core :as migrations]))

(defmacro with-db
  "A function to be used to tests that demand db access."
  [& body]
  `(do
     (mount/start #'mftickets.config/env #'mftickets.db.core/*db*)
     (migrations/migrate ["migrate"] (select-keys mftickets.config/env [:database-url]))
     (db.core/with-rollback
       ~@body)
     (mount/stop #'mftickets.config/env #'mftickets.db.core/*db*)))

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
