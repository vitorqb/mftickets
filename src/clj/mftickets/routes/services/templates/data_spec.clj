(ns mftickets.routes.services.templates.data-spec
  (:require [clojure.spec.alpha :as spec]
            [spec-tools.data-spec :as ds]))

(def template
  {:id int?
   :project-id int?
   :name string?
   :creation-date string?
   :sections
   [{(ds/opt :id) (spec/or :int int? :nil nil?)
     :template-id int?
     :name string?
     :properties
     [{(ds/opt :id) (spec/or :int int? :nil nil?)
       (ds/opt :template-section-id) (spec/or :int int? :nil nil?)
       :name string?
       :is-multiple boolean?
       :value-type keyword?}]}]})
