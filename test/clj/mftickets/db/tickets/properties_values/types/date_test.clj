(ns mftickets.db.tickets.properties-values.types.date-test
  (:require [mftickets.db.tickets.properties-values.types.date :as sut]
            [clojure.test :as t :refer [is are deftest testing use-fixtures]]
            [mftickets.test-utils :as tu]))

(deftest test-get-date-property-value

  (tu/with-db
    (let [id 1
          generic-property-value {:id 2}
          value "2020-01-06T12:14:56"
          _ (tu/insert! :datePropertiesValues {:id id
                                               :propertyValueId (:id generic-property-value)
                                               :value value})
          result (sut/get-date-property-value generic-property-value)]

      (testing "Retrieves value from db"
        (is (= (:templates.properties.types.date/value result) value))))))

(deftest test-create-date-property-value

  (tu/with-db
    (let [property-value-id 1231
          value "2020-01-06T12:31:09"
          property-value-data {:templates.properties.types.date/value value}
          _ (sut/create-date-property-value property-value-data property-value-id)]
      (is (= 1 (tu/count! "FROM datePropertiesValues WHERE value = ? AND propertyValueId = ?"
                          value
                          property-value-id))))))
