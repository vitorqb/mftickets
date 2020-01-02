(ns mftickets.utils.transform-test
  (:require [clojure.test :as t :refer [are deftest is testing use-fixtures]]
            [mftickets.test-utils :as tu]
            [mftickets.utils.transform :as sut]))

(use-fixtures :once tu/common-fixture)

(deftest test-remapkey
  (is (= {:b 1}
         (sut/remapkey {:a 1} :a :b)))
  (is (= {}
         (sut/remapkey {} :a :b))))
