(ns mftickets.utils.kw-test
  (:require [clojure.test :as t :refer [are deftest is testing use-fixtures]]
            [mftickets.test-utils :as tu]
            [mftickets.utils.kw :as sut]))

(use-fixtures :once tu/common-fixture)

(deftest test-full-name
  (testing "With ns"
    (is (= "mftickets.utils.kw-test/foo" (sut/full-name ::foo))))
  (testing "No ns"
    (is (= "foo" (sut/full-name :foo)))))
