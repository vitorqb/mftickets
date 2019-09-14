(ns mftickets.routes.services.login-test
  (:require [mftickets.routes.services.login :as sut]
            [muuntaja.core :as m]
            [ring.util.http-response :as http-response]
            [ring.mock.request :as mock.request]
            [clojure.test :as t :refer [is are deftest testing use-fixtures]]
            [mftickets.routes.services.helpers :as services.helpers]
            [mftickets.test-utils :as test-utils]
            [mftickets.handler :refer [app]]
            [mftickets.domain.users :as domain.users]
            [mftickets.domain.login :as domain.login]
            [mftickets.utils.emails :as utils.emails]))

(deftest test-handle-send-user-key

  (testing "Base test"
    (test-utils/with-app
      (test-utils/with-db
        (with-redefs [utils.emails/send-email! (constantly nil)]
          (let [resp (-> (mock.request/request :post "/api/login/send-key")
                         (mock.request/json-body {:email "foo@bar.com"})
                         ((app)))]
            (is (= 204 (:status resp)))))))))

(deftest test-handle-get-token
  (let [handle-get-token #'sut/handle-get-token
        invalid-key-bad-request #'sut/invalid-key-bad-request]

    (testing "Valid user and key..."
      (with-redefs [domain.users/get-user (constantly {:user-id 999})
                    domain.login/create-user-token! (constantly {:value ::token})]
        (let [request {:parameters {:body {:email "foo" :keyValue "bar"}}}
              response (handle-get-token request)]

          (testing "Returns created token in body."
            (is (= {:token ::token} (:body response))))

          (testing "Returns 200"
            (is (= 200 (:status response))))

          (testing "Returns session with token"
            (is (= {:token ::token} (:session response)))))))

    (testing "Returns invalid key if invalid keys"
      (with-redefs [domain.users/get-user (constantly {:user-id 999})
                    domain.login/create-user-token! (constantly ::domain.login/invalid-user-key)]
        (let [request {:parameters {:body {:email "foo" :keyValue "bar"}}}
              response (handle-get-token request)]
          (is (= (invalid-key-bad-request) response)))))

    (testing "Returns invalid user response if invalid user"
      (with-redefs [domain.users/get-user (constantly nil)]
        (is (= (services.helpers/unknown-user-bad-request)
               (handle-get-token {:parameters {:body {:email "foo" :keyValue "bar"}}})))))))

(deftest test-handle-get-token-from-cookie
  (let [handle-get-token-from-cookie #'sut/handle-get-token-from-cookie]

    (let [request {:session {:token "foo"}}]

      (testing "When token exists and is valid, returns it."
        (with-redefs [domain.login/is-valid-token-value? (constantly true)]
          (is (= {:body {:token {:value "foo"}}}
                 (handle-get-token-from-cookie request)))))

      (testing "When token exists but is not valid, returns nil"
        (with-redefs [domain.login/is-valid-token-value? (constantly false)]
          (is (= {:body nil}
                 (handle-get-token-from-cookie request))))))

    (testing "When token does not exist, returns nil"
      (let [request {:session {}}]
        (is (= {:body nil}
               (handle-get-token-from-cookie request)))))))
