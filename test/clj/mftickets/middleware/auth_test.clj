(ns mftickets.middleware.auth-test
  (:require [clojure.test :as t :refer [are deftest is testing use-fixtures]]
            [mftickets.middleware.auth :as sut]
            [mftickets.test-utils :as tu]))

(use-fixtures :once tu/common-fixture)

(deftest test-wrap-auth

  (testing "Invalid"
    (let [handler (sut/wrap-auth identity (constantly :mftickets.auth/invalid))]
      (is (= (sut/unauthorized-response)
             (handler {})))))

  (testing "Valid"
    (let [token->user-or-err #(when (= % ::token) [:mftickets.auth/valid ::user])
          request {:headers {"authorization" ::token}}
          handler (sut/wrap-auth identity token->user-or-err)]
      (is (= (assoc request :mftickets.auth/user ::user)
             (handler request))))))
