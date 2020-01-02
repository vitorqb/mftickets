(ns mftickets.db.users-test
  (:require [mftickets.db.users :as sut]
            [mftickets.test-utils :as tu]
            [clojure.test :as t :refer [is are deftest testing use-fixtures]]))

(use-fixtures :once tu/common-fixture)

(deftest test-get-project-ids-for-user

  (tu/with-db
    (tu/gen-save! tu/users-projects {:user-id 1 :project-id 1})
    (tu/gen-save! tu/users-projects {:user-id 1 :project-id 2})
    (tu/gen-save! tu/users-projects {:user-id 2 :project-id 3})

    (testing "Two projects"
      (is (= (sut/get-projects-ids-for-user {:id 1}) #{1 2})))

    (testing "One project"
      (is (= (sut/get-projects-ids-for-user {:id 2}) #{3})))

    (testing "No project"
      (is (= (sut/get-projects-ids-for-user {:id 3}) #{})))))
