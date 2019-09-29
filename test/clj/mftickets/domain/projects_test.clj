(ns mftickets.domain.projects-test
  (:require [clojure.test :as t :refer [are deftest is testing use-fixtures]]
            [mftickets.db.core :as db.core]
            [mftickets.db.projects :as db.projects]
            [mftickets.domain.projects :as sut]
            [mftickets.test-utils :as tu]))

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

(deftest test-create-project!

  (testing "Base"
    (with-redefs [db.core/run-effects! (fn [& xs] xs)]
      (is (= [[db.projects/create-project! {:name "N" :description "D"}]
              [:id ::db.core/<]
              [db.projects/assign-user! {:user-id 1 :project-id ::db.core/<}]
              [:project-id ::db.core/<]
              [db.projects/get-project {:id ::db.core/<}]]
             (sut/create-project! {:name "N" :description "D" :user {:id 1}})))))

  (testing "Integration: "
    (tu/with-db
      (let [user (tu/gen-save! tu/user)
            project (sut/create-project! {:name "N" :description "D" :user user})]

        (testing "Writes to project table"
          (is (= 1
                 (tu/count! "FROM projects WHERE name=? AND description=?" "N" "D"))))

        (testing "Writes to usersProjects table"
          (is (= 1
                 (tu/count! "FROM usersProjects WHERE userId=? AND projectId=?"
                            (:id user)
                            (:id project)))))))))
