(ns mftickets.domain.tickets.properties-values.types.date-test
  (:require [clojure.test :as t :refer [are deftest is testing use-fixtures]]
            [mftickets.domain.tickets.properties-values.create
             :as
             properties-values.create]
            [mftickets.domain.tickets.properties-values.create.inject
             :as
             properties-values.create.inject]
            [mftickets.domain.tickets.properties-values.get
             :as
             properties-values.get]
            [mftickets.domain.tickets.properties-values.types.date :as sut]
            [mftickets.test-utils :as tu]))

(deftest test-integration-create-and-get-property-value

  (tu/with-db
    (let [ticket
          {:id 1881}

          property
          {:id 222 :value-type sut/value-type}

          get-property
          (constantly property)

          inject
          {::properties-values.create.inject/get-property get-property}

          opts
          {:property property :ticket ticket}

          property-value-data
          {:id nil
           :property-id (:id property)
           :ticket-id (:id ticket)
           :templates.properties.types.date/value "2020-01-06T12:24:11"}

          property-value
          (properties-values.create/create-property-value! property-value-data opts)

          result
          (properties-values.get/get-property-value opts)]

      (is (= (:property-id result) (:id property)))
      (is (= (:ticket-id result) (:id ticket)))
      (is (= (:templates.properties.types.date/value result)
             (:templates.properties.types.date/value property-value-data)))
      (is (= 1
             (tu/count! "FROM datePropertiesValues WHERE propertyValueId = ? AND value = ?"
                        (:id property-value)
                        (:templates.properties.types.date/value property-value-data)))))))
