(ns mftickets.domain.users-test
  (:require [mftickets.domain.users :as sut]
            [mftickets.test-utils :as test-utils]
            [clojure.test :as t :refer [is are deftest testing use-fixtures]]))

(deftest test-create-user!

  (testing "Creates and gets an user"
    (test-utils/with-db
      (let [user (sut/create-user! {:email "foo"})]
        (is (int? (:id user)))
        (is (= "foo" (:email user)))))))

(deftest test-get-or-create-user!

  (testing "Returns user if exists"
    (with-redefs [sut/get-user (constantly ::user)]
      (is (= ::user (sut/get-or-create-user! {})))))

  (testing "Returns new user if does not exist"
    (with-redefs [sut/get-user (constantly nil)
                  sut/create-user! (constantly ::new-user)]
      (is (= ::new-user (sut/get-or-create-user! {}))))))
