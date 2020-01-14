(ns mftickets.routes.services.templates.validation.create-test
  (:require [mftickets.routes.services.templates.validation.create :as sut]
            [clojure.test :as t :refer [is are deftest testing use-fixtures]]
            [mftickets.test-utils :as tu]))

(deftest test-get-sections-ids
  (let [template {:sections [{:id 1} {:id 2} {:id nil} {}]}]
    (is (= [1 2 nil nil] (#'sut/get-sections-ids template)))))

(deftest test-get-sections-template-ids
  (let [template {:sections [{:template-id 1} {:template-id 2} {:template-id nil} {}]}]
    (is (= [1 2 nil nil] (#'sut/get-sections-template-ids template)))))

(deftest test-get-properties-ids
  (let [template {:sections [{:properties [{:id 1} {:id 2} {:id nil} {}]}
                             {:properties [{:id 3}]}]}]
    (is (= [1 2 nil nil 3] (#'sut/get-properties-ids template)))))

(deftest test-get-properties-sections-ids
  (let [section1 {:properties [{:template-section-id 1} {:template-section-id nil} {}]}
        section2 {:properties [{:template-section-id 3}]}
        template {:sections [section1 section2]}]
    (is (= [1  nil nil 3] (#'sut/get-properties-sections-ids template)))))

(deftest test-get-radio-options-ids
  (let [radio-opt1 {:id 1}
        radio-opt2 {}
        properties [{:templates.properties.types.radio/options [radio-opt1 radio-opt2]}]
        sections [{:properties properties}]
        template {:sections sections}]
    (is (= [1 nil] (#'sut/get-radio-options-ids template)))))

(deftest test-get-radio-options-properties-ids
  (let [radio-opt1 {:property-id 1}
        radio-opt2 {}
        properties [{:templates.properties.types.radio/options [radio-opt1 radio-opt2]}]
        sections [{:properties properties}]
        template {:sections sections}]
    (is (= [1 nil] (#'sut/get-radio-options-property-ids template)))))
