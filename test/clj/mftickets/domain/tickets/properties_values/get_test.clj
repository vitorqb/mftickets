(ns mftickets.domain.tickets.properties-values.get-test
  (:require [clojure.test :as t :refer [are deftest is testing use-fixtures]]
            [mftickets.db.core :as db.core]
            [mftickets.db.tickets.properties-values :as db.properties-values]
            [mftickets.domain.tickets.properties-values.get :as sut]
            [mftickets.test-utils :as tu]))

(deftest test-get-generic-property-value
  (testing "Dispatches to db"
    (with-redefs [db.properties-values/get-generic-property-value (fn [x] [::get x])]
      (let [opts {:property {:id 1} :ticket {:id 2}}]
        (is (= [::get opts] (@#'sut/get-generic-property-value opts)))))))

(deftest test-get-property-value

  (testing "Returns nil if no generic property value"
    (with-redefs [sut/get-generic-property-value (constantly nil)]
      (is (nil? (sut/get-property-value {})))))

  (testing "Merges generic and specific property value data"
    (with-redefs [sut/get-generic-property-value (constantly {:foo 1})
                  sut/get-type-specific-property-value (constantly {:bar 2})]
      (is (= (merge {:foo 1} {:bar 2}) (sut/get-property-value {}))))))

(deftest test-integration-get-property-value

  (tu/with-db
    (let [property {:id 117191 :value-type :templates.properties.types/text}
          ticket {:id 199918}
          id 81972
          value "Foo"
          _ (tu/insert! :propertiesValues {:id id
                                           :propertyId (:id property)
                                           :ticketId (:id ticket)})
          _ (tu/insert! :textPropertiesValues {:id 781278218712
                                               :propertyValueId id
                                               :value value})]
      (is (= {:id id
              :property-id (:id property)
              :ticket-id (:id ticket)
              :templates.properties.types.text/value value}
             (sut/get-property-value {:property property :ticket ticket}))))))
