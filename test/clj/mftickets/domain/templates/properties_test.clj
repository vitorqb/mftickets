(ns mftickets.domain.templates.properties-test
  (:require [clojure.test :as t :refer [are deftest is testing use-fixtures]]
            [mftickets.db.templates.properties :as db.templates.properties]
            [mftickets.domain.templates.properties :as sut]
            [mftickets.domain.templates.properties.get :as domain.properties.get]
            [mftickets.test-utils :as tu]))

(use-fixtures :once tu/common-fixture)

(deftest test-get-property-types
  (is (= #{:templates.properties.types/radio
           :templates.properties.types/text
           :templates.properties.types/date}
         (sut/get-property-types))))

(deftest test-get-properties-ids-set-for-template-id

  (let [properties [{:id 1}]
        template {:id 1}
        get-properties-for-template {template properties}]
    (with-redefs [domain.properties.get/get-properties-for-template get-properties-for-template]
      (is (= #{1} (sut/get-properties-ids-set-for-template-id (:id template)))))))

(deftest test-delete-property!

  (with-redefs [db.templates.properties/delete-property! (fn [x] [::delete-property x])]
    (let [property {:id 1}]
      (is (= [::delete-property property] (sut/delete-property! property))))))
