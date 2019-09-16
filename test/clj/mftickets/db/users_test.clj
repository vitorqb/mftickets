(ns mftickets.db.users-test
  (:require [mftickets.db.users :as sut]
            [mftickets.test-utils :as test-utils]
            [clojure.test :as t :refer [is are deftest testing use-fixtures]]))

(deftest test-get-project-ids-for-user

  (test-utils/with-db
    (test-utils/insert! :usersProjects {:userId 1 :projectId 1})
    (test-utils/insert! :usersProjects {:userId 1 :projectId 2})
    (test-utils/insert! :usersProjects {:userId 2 :projectId 3})

    (testing "Two projects"
      (is (= (sut/get-projects-ids-for-user {:id 1}) #{1 2})))

    (testing "One project"
      (is (= (sut/get-projects-ids-for-user {:id 2}) #{3})))

    (testing "No project"
      (is (= (sut/get-projects-ids-for-user {:id 3}) #{})))))
