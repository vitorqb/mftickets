(ns mftickets.routes.services.templates.data-spec
  (:require [clojure.spec.alpha :as spec]
            [com.rpl.specter :as s]
            [com.rpl.specter.protocols]
            [spec-tools.data-spec :as ds])
  (:import [spec_tools.data_spec OptionalKey]))

(defn- select-key-eq [[k1 _] k2] (= k1 k2))

(extend-type OptionalKey
  com.rpl.specter.protocols/ImplicitNav
  (implicit-nav [this] (-> this :k com.rpl.specter.protocols/implicit-nav)))

(def template
  {:id int?
   :project-id int?
   :name string?
   :creation-date string?
   :sections
   [{(ds/opt :id) (spec/or :int int? :nil nil?)
     :template-id int?
     :name string?
     :order (spec/nilable integer?)
     :properties
     [{(ds/opt :id) (spec/or :int int? :nil nil?)
       (ds/opt :template-section-id) (spec/or :int int? :nil nil?)
       :name string?
       :is-multiple boolean?
       :value-type keyword?
       :order (spec/nilable integer?)}]}]})

(def create-template
  (->> template
       (s/setval :id nil?)
       (s/setval :creation-date nil?)
       (s/setval [:sections s/FIRST (ds/opt :id)] nil?)
       (s/setval [:sections s/FIRST (ds/opt :template-id)] nil?)
       (s/setval [:sections s/FIRST :properties s/FIRST (ds/opt :id)] nil?)
       (s/setval [:sections s/FIRST :properties s/FIRST (ds/opt :template-section-id)] nil?)))
