(ns mftickets.db.templates.sections-test
  (:require [mftickets.db.templates.sections :as sut]
            [clojure.test :as t :refer [is are deftest testing use-fixtures]]
            [mftickets.test-utils :as test-utils]
            [clojure.java.jdbc :as jdbc]
            [mftickets.db.core :as db.core]))

(deftest test-get-sections-for-template

  (test-utils/with-db
    (jdbc/insert! db.core/*db* :templateSections {:id 1 :templateId 2 :name "Foo"})
    (jdbc/insert! db.core/*db* :templateSections {:id 2 :templateId 2 :name "Bar"})
    (jdbc/insert! db.core/*db* :templateSections {:id 11 :templateId 12 :name "Baz"})
    
    (testing "When exists"
      (is (= [{:id 1 :template-id 2 :name "Foo"}
              {:id 2 :template-id 2 :name "Bar"}]
             (sut/get-sections-for-template {:id 2}))))

    (testing "When does not exist"
      (is (= '()
             (sut/get-sections-for-template {:id 9}))))))
