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
