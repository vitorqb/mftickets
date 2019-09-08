(ns mftickets.domain.login-test
  (:require [mftickets.domain.login :as sut]
            [clojure.test :as t :refer [is are deftest testing use-fixtures]]
            [mftickets.db.core :as db.core]
            [mftickets.db.login :as db.login]
            [mftickets.utils.emails :as utils.emails]
            [mftickets.test-utils :as test-utils]))

(deftest test-generate-random-key-value
  (with-redefs [rand-nth (constantly 1)]
    (is (= (apply str (repeat sut/key-value-length 1))
           (sut/generate-random-key-value)))))

(deftest test-create-user-key!
  (with-redefs [db.core/run-effects! (fn [& xs] xs)]

    (let [user-id 1
          key "foo"
          effects (sut/create-user-key! {:user-id 1 :value key})]

      (testing "Invalidates user keys before"
        (let [[[fun & args] & _] effects]
          (is (= db.login/invalidate-user-keys! fun))
          (is (= [{:user-id user-id}] args))))

      (testing "Calls for creation of new key"
        (let [[_ [fun & args]] effects]
          (is (= db.login/create-user-key! fun))
          (is (= [{:user-id user-id :value key}] args)))))))

(deftest test-send-key!
  (with-redefs [utils.emails/send-email! identity]

    (testing "Calls send-email! with correct args"
      (let [email ::email
            user-key {:value ::value}
            text-body (sut/send-key-email-text-body (:value user-key))]
        (is (= {:email email :text-body text-body}
               (sut/send-key! {:email email :user-key user-key})))))))

(deftest test-create-user-token!
  (with-redefs [db.core/run-effects! (fn [& xs] xs)]

    (testing "Returns invalid user key if key is not found or invalid"
      (with-redefs [sut/is-valid-user-key? (constantly false)]
        (is (= ::sut/invalid-user-key
               (sut/create-user-token! {:user-key {:user-id 999 :value "foo"}})))))

    (testing "Calls invalidate-user-keys!"
      (with-redefs [sut/is-valid-user-key? (constantly true)]
        (let [user-key {:user-id 999 :value "foo"}
              effects (sut/create-user-token! {:user-key user-key})
              [[fn & args] & _] effects]
          (is (= fn db.login/invalidate-user-keys!))
          (is (= args [{:user-id 999}])))))

    (testing "Calls db create-user-token!"
      (with-redefs [sut/is-valid-user-key? (constantly true)]
        (let [user-key {:user-id 999 :value "foo"}
              effects (sut/create-user-token! {:user-key user-key :token-value "bar"})
              [_ [fn & args] & _] effects]
          (is (= fn db.login/create-user-token!))
          (is (= args [{:user-id 999 :value "bar"}])))))))

(deftest test-get-user-from-token-value

  (testing "Valid token"
    (test-utils/with-db
      (let [user-key (sut/create-user-key! {:user-id 1 :value "foo"})]
        (sut/create-user-token! {:user-key user-key :token-value "bar"})
        (is (= 1 (sut/get-user-id-from-token-value "bar"))))))

  (testing "Unexistant token"
    (test-utils/with-db
      (is (nil? (sut/get-user-id-from-token-value "foo"))))))
