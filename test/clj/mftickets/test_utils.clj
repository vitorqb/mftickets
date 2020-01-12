(ns mftickets.test-utils
  (:require [clojure.java.jdbc :as jdbc]
            [conman.core :as conman]
            [mftickets.config :as config]
            [mftickets.db.core :as db.core]
            [mftickets.domain.projects :as domain.projects]
            [mftickets.domain.users :as domain.user]
            mftickets.inject
            [mftickets.test-utils.impl.factories :as impl.factories]
            [mount.core :as mount]
            [muuntaja.core :as muuntaja]
            [ring.mock.request :as mock.request]))

(def test-db "jdbc:sqlite:mftickets_test.db")

(defn insert!
  "Wrapper around jdbc insert!"
  [table params]
  (jdbc/insert! db.core/*db* table params))

(defn count!
  "A shortcut for a count statement."
  [from-where-clause & params]
  (or (some->> (apply vector (str "SELECT COUNT(*) AS result " from-where-clause) params)
               (jdbc/query db.core/*db*)
               first
               :result)
      0))

(defn common-fixture
  "Common fixtures for all tests."
  [f]
  (mount/start #'mftickets.inject/inject)
  (f)
  (mount/stop #'mftickets.inject/inject))

(defmacro with-db
  "A function to be used to tests that demand db access."
  [& body]
  `(do
     (binding [mftickets.db.core/*db* (conman/connect! {:jdbc-url ,test-db})] 
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
  "Context handler providing an user and a token.
  If `project-sym` is given, also creates a project and assigns it to the user."
  [[user-sym token-sym project-sym] & body]
  {:pre [(symbol? user-sym) (symbol? token-sym) (or (nil? project-sym) (symbol? project-sym))]}
  `(do
     (insert! :users {:id 1 :email "user@user.com"})
     (insert! :userLoginTokens {:id 1 :userId 1 :value "foo" :createdAt "2019-01-01T12:12:12"})
     ~@(when project-sym
         `((insert! :projects {:id 127381 :name "A Project" :description "A proj description"})
           (insert! :usersProjects {:id 78218127 :userId 1 :projectId 127381})))
     (let [~user-sym (domain.user/get-user-by-id {:id 1})
           ~token-sym "foo"
           ~@(and project-sym `(~project-sym (domain.projects/get-project 127381)))
           ;; ~(or project-sym '_) ~(and project-sym '(domain.projects/get-project 127381))
           ]
       ~@body)))

(defn decode-response-body
  "Parses and returns the body of a request"
  [r]
  (when-not (-> r :headers (get "Content-Type") (= "application/octet-stream"))
    (muuntaja/decode-response-body r)))

(defn auth-header
  "Adds a token to the header of a request."
  [request token-value]
  (mock.request/header request "authorization" (str "Bearer " token-value)))


;; Factories
(defn gen [strategy opts] (impl.factories/gen strategy opts))
(defn gen-save!
  ([strategy] (gen-save! strategy {}))
  ([strategy opts] (impl.factories/gen-save! strategy opts insert!)))
(defn save! [strategy obj] (impl.factories/save! strategy obj))
(def template (impl.factories/->Template))
(def ticket (impl.factories/->Ticket))
(def template-section (impl.factories/->TemplateSection))
(def template-section-property (impl.factories/->TemplateSectionProperty))
(def user-login-token (impl.factories/->UserLoginToken))
(def users-projects (impl.factories/->UsersProjects))
(def project (impl.factories/->Project))
(def user (impl.factories/->User))
