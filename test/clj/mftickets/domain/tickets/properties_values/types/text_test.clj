(ns mftickets.domain.tickets.properties-values.types.text-test
  (:require [clojure.test :as t :refer [are deftest is testing use-fixtures]]
            [mftickets.db.core :as db.core]
            [mftickets.db.tickets.properties-values :as db.properties-values]
            [mftickets.db.tickets.properties-values.types.text
             :as
             db.properties-values.types.text]
            [mftickets.domain.tickets.properties-values.create
             :as
             properties-values.create]
            [mftickets.domain.tickets.properties-values.create.inject
             :as
             properties-values.create.inject]
            [mftickets.domain.tickets.properties-values.get
             :as
             properties-values.get]
            [mftickets.domain.tickets.properties-values.types.text :as sut]
            [mftickets.test-utils :as tu]
            [mftickets.utils.date-time :as date-time]))

(deftest test-get-text-property-value

  (testing "Dispatches to db"
    (let [generic-property-value {:id 1929181}
          specific-property-value {:templates.properties.types.text/value "FOO"}
          db-get-text-property-value (constantly specific-property-value)
          property {:value-type sut/value-type}
          ticket {:id 1}
          opts {:property property :ticket ticket}]
      (with-redefs [db.properties-values.types.text/get-text-property-value
                    db-get-text-property-value]
        (is (= specific-property-value
               (properties-values.get/get-type-specific-property-value
                opts
                generic-property-value)))))))

(deftest test-create-text-property-value

  (testing "Dispatches to db"
    (let [db-create-text-property-value (fn [x i] [::create x i])
          property {:value-type sut/value-type}
          id 1]
      (with-redefs [db.properties-values.types.text/create-text-property-value!
                    db-create-text-property-value]
        (is (= [::create property id]
               (properties-values.create/create-type-specific-property-value!
                sut/value-type
                property
                id)))))))

(deftest test-integration-create-and-get-property-value

  (tu/with-db

    (let [now
          "20200101T19:59:20"

          ticket
          {:id 2}

          property
          {:id 1 :value-type :templates.properties.types/text}

          get-property
          {1 property}

          inject
          {::properties-values.create.inject/get-property get-property}

          property-value-data
          {:id nil
           :property-id (:id property)
           :ticket-id (:id ticket)
           :templates.properties.types.text/value "Bla Bar Foo"}

          property-value
          (with-redefs [date-time/now-as-str (constantly now)]
            (properties-values.create/create-property-value! inject property-value-data))]

      (testing "Returns the gotten property value"
        (is (= (properties-values.get/get-property-value {:ticket ticket :property property})
               property-value)))

      (testing "Id is int"
        (is (int? (:id property-value))))

      (testing "property-id is equal"
        (is (= (:property-id property-value-data) (:property-id property-value))))

      (testing "Value is equal"
        (is (= (:templates.properties.types.text/value property-value-data)
               (:templates.properties.types.text/value property-value)))))))
