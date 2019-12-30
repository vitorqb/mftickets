(ns mftickets.domain.tickets-test
  (:require [clojure.test :as t :refer [are deftest is testing use-fixtures]]
            [mftickets.db.core :as db.core]
            [mftickets.db.tickets :as db.tickets]
            [mftickets.domain.tickets :as sut]
            [mftickets.domain.tickets.inject :as domain.tickets.inject]
            [mftickets.domain.tickets.properties-values :as properties-values]
            [mftickets.test-utils :as tu]))

(use-fixtures :once tu/common-fixture)

(deftest test-get-ticket

  (testing "Dispatches to get-raw-ticket and get-properties-values"
    (let [ticket {:id 1}
          get-raw-ticket {(:id ticket) ticket}

          properties [{:id 3} {:id 4}]
          m-get-properties-for-ticket {ticket properties}

          properties-values [{:id 5} {:id 6}]
          m-get-properties-values (fn [{ticket* :ticket properties* :properties}]
                                    (and (= ticket ticket*)
                                         (= properties* properties)
                                         properties-values))

          inject {::domain.tickets.inject/get-properties-for-ticket m-get-properties-for-ticket}]

      (with-redefs [db.tickets/get-raw-ticket get-raw-ticket
                    properties-values/get-properties-values m-get-properties-values]

        (is (= (assoc ticket :properties-values properties-values)
               (sut/get-ticket inject (:id ticket))))))))

(deftest test-create-ticket-properties-values!

  (let [run-effects-calls (atom [])
        run-effects (fn [& xs] (swap! run-effects-calls conj xs))
        get-ticket (constantly ::get-ticket)
        create-property-value! (fn [_ _] nil)
        inject {::domain.tickets.inject/create-property-value! create-property-value!}
        property-value-1 {:id nil
                          :property-id 1
                          :value "This is a text"}
        property-value-2 {:id nil
                          :property-id 2
                          :value "This is a date"}
        properties-values [property-value-1 property-value-2]
        ticket-data {:id 1}
        assoc-ticket-id #(assoc % :ticket-id (:id ticket-data))]

    (with-redefs [db.core/run-effects! run-effects
                  sut/get-ticket get-ticket]

      (let [result (#'sut/create-ticket-properties-values! inject properties-values ticket-data)]

        (testing "Delegates to injected create-property-value!"
          (is (= [[[create-property-value! inject (assoc-ticket-id property-value-1)]
                   [create-property-value! inject (assoc-ticket-id property-value-2)]]]
                 @run-effects-calls)))

        (testing "Returns the full ticket by `get-ticket`."
          (is (= result ::get-ticket)))))))

(deftest test-create-ticket!

  (testing "Delegates to db.create-raw-ticket!"
    (let [inject {::inject 1} ticket-data {::ticket-data 1 :properties-values ::vals}]
      (with-redefs [db.core/run-effects! (fn [& xs] xs)]
        (is (= [[db.tickets/create-raw-ticket! ticket-data]
                [@#'sut/create-ticket-properties-values! inject ::vals ::db.core/<]]
               (sut/create-ticket! inject ticket-data)))))))
