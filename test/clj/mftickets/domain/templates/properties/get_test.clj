(ns mftickets.domain.templates.properties.get-test
  (:require [clojure.test :as t :refer [are deftest is testing use-fixtures]]
            [mftickets.db.templates.properties :as db.properties]
            [mftickets.domain.templates.properties.get :as sut]
            [mftickets.test-utils :as tu]))

(deftest test-get-property
  (let [generic-property {:id 1 :value-type ::bar}
        type-specific-property {:value "FOO"}]
    (with-redefs [db.properties/get-generic-property (constantly generic-property)
                  sut/get-type-specific-property (constantly type-specific-property)]
      (is (= (merge generic-property type-specific-property)
             (sut/get-property 1))))))

(deftest test-get-properties-for-section
  (let [generic-property {:id 1}
        typed-property {::value "Foo"}]
    (with-redefs [db.properties/get-generic-properties-for-section (constantly [generic-property])
                  sut/get-type-specific-property (constantly typed-property)]
      (is (= [(merge generic-property typed-property)]
             (sut/get-properties-for-section {:id 1}))))))

(deftest test-get-properties-for-template
  (let [property1 {:id 1 :value-type :template.properties.types/date}
        property2 {:id 2 :value-type :template.properties.types/radio}
        get-generic-properties-for-template (constantly [property1 property2])
        radio-opts {:value "Foo"}
        radio-data {:templates.properties.types.radio/options radio-opts}
        get-type-specific-property #(case (:value-type %)
                                      :template.properties.types/radio radio-data
                                      nil)]
    (with-redefs [db.properties/get-generic-properties-for-template
                  get-generic-properties-for-template

                  sut/get-type-specific-property
                  get-type-specific-property]
      (is (= [property1 (merge property2 radio-data)]
             (sut/get-properties-for-template {:id 1}))))))
