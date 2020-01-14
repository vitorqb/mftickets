(ns mftickets.domain.templates.properties.create-test
  (:require [clojure.test :as t :refer [are deftest is testing use-fixtures]]
            [mftickets.db.core :as db.core]
            [mftickets.db.templates.properties :as db.templates.properties]
            [mftickets.domain.templates.properties.create :as sut]
            [mftickets.test-utils :as tu]))

(use-fixtures :once tu/common-fixture)

(deftest test-create-and-merge-type-specific-property!
  (let [property-data {::foo 1}
        created-generic-property {::bar 1}
        created-type-specific {::baz 1}
        result (with-redefs [sut/create-type-specific-property! #(do %& created-type-specific)]
                 (@#'sut/create-and-merge-type-specific-property!
                  property-data
                  created-generic-property))]
    (is (= result (merge created-generic-property created-type-specific)))))

(deftest test-create-property
  (with-redefs [db.core/run-effects! (fn [& xs] xs)]
    (let [property {:id 1}
          [r1 r2] (sut/create-property! property)]

      (testing "Creates property"
        (is (= [db.templates.properties/create-generic-property! property] r1)))

      (testing "Creates type-specific property"
        (is (= [@#'sut/create-and-merge-type-specific-property! property ::db.core/<] r2))))))
