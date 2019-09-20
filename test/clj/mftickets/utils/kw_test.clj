(ns mftickets.utils.kw-test
  (:require [mftickets.utils.kw :as sut]
            [clojure.test :as t :refer [is are deftest testing use-fixtures]]))

(deftest test-full-name
  (testing "With ns"
    (is (= "mftickets.utils.kw-test/foo" (sut/full-name ::foo))))
  (testing "No ns"
    (is (= "foo" (sut/full-name :foo)))))
