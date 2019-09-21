(ns mftickets.db.templates.properties-test
  (:require [mftickets.db.templates.properties :as sut]
            [clojure.test :as t :refer [is are deftest testing use-fixtures]]
            [mftickets.test-utils :as tu]))

(deftest test-get-properties-for-template

  (testing "Empty"
    (tu/with-db
      (is (= [] (sut/get-properties-for-template {:id 1})))))

  (testing "Base"
    (tu/with-db
      (tu/gen-save! tu/template-section {:id 1})
      (let [template-section-property (tu/gen-save! tu/template-section-property
                                                    {:template-section-id 1})]
        (is (= [template-section-property]
               (sut/get-properties-for-template {:id 9})))))))

(deftest test-get-properties-for-templates-ids

  (testing "Empty"
    (tu/with-db
      (is (= [] (sut/get-properties-for-templates-ids (range 0 10))))))

  (testing "Base"
    (tu/with-db
      ;; Adds two template sections for the same template
      (tu/gen-save! tu/template-section {:id 1 :template-id 2})
      (tu/gen-save! tu/template-section {:id 2 :template-id 2})

      ;; An unrelated one
      (tu/gen-save! tu/template-section {:id 777 :template-id 666})

      ;; An unrelated property
      (tu/gen-save! tu/template-section-property {:id 999 :template-section-id 999})

      ;; And three properties for the sections
      (let [properties
            [(tu/gen-save! tu/template-section-property {:id 1 :template-section-id 1})
             (tu/gen-save! tu/template-section-property {:id 2 :template-section-id 1})
             (tu/gen-save! tu/template-section-property {:id 3 :template-section-id 2})]]

        (is (= [] (sut/get-properties-for-templates-ids [])))
        (is (= [] (sut/get-properties-for-templates-ids [888])))
        (is (= properties (sut/get-properties-for-templates-ids [0 1 2 3])))))))
