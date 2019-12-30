(ns mftickets.db.tickets-test
  (:require [clojure.test :as t :refer [are deftest is testing use-fixtures]]
            [mftickets.db.tickets :as sut]
            [mftickets.test-utils :as tu]
            [mftickets.utils.date-time :as date-time]))

(use-fixtures :once tu/common-fixture)

(deftest test-get-raw-ticket

  (tu/with-db

    (testing "Nil if not exist"
      (is (nil? (sut/get-raw-ticket 9999))))

    (testing "Base"
      (let [id 1
            template-id 2
            created-at "2018-12-14T19:08:00"
            created-by-user-id 3
            data {:id id
                  :template-id template-id
                  :created-at created-at
                  :created-by-user-id created-by-user-id}
            ticket (tu/gen-save! tu/ticket data)
            result (sut/get-raw-ticket id)]

        (testing "Id"
          (is (= id (:id result))))

        (testing "template-id"
          (is (= template-id (:template-id result))))

        (testing "created-at"
          (is (= created-at (:created-at result))))

        (testing "created-by-user-id"
          (is (= created-by-user-id (:created-by-user-id result))))))))

(deftest test-create-raw-ticket!

  (tu/with-db
    (testing "Base"
      (let [ticket-data
            {:id nil
             :template-id 999
             :created-at nil
             :created-by-user-id 888
             :properties-values
             [{:id nil
               :property-id 777
               :value "This is a text"}]}

            fake-now
            "2019-01-01T12:12:12"

            result
            (with-redefs [date-time/now-as-str (constantly fake-now)]
              (sut/create-raw-ticket! ticket-data))]

        (testing "Returns an id"
          (is (int? (:id result))))

        (testing "Really inserts into the db"
          (is (= 1 (tu/count! "FROM tickets WHERE id=?" (:id result)))))

        (testing "Returns the created object"
          (is (= (sut/get-raw-ticket (:id result)) result)))

        (testing "Returns template-id"
          (is (= (:template-id ticket-data) (:template-id result))))

        (testing "Returns created-at"
          (is (= fake-now (:created-at result))))

        (testing "Returns created-by-user-id"
          (is (= (:created-by-user-id ticket-data) (:created-by-user-id result))))))))
