(ns mftickets.db.core
  (:require
   [clojure.walk :as walk]
   [clojure.java.jdbc :as jdbc]
   [conman.core :as conman]
   [java-time.pre-java8 :as jt]
   [mount.core :refer [defstate]]
   [mftickets.config :refer [env]]))

(defstate ^:dynamic *db*
          :start (conman/connect! {:jdbc-url (env :database-url)})
          :stop (conman/disconnect! *db*))

(conman/bind-connection *db* "sql/queries.sql")

(defn run-effects!
  "Runs a bunch of functions inside a transaction.
  `funs` must be a sequence of (fun & args).
  Returns the value of the last applied fun.
  If the keyword `::<` is found in args, it is substituted by the current value.
  The check is done view clojure.walk/postwalk."
  [[fun & args] & funs-args]
  (let [result (atom nil)
        fun-args* (concat [(apply vector fun args)] funs-args)]
    (conman/with-transaction [*db*]
      (doseq [[fun** & args*] fun-args*
              :let [args** (walk/postwalk #(if (= % ::<) @result %) args*)]]
        (reset! result (apply fun** args**))))
    @result))

(defmacro with-rollback
  "Runs body inside a transaction to be rollbacked."
  [& body]
  `(conman/with-transaction [*db*]
     (jdbc/db-set-rollback-only! *db*)
     ~@body))

(defn get-id-from-insert
  "Returns the last inserted id from an sqlite insert."
  [x]
  (get x (keyword "last_insert_rowid()")))

(extend-protocol jdbc/IResultSetReadColumn
  java.sql.Timestamp
  (result-set-read-column [v _2 _3]
    (.toLocalDateTime v))
  java.sql.Date
  (result-set-read-column [v _2 _3]
    (.toLocalDate v))
  java.sql.Time
  (result-set-read-column [v _2 _3]
    (.toLocalTime v)))

(extend-protocol jdbc/ISQLValue
  java.util.Date
  (sql-value [v]
    (java.sql.Timestamp. (.getTime v)))
  java.time.LocalTime
  (sql-value [v]
    (jt/sql-time v))
  java.time.LocalDate
  (sql-value [v]
    (jt/sql-date v))
  java.time.LocalDateTime
  (sql-value [v]
    (jt/sql-timestamp v))
  java.time.ZonedDateTime
  (sql-value [v]
    (jt/sql-timestamp v)))

