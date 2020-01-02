(ns mftickets.db.login-test
  (:require [mftickets.db.login :as sut]
            [clojure.test :as t :refer [is are deftest testing use-fixtures]]
            [clojure.java.jdbc :as jdbc]
            [mftickets.utils.date-time :as utils.date-time]
            [mftickets.db.core :as db.core]
            [mftickets.test-utils :as tu]))

(use-fixtures :once tu/common-fixture)

(deftest test-create-user-key!

  (testing "Integration"
    (tu/with-db
      (with-redefs [utils.date-time/now-as-str (constantly "1993-11-23T11:30:25")]
        (let [user-key {:user-id 88 :value "foo"}
              response (sut/create-user-key! user-key)]
          (is (int? (:id response)))
          (is (= "1993-11-23T11:30:25" (:created-at response)))
          (is (= 88 (:user-id response)))
          (is (= "foo" (:value response))))))))

(deftest test-create-and-retrieve-token

  (testing "Integration"
    (tu/with-db
      (with-redefs [utils.date-time/now-as-str (constantly "1993-11-23T11:30:25")]
        (let [user-token {:user-id 9 :value "foo"}
              response (sut/create-user-token! user-token)]
          (is (int? (:id response)))
          (is (= "1993-11-23T11:30:25" (:created-at response)))
          (is (= 9 (:user-id response)))
          (is (= "foo" (:value response)))
          (is (false? (:has-been-invalidated response))))))))

(deftest test-is-valid-user-key?

  (testing "True"
    (tu/with-db
      (let [user-key {:value "foo" :user-id 1}]
        (sut/create-user-key! user-key)
        (is (true? (sut/is-valid-user-key? user-key))))))

  (testing "False"
    (tu/with-db
      (is (nil? (sut/is-valid-user-key? {:value "bar" :user-id 1}))))))

(deftest test-is-valid-token-value?

  (testing "When token does not exists"
    (tu/with-db
      (is (= nil (sut/is-valid-token-value? "foo")))))

  (testing "When token has expired"
    (tu/with-db
      (let [{:keys [value]} (tu/gen-save! tu/user-login-token {:has-been-invalidated true})]
        (is (false? (sut/is-valid-token-value? value))))))

  (testing "When token exists and is valid"
    (tu/with-db
      (let [{:keys [value]} (tu/gen-save! tu/user-login-token {:has-been-invalidated false})]
        (is (true? (sut/is-valid-token-value? value)))))))
