(ns mftickets.routes.services.login-test
  (:require [mftickets.routes.services.login :as sut]
            [muuntaja.core :as m]
            [ring.mock.request :as mock.request]
            [clojure.test :as t :refer [is are deftest testing use-fixtures]]
            [mftickets.test-utils :as test-utils]
            [mftickets.handler :refer [app]]
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
