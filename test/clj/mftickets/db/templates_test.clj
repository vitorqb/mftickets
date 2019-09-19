(ns mftickets.db.templates-test
  (:require [mftickets.db.templates :as sut]
            [clojure.test :as t :refer [is are deftest testing use-fixtures]]
            [mftickets.test-utils :as tu]
            [clojure.java.jdbc :as jdbc]
            [mftickets.db.core :as db.core]))

(deftest test-get-raw-template

  (testing "When exists"
    (tu/with-db
      (let [template (tu/gen-save! tu/template {:id 1})]
        (is (= template (sut/get-raw-template {:id 1})))
        (is (nil? (sut/get-raw-template {:id 2})))))))
