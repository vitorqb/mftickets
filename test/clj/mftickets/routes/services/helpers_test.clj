(ns mftickets.routes.services.helpers-test
  (:require [mftickets.routes.services.helpers :as sut]
            [clojure.test :as t :refer [is are deftest testing use-fixtures]]
            [mftickets.domain.login :as domain.login]
            [mftickets.domain.users :as domain.users]))

(deftest test-if-let-user

  (testing "false"
    (is (= (sut/unknown-user-bad-request)
           (sut/if-let-user [user nil]
             user))))

  (testing "true"
    (is (= "FOO"
           (sut/if-let-user [user "FOO"]
             user)))))

(deftest test-parse-raw-token-value

  (testing "Valid"
    (is (= "foo" (sut/parse-raw-token-value "Bearer foo"))))

  (testing "Invalid"
    (is (nil? (sut/parse-raw-token-value "foo")))))

(deftest test-token->user-or-err

  (testing "Retrieves user"
    (let [user {:id 999}]
      (with-redefs [domain.login/get-user-id-from-token-value (constantly (:id user))
                    domain.users/get-user-by-id (constantly user)]
        (is (= [:mftickets.auth/valid user]
               (sut/token->user-or-err "Bearer foobarbaz"))))))

  (testing "Error"
    (with-redefs [domain.login/get-user-id-from-token-value (constantly nil)]
      (is (= :mftickets.auth/invalid
             (sut/token->user-or-err "INVALID TOKEN"))))))
