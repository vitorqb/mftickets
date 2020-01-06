(ns mftickets.domain.tickets.properties-values.create-test
  (:require [clojure.test :as t :refer [are deftest is testing use-fixtures]]
            [mftickets.db.core :as db.core]
            [mftickets.db.tickets.properties-values :as db.properties-values]
            [mftickets.domain.tickets.properties-values.create :as sut]
            [mftickets.domain.tickets.properties-values.create.inject
             :as
             properties-values.create.inject]
            [mftickets.domain.tickets.properties-values.get
             :as
             domain.properties-values.get]
            [mftickets.test-utils :as tu]))

(deftest test-create-property-value!

  (let [ticket {:id 11}
        db-effects (atom [])
        m-run-effects! (fn [& xs] (swap! db-effects conj xs))
        m-get-property-value (fn [x] [::get x])
        value-type :templates.properties.types/text
        get-property {9 {:id 9 :value-type value-type}}
        inject {::properties-values.create.inject/get-property get-property}
        property-value-data {:property-id 9 :ticket-id (:id ticket)}
        result (with-redefs [db.core/run-effects! m-run-effects!
                             domain.properties-values.get/get-property-value m-get-property-value]
                 (sut/create-property-value! inject property-value-data))]
  
    (testing "Dispatches to create generic property value"
      (is (= [db.properties-values/create-generic-property-value! property-value-data]
             (-> @db-effects first first))))

    (testing "Dispatches to create type specific  property value"
      (is (= [sut/create-type-specific-property-value! value-type property-value-data ::db.core/<]
             (-> @db-effects first second))))

    (testing "Calls get properties-values"
      (is (= [::get {:property (get-property 9) :ticket ticket}] result)))))
