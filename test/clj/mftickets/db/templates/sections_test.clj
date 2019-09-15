(ns mftickets.db.templates.sections-test
  (:require [mftickets.db.templates.sections :as sut]
            [clojure.test :as t :refer [is are deftest testing use-fixtures]]
            [mftickets.test-utils :as test-utils]))

(deftest test-get-sections-for-template

  (test-utils/with-db
    (test-utils/insert! :templateSections {:id 1 :templateId 2 :name "Foo"})
    (test-utils/insert! :templateSections {:id 2 :templateId 2 :name "Bar"})
    (test-utils/insert! :templateSections {:id 11 :templateId 12 :name "Baz"})
    
    (testing "When exists"
      (is (= [{:id 1 :template-id 2 :name "Foo"}
              {:id 2 :template-id 2 :name "Bar"}]
             (sut/get-sections-for-template {:id 2}))))

    (testing "When does not exist"
      (is (= '()
             (sut/get-sections-for-template {:id 9}))))))
