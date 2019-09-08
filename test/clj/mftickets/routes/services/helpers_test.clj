(ns mftickets.routes.services.helpers-test
  (:require [mftickets.routes.services.helpers :as sut]
            [clojure.test :as t :refer [is are deftest testing use-fixtures]]))

(deftest test-if-let-user

  (testing "false"
    (is (= (sut/unknown-user-bad-request)
           (sut/if-let-user [user nil]
             user))))

  (testing "true"
    (is (= "FOO"
           (sut/if-let-user [user "FOO"]
             user)))))
