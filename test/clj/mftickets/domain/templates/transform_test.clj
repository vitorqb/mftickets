(ns mftickets.domain.templates.transform-test
  (:require [clojure.test :as t :refer [are deftest is testing use-fixtures]]
            [mftickets.domain.templates.transform :as sut]
            [mftickets.test-utils :as tu]))

(use-fixtures :once tu/common-fixture)

(deftest test-set-section-order

  (testing "Section not found"
    (let [template {:id 1}]
      (is (= template (sut/set-section-order template 1 1)))))

  (testing "Base"
    (let [section-id 2
          new-order 999
          template {:id 1 :sections [{:id section-id :order 0} {:id 3 :order 1}]}
          exp-template (assoc-in template [:sections 0 :order] new-order)]
      (is (= exp-template (sut/set-section-order template section-id new-order))))))

(deftest test-get-section-order

  (testing "Nil"
    (let [template {:sections []}]
      (is (nil? (sut/get-section-order template 1)))))

  (testing "Nil (no sections)"
    (let [template {}]
      (is (nil? (sut/get-section-order template 1)))))

  (testing "Base"
    (let [order 999
          section-id 888
          template {:sections [{:id 1 :order 2} {:id section-id :order order}]}]
      (is (= order (sut/get-section-order template section-id))))))
