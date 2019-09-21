(ns mftickets.domain.templates.sections-test
  (:require [mftickets.domain.templates.sections :as sut]
            [clojure.test :as t :refer [is are deftest testing use-fixtures]]
            [mftickets.db.templates.sections :as db.templates.sections]))

(deftest test-get-sections-for-template

  (with-redefs [db.templates.sections/get-sections-for-template identity]
    (is (= {:id 1} (sut/get-sections-for-template {:id 1})))))


(deftest test-sections-getter

  (let [templates [{:id 1} {:id 2}]
        sections [{:id 1 :template-id 1} {:id 2 :template-id 1} {:id 3 :template-id 3}]]
    (with-redefs [db.templates.sections/get-sections-for-templates-ids
                  #(when (= % [1 2]) sections)]
      (let [getter (sut/sections-getter templates)]
        (is (= [{:id 1 :template-id 1} {:id 2 :template-id 1}]
               (getter {:id 1})))
        (is (= []
               (getter {:id 2})))))))
