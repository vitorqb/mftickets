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

(def radio-property-option
  {(ds/opt :id) (spec/nilable int?)
   :value string?
   (ds/opt :property-id) (spec/nilable int?)})

(def template
  {(ds/opt :id) (spec/nilable int?)
   :project-id int?
   :name string?
   (ds/opt :creation-date) (spec/nilable string?)
   :sections
   [{(ds/opt :id) (spec/nilable int?)
     (ds/opt :template-id) (spec/nilable int?)
     :name string?
     :order (spec/nilable integer?)
     :properties
     [{(ds/opt :id) (spec/nilable int?)
       (ds/opt :template-section-id) (spec/nilable int?)
       :name string?
       :is-multiple boolean?
       :value-type keyword?
       :order (spec/nilable integer?)
       (ds/opt :templates.properties.types.radio/options) [radio-property-option]}]}]})
