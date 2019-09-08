(ns mftickets.utils.transform-test
  (:require [mftickets.utils.transform :as sut]
            [clojure.test :as t :refer [is are deftest testing use-fixtures]]))

(deftest test-remapkey
  (is (= {:b 1}
         (sut/remapkey {:a 1} :a :b)))
  (is (= {}
         (sut/remapkey {} :a :b))))
