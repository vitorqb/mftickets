(ns mftickets.db.templates.properties-test
  (:require [mftickets.db.templates.properties :as sut]
            [clojure.test :as t :refer [is are deftest testing use-fixtures]]
            [mftickets.test-utils :as test-utils]))

(deftest test-get-properties-for-template

  (testing "Empty"
    (test-utils/with-db
      (is (= [] (sut/get-properties-for-template {:id 1})))))

  (testing "Base"
    (test-utils/with-db
      (test-utils/insert! :templateSections {:id 1 :templateId 9 :name "Foo"})
      (test-utils/insert! :templateSectionProperties
                          {:id 1
                           :templateSectionId 1
                           :name "Bar"
                           :isMultiple false
                           :valueType "section.property.value.types/text"})
      (is (= [{:id 1
               :template-section-id 1
               :name "Bar"
               :is-multiple false
               :value-type :section.property.value.types/text}]
             (sut/get-properties-for-template {:id 9}))))))
