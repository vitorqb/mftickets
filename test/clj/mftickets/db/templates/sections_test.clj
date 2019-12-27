(ns mftickets.db.templates.sections-test
  (:require [mftickets.db.templates.sections :as sut]
            [clojure.test :as t :refer [is are deftest testing use-fixtures]]
            [mftickets.test-utils :as tu]))

(deftest test-get-sections-for-template

  (tu/with-db
    (let [template-sections-for-template-2
          [(tu/gen-save! tu/template-section {:id 1 :name "Foo" :template-id 2})
           (tu/gen-save! tu/template-section {:id 2 :name "Bar" :template-id 2})]

          template-section-for-other-templates
          [(tu/gen-save! tu/template-section {:id 3 :name "Baz" :template-id 3})]]
    
      (testing "When exists"
        (is (= template-sections-for-template-2 (sut/get-sections-for-template {:id 2}))))

      (testing "When does not exist"
        (is (= '() (sut/get-sections-for-template {:id 9})))))))

(deftest test-get-sections-for-templates-ids

  (tu/with-db
    (let [template-sections-for-template-1
          [(tu/gen-save! tu/template-section {:id 1 :template-id 1})
           (tu/gen-save! tu/template-section {:id 2 :template-id 1})]

          template-sections-for-template-2
          [(tu/gen-save! tu/template-section {:id 3 :template-id 2})
           (tu/gen-save! tu/template-section {:id 4 :template-id 2})]

          template-sections-for-other-templates
          [(tu/gen-save! tu/template-section {:id 5 :template-id 200})
           (tu/gen-save! tu/template-section {:id 6 :template-id 200})]]

      (testing "Empty list"
        (is (= (sut/get-sections-for-templates-ids []) [])))

      (testing "Two long"
        (is (= (sut/get-sections-for-templates-ids [1 2])
               (concat template-sections-for-template-1 template-sections-for-template-2)))))))

(deftest test-get-section!
  (tu/with-db
    (let [section (tu/gen-save! tu/template-section {})]
      (is (= section (sut/get-section (:id section)))))))

(deftest test-delete-section!

  (tu/with-db
    (let [section (tu/gen-save! tu/template-section {})]
      (is (= [section] (sut/get-sections-for-template {:id (:template-id section)})))
      (sut/delete-section! section)
      (is (= [] (sut/get-sections-for-template {:id (:template-id section)}))))))

(deftest test-update-raw-section!

  (tu/with-db
    (let [section (tu/gen-save! tu/template-section {})
          new-section {:id (:id section) :template-id 999 :name "Foo Baz 999" :order 123871}
          _ (@#'sut/update-raw-section! new-section)]

      (testing "Name is updated"
        (is (= (:name new-section) (-> section :id sut/get-section :name))))

      (testing "template-id is ignored"
        (is (= (:template-id section) (-> section :id sut/get-section :template-id))))

      (testing "Order is updated"
        (is (= (:order new-section) (-> section :id sut/get-section :order)))))))

(deftest test-create-section!

  (let [section {:template-id 999 :name "Foo Bar Baz" :order 213213}]
    (tu/with-db
      (let [created-section (sut/create-section! section)]
        (is (int? (:id created-section)))
        (is (= section (dissoc created-section :id)))))))
