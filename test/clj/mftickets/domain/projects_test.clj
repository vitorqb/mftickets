(ns mftickets.domain.projects-test
  (:require [mftickets.domain.projects :as sut]
            [mftickets.test-utils :as tu]
            [clojure.test :as t :refer [is are deftest testing use-fixtures]]
            [mftickets.db.projects :as db.projects]))

(deftest test-get-project

  (tu/with-db

    (testing "Not found"
      (is (nil? (sut/get-project {:id 999}))))

    (testing "Found"
      (let [project (tu/gen-save! tu/project {:id 888})]
        (is (= project (sut/get-project 888)))))))

(deftest test-get-projects-for-user
  (let [user {:id 99}
        projects [{:id 9} {:id 8}]]
    (with-redefs [db.projects/get-projects-for-user #(if (= % {:user-id (:id user)})
                                                       projects)]
      (is (= (sut/get-projects-for-user user) projects)))))
