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

(deftest test-count-templates-for-project

  (testing "No templates"
    (tu/with-db
      (is (= 0 (sut/count-templates-for-project {:project-id 1})))))

  (testing "Two templates"
    (tu/with-db
      (tu/gen-save! tu/template {:id 1 :project-id 1})
      (tu/gen-save! tu/template {:id 2 :project-id 1})
      (is (= 2 (sut/count-templates-for-project {:project-id 1}))))))
