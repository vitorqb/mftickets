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
  (serialize-to-db [this obj] "Prepares an object to be saved into the db."))

(defn save! [strategy obj insert!]
  "Saves obj to the database using strategy."
  {:pre [(fn? insert!)
         (satisfies? Factory strategy)
         (satisfies? DbFactory strategy)]}
  (let [serialized-obj (serialize-to-db strategy obj)
        table          (table strategy)]
    (insert! table serialized-obj)
    obj))

(defn gen-save! [strategy opts insert!]
  "Generates and insert's an object into the db."
  {:pre [(fn? insert!)
         (satisfies? DbFactory strategy)
         (satisfies? Factory strategy)]}
  (let [obj (gen strategy opts)]
    (save! strategy obj insert!)))

;; Implementations
(deftype UserLoginToken []
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
      :created-at :createdAt})))

(deftype UsersProjects []
  Factory
  (gen [_ opts]
    (merge {:userId 1 :project-id 1} opts))

  DbFactory
  (table [_] :usersProjects)
  (serialize-to-db [_ opts]
    (clojure.set/rename-keys opts {:user-id :userId :project-id :projectId})))

(deftype Template []
  Factory
  (gen [_ opts]
    (merge
     {:id 1 :project-id 1 :name "Foo" :creation-date "2019-09-14T19:08:45"}
     opts))

  DbFactory
  (table [_] :templates)
  (serialize-to-db [_ {:keys [id project-id name creation-date]}]
    {:id id :projectId project-id :name name :creationDate creation-date}))

(deftype TemplateSection []
  Factory
  (gen [_ opts] (merge {:id 1 :template-id 9 :name "Foo"} opts))

  DbFactory
  (table [_] :templateSections)
  (serialize-to-db [_ opts] (clojure.set/rename-keys opts {:template-id :templateId})))

(deftype TemplateSectionProperty []
  Factory
  (gen [_ opts]
    (merge
     {:id 1
      :template-section-id 1
      :name "Bar"
      :is-multiple false
      :value-type :section.property.value.types/text}
     opts))

  DbFactory
  (table [_] :templateSectionProperties)
  (serialize-to-db [_ opts]
    (-> opts
        (update :value-type utils.kw/full-name)
        (clojure.set/rename-keys {:template-section-id :templateSectionId
                                  :is-multiple :isMultiple
                                  :value-type :valueType}))))
