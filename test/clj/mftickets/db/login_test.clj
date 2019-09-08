(ns mftickets.db.login-test
  (:require [mftickets.db.login :as sut]
            [clojure.test :as t :refer [is are deftest testing use-fixtures]]
            [mftickets.utils.date-time :as utils.date-time]
            [mftickets.db.core :as db.core]
            [mftickets.test-utils :as test-utils]))

(deftest test-create-user-key!

  (testing "Integration"
    (test-utils/with-db
      (with-redefs [utils.date-time/now-as-str (constantly "1993-11-23T11:30:25")]
        (let [user-key {:user-id 88 :value "foo"}
              response (sut/create-user-key! user-key)]
          (is (int? (:id response)))
          (is (= "1993-11-23T11:30:25" (:created-at response)))
          (is (= 88 (:user-id response)))
          (is (= "foo" (:value response))))))))
