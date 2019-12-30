(ns mftickets.db.tickets.properties-values.types.text-test
  (:require [mftickets.db.tickets.properties-values.types.text :as sut]
            [clojure.test :as t :refer [is are deftest testing use-fixtures]]
            [mftickets.test-utils :as tu]))

(use-fixtures :once tu/common-fixture)

(deftest test-get-text-property-value

  (testing "Inexistant "
    (tu/with-db
      (let [generic-property-value {:id 1}]
        (is (nil? (sut/get-text-property-value generic-property-value))))))

  (testing "Base"
    (tu/with-db
      (let [property-value-id 91729
            value "Foo"
            _ (tu/insert! :textPropertiesValues {:id 1
                                                 :propertyValueId property-value-id
                                                 :value value})
            generic-property-value {:id property-value-id}]
        (is (= {:templates.properties.types.text/value value}
               (sut/get-text-property-value generic-property-value))))))

  (testing "Null Value"
    (tu/with-db
      (let [property-value-id 91729
            _ (tu/insert! :textPropertiesValues {:id 1
                                                 :propertyValueId property-value-id})
            generic-property-value {:id property-value-id}]
        (is (= {:templates.properties.types.text/value ""}
               (sut/get-text-property-value generic-property-value)))))))

(deftest test-create-text-property-value!

  (let [property-value-data {:id nil :property-id 1 :templates.properties.types.text/value "FFF"}
        property-value-id 999]

    (testing "Inserts into textpropertiesvalues table"
      (tu/with-db
        (sut/create-text-property-value! property-value-data property-value-id)
        (is (= 1 (tu/count! (str "FROM textPropertiesValues WHERE value = \"FFF\" AND"
                                 " propertyValueId = 999"))))))

    (testing "Returns nil"
      (tu/with-db
        (is (nil? (sut/create-text-property-value! property-value-data property-value-id)))))))
