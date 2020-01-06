(ns user
  "Userspace functions you can run by default in your local REPL."
  (:require [clojure.spec.alpha :as spec]
            [clojure.tools.namespace.repl :as repl]
            [conman.core :as conman]
            [expound.alpha :as expound]
            [luminus-migrations.core :as migrations]
            [mftickets.config :refer [env]]
            [mftickets.core :refer [start-app]]
            [mftickets.db.core :as db.core]
            [mftickets.db.prefill :as db.prefill]
            [mount.core :as mount]
            [clojure.test :as t]))

(add-tap (bound-fn* clojure.pprint/pprint))

(defn start 
  "Starts application.
  You'll usually want to run this on startup."
  []
  (mount/start-without #'mftickets.core/repl-server))

(defn stop 
  "Stops application."
  []
  (mount/stop-except #'mftickets.core/repl-server))

(defn restart 
  "Restarts application."
  []
  (stop)
  (start))

(defn restart-db 
  "Restarts database."
  []
  (mount/stop #'mftickets.db.core/*db*)
  (mount/start #'mftickets.db.core/*db*)
  (binding [*ns* 'mftickets.db.core]
    (conman/bind-connection mftickets.db.core/*db* "sql/queries.sql")))

(defn reset-db 
  "Resets database."
  []
  (migrations/migrate ["reset"] (select-keys env [:database-url])))

(defn migrate 
  "Migrates database up for all outstanding migrations."
  []
  (migrations/migrate ["migrate"] (select-keys env [:database-url])))

(defn rollback 
  "Rollback latest database migration."
  []
  (migrations/migrate ["rollback"] (select-keys env [:database-url])))

(defn create-migration 
  "Create a new up and down migration file with a generated timestamp and `name`."
  [name]
  (migrations/create name (select-keys env [:database-url])))

(defn run-prefills!
  "Runs all prefills from mftickets.db.prefill"
  []
  (db.prefill/run-prefills! db.core/*db*))

(defn refresh-all-ns! []
  (stop)
  (repl/refresh-all :after 'user/start))

(spec/check-asserts true)
