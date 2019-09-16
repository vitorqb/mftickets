(ns mftickets.test-utils
  (:require
   [mftickets.db.core :as db.core]
   [mftickets.domain.users :as domain.user]
   [conman.core :as conman]
   [mount.core :as mount]
   [mftickets.config :as config]
   [luminus-migrations.core :as migrations]
   [muuntaja.core :as muuntaja]
   [clojure.java.jdbc :as jdbc]
   [ring.mock.request :as mock.request]))

(def test-db "jdbc:sqlite:mftickets_test.db")

(defn insert!
  "Wrapper around jdbc insert!"
  [table params]
  (jdbc/insert! db.core/*db* table params))

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

(defmacro with-user-and-token
  "Context handler providing an user and a token."
  [[user-sym token-sym] & body]
  {:pre [(symbol? user-sym) (symbol? token-sym)]}
  `(do
     (insert! :users {:id 1 :email "user@user.com"})
     (insert! :userLoginTokens {:id 1 :userId 1 :value "foo" :createdAt "2019-01-01T12:12:12"})
     (let [~user-sym (domain.user/get-user-by-id {:id 1})
           ~token-sym "foo"]
       ~@body)))

(defn decode-response-body
  "Parses and returns the body of a request"
  [r]
  (muuntaja/decode-response-body r))

(defn auth-header
  "Adds a token to the header of a request."
  [request token-value]
  (mock.request/header request "authorization" (str "Bearer " token-value)))
