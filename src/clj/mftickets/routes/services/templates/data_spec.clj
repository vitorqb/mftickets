(ns mftickets.routes.services.templates.data-spec
  (:require [clojure.spec.alpha :as spec]
            [spec-tools.data-spec :as ds]))

(def template
  {:id int?
   :project-id int?
   :name string?
   :creation-date string?
   :sections
   [{(ds/opt :id) int?
     :template-id int?
     :name string?
     :properties
     [{:id (spec/or :int int? :nil nil?)
       :template-section-id int?
       :name string?
       :is-multiple boolean?
       :value-type keyword?}]}]})
