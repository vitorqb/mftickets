(ns mftickets.db.templates.sections-test
  (:require [mftickets.db.templates.sections :as sut]
            [clojure.test :as t :refer [is are deftest testing use-fixtures]]
            [mftickets.test-utils :as tu]))

(deftest test-get-sections-for-template

  (tu/with-db
    (let [template-sections-for-template-2
          [(tu/gen-save! tu/template-section {:id 1 :name "Foo" :template-id 2})
           (tu/gen-save! tu/template-section {:id 2 :name "Bar" :template-id 2})]

          template-section-for-other-templates
          [(tu/gen-save! tu/template-section {:id 3 :name "Baz" :template-id 3})]]
    
      (testing "When exists"
        (is (= template-sections-for-template-2 (sut/get-sections-for-template {:id 2}))))

      (testing "When does not exist"
        (is (= '() (sut/get-sections-for-template {:id 9})))))))
