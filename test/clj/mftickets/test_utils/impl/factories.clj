(ns mftickets.test-utils.impl.factories
  "Helper functions that generate test data."
  (:require [clojure.set]
            [mftickets.utils.kw :as utils.kw]))

;; Protocols
(defprotocol Factory
  "A protocol for a factory of test data."
  (gen [this opts] "Generates the object from opts."))

(defprotocol DbFactory
  "A protocol for a factory that can be saved to db"
  (table [this] "Returns the table to use.")
  (serialize-to-db [this obj] "Prepares an object to be saved into the db.")
  (standardize-raw-obj [this obj] "Parses the raw data used to store into it's standard form"))

(defn save! [strategy obj insert!]
  "Saves obj to the database using strategy."
  {:pre [(fn? insert!)
         (satisfies? Factory strategy)
         (satisfies? DbFactory strategy)]}
  (let [serialized-obj (serialize-to-db strategy obj)
        table          (table strategy)]
    (insert! table serialized-obj)
    (standardize-raw-obj strategy obj)))

(defn gen-save! [strategy opts insert!]
  "Generates and insert's an object into the db."
  {:pre [(fn? insert!)
         (satisfies? DbFactory strategy)
         (satisfies? Factory strategy)]}
  (let [obj (gen strategy opts)]
    (save! strategy obj insert!)))

;; Implementations
(deftype UserLoginToken [])

(extend-type UserLoginToken
  Factory
  (gen [_ opts]
    (merge
     {:id 1
      :user-id 1
      :value "AAAAAAAAAAA111111111111111111bbbbbbbbbbbbbbbbb"
      :created-at "2019-01-01T12:12:12"
      :has-been-invalidated false}
     opts))

  DbFactory
  (table [_] :userLoginTokens)
  (serialize-to-db [_ opts]
    (clojure.set/rename-keys
     opts
     {:user-id :userId
      :has-been-invalidated :hasBeenInvalidated
      :created-at :createdAt}))
  (standardize-raw-obj [_ x] x))

(deftype UsersProjects [])
(extend-type UsersProjects
  Factory
  (gen [_ opts]
    (merge {:user-id 1 :project-id 1} opts))

  DbFactory
  (table [_] :usersProjects)
  (serialize-to-db [_ opts]
    (clojure.set/rename-keys opts {:user-id :userId :project-id :projectId}))
  (standardize-raw-obj [_ x] x))

(deftype Project [])
(extend-type Project
  Factory
  (gen [_ opts]
    (merge {:id 1 :name "My Project" :description "My project description"} opts))

  DbFactory
  (table [_] :projects)
  (serialize-to-db [_ opts] opts)
  (standardize-raw-obj [_ x] x))

(deftype User [])
(extend-type User
  Factory
  (gen [_ opts] (merge {:id 1 :email "foo@bar.com"} opts))

  DbFactory
  (table [_] :users)
  (serialize-to-db [_ opts] opts)
  (standardize-raw-obj [_ x] x))


(deftype Template [])
(extend-type Template
  Factory
  (gen [_ opts]
    (merge
     {:id 1 :project-id 1 :name "Foo" :creation-date "2019-09-14T19:08:45"}
     opts))

  DbFactory
  (table [_] :templates)
  (serialize-to-db [_ {:keys [id project-id name creation-date]}]
    {:id id :projectId project-id :name name :creationDate creation-date})
  (standardize-raw-obj [_ x] x))

(deftype TemplateSection [])
(extend-type TemplateSection
  Factory
  (gen [_ opts] (merge {:id 1 :template-id 9 :name "Foo" :order 0} opts))

  DbFactory
  (table [_] :templateSections)
  (serialize-to-db [_ opts]
    (clojure.set/rename-keys opts {:template-id :templateId :order :orderIndex}))
  (standardize-raw-obj [_ x] x))

(deftype TemplateSectionProperty [])
(extend-type TemplateSectionProperty
  Factory
  (gen [_ opts]
    (merge
     {:id 1
      :template-section-id 1
      :name "Bar"
      :is-multiple false
      :value-type :templates.properties.types/text
      :order 0}
     opts))

  DbFactory
  (table [_] :templateSectionProperties)
  (serialize-to-db [_ opts]
    (-> opts
        (update :value-type utils.kw/full-name)
        (clojure.set/rename-keys {:template-section-id :templateSectionId
                                  :is-multiple :isMultiple
                                  :value-type :valueType
                                  :order :orderIndex})))
  (standardize-raw-obj [_ x] x))
