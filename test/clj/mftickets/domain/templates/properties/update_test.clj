(ns mftickets.domain.templates.properties.update-test
  (:require [clojure.test :as t :refer [are deftest is testing use-fixtures]]
            [mftickets.db.templates.properties :as db.properties]
            [mftickets.domain.templates.properties.get :as domain.properties.get]
            [mftickets.domain.templates.properties.update :as sut]
            [mftickets.test-utils :as tu]))

(deftest test-update-property-integration

  (tu/with-db
    (let [property
          (tu/gen-save! tu/template-section-property
                        {:value-type :templates.properties.types/radio})

          radio-opts
          (tu/gen-save! tu/template-section-property-radio-option
                        {:property-id (:id property)})
          
          new-name
          "AAAAA"

          new-radio-opts
          [{:id 1 :value "FOOOO" :property-id (:id property)}]

          updated-property
          (assoc property :name "AAAA" :templates.properties.types.radio/options new-radio-opts)]

      (sut/update-property! updated-property)
      (is (= updated-property
             (domain.properties.get/get-property (:id property)))))))
