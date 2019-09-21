(ns mftickets.domain.templates.properties-test
  (:require [mftickets.domain.templates.properties :as sut]
            [clojure.test :as t :refer [is are deftest testing use-fixtures]]
            [mftickets.db.templates.properties :as db.templates.properties]))

(deftest test-get-properties-for-template

  (with-redefs [db.templates.properties/get-properties-for-template identity]
    (is (= {:id 1} (sut/get-properties-for-template {:id 1})))))

(deftest test-properties-getter

  (with-redefs [db.templates.properties/get-properties-for-templates-ids
                #(when (= % [1 3])
                   [{:id 1 :template-section-id 2} {:id 3 :template-section-id 4}])]
    (let [templates [{:id 1 :sections [{:id 2}]} {:id 3 :sections [{:id 4}]}]
          getter (sut/properties-getter templates)]

      (is (= [{:id 1 :template-section-id 2}] (getter (first templates))))
      (is (= [{:id 3 :template-section-id 4}] (getter (second templates))))
      (is (= [] (getter {:id 999 :sections []}))))))
