(ns mftickets.db.core-test
  (:require [clojure.test :as t :refer [are deftest is testing use-fixtures]]
            [mftickets.db.core :as sut]
            [mftickets.middleware.pagination :as middleware.pagination]
            [mftickets.test-utils :as tu]))

(use-fixtures :once tu/common-fixture)

(deftest test-parse-pagination-data

  (testing "No params"
    (is (= nil (sut/parse-pagination-data {})))
    (is (= nil (sut/parse-pagination-data nil))))

  (testing "WIth params"
    (let [page-size 5
          page-number 3
          pagination-data #::middleware.pagination{:page-size page-size
                                                   :page-number page-number}]
      
      (is (= {:offset (-> page-number dec (* page-size)) :limit page-size}
             (sut/parse-pagination-data pagination-data))))))

(deftest test-parse-string-match

  (testing "nil"
    (is (nil? (sut/parse-string-match nil))))

  (testing "No spaces"
    (is (= "%foo%" (sut/parse-string-match "foo"))))

  (testing "Spaces replaces by %"
    (is (= "%foo%bar%" (sut/parse-string-match "foo bar")))))
