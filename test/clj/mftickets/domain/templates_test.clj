(ns mftickets.domain.templates-test
  (:require [mftickets.domain.templates :as sut]
            [clojure.test :as t :refer [is are deftest testing use-fixtures]]
            [mftickets.test-utils :as tu]
            [mftickets.db.core :as db.core]
            [clojure.java.jdbc :as jdbc]))

(deftest test-get-raw-template

  (testing "Base"
    (tu/with-db
      (let [template (tu/gen-save! tu/template {})]
        (is (= template (sut/get-raw-template 1)))))))

(deftest test-get-raw-templates-for-project

  (testing "Base"
    (tu/with-db
      (let [templates [(tu/gen-save! tu/template {:id 1 :project-id 1})
                       (tu/gen-save! tu/template {:id 2 :project-id 1})
                       (tu/gen-save! tu/template {:id 3 :project-id 2})]]
        (is (= (take 2 templates)
               (sut/get-raw-templates-for-project {:id 1})))
        (is (= [(last templates)]
               (sut/get-raw-templates-for-project {:id 2})))
        (is (= []
               (sut/get-raw-templates-for-project {:id 3})))))))

(deftest test-get-projects-ids-for-template

  (testing "Base"
    (tu/with-db
      (tu/gen-save! tu/template {:id 2 :projectId 1})

      (testing "Existing" 
        (is (= #{1} (sut/get-projects-ids-for-template {:id 2}))))

      (testing "Non existing" 
        (is (= #{} (sut/get-projects-ids-for-template {:id 3})))))))

(deftest test-test-assoc-property-to-template

  (testing "Not found"
    (let [property {:id 1 :template-section-id 2}
          sections [{:id 3}]
          template {:sections sections}]
      (is (= template (sut/assoc-property-to-template template property)))))

  (testing "Base"
    (let [property {:id 10 :template-section-id 2}
          sections [{:id 1} {:id 2 :properties [{:id 11 :template-section-id 2}]}]
          template {:sections sections}]
      (is (= {:sections [{:id 1} {:id 2 :properties [{:id 11 :template-section-id 2} property]}]}
             (sut/assoc-property-to-template template property))))))
