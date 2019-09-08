(ns mftickets.middleware.auth-test
  (:require [mftickets.middleware.auth :as sut]
            [clojure.test :as t :refer [is are deftest testing use-fixtures]]))

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
