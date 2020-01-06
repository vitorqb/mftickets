(ns mftickets.db.tickets.properties-values-test
  (:require [mftickets.db.tickets.properties-values :as sut]
            [clojure.test :as t :refer [is are deftest testing use-fixtures]]
            [mftickets.test-utils :as tu]))

(use-fixtures :once tu/common-fixture)

(deftest test-create-generic-property-value!

  (testing "When creating a generic property..."
    (tu/with-db
      (let [property-value {:id nil :property-id 2 :ticket-id 3}
            return (sut/create-generic-property-value! property-value)]

        (testing "Returns the id entered"
          (is (int? return)))

        (testing "Actually inserts into the db"
          (is (= 1 (tu/count!
                    "FROM propertiesValues WHERE id = ? AND propertyId = ? AND ticketId = ?"
                    return
                    2
                    3))))))))

(deftest test-get-generic-property-value

  (testing "Base"
    (tu/with-db
      (let [id 123
            propertyId 1317
            ticketId 13121
            _ (tu/insert! :propertiesValues {:id id :propertyId propertyId :ticketId ticketId})
            opts {:ticket {:id ticketId} :property {:id propertyId}}]
        (is (= {:id id :ticket-id ticketId :property-id propertyId}
               (sut/get-generic-property-value opts))))))

  (testing "Non Existant"
    (tu/with-db
      (is (nil? (sut/get-generic-property-value {:ticket {:id 1221} :property {:id 111}}))))))
