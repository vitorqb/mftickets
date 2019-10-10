(ns mftickets.db.projects-test
  (:require [mftickets.db.projects :as sut]
            [clojure.test :as t :refer [is are deftest testing use-fixtures]]
            [mftickets.test-utils :as tu]
            [clojure.spec.alpha :as spec]))

(deftest test-get-projects-for-user

  (testing "Base"
    (tu/with-db
      ;; Some user
      (let [user (tu/gen-save! tu/user {:id 1})]

        ;; Adds a project for *OTHER* user
        (tu/gen-save! tu/project {:id 1})
        (tu/gen-save! tu/users-projects {:user-id 2 :project-id 1})
        
        (testing "No projects"
          (is (= [] (sut/get-projects-for-user {:user-id (:id user)}))))

        ;; Adds the same project for the user
        (tu/gen-save! tu/users-projects {:user-id (:id user) :project-id 1})

        ;; And an entry in projects-users but with no project!
        (tu/gen-save! tu/users-projects {:user-id (:id user) :project-id 2})

        (testing "Brings existing projects for the user"
          (is (= [(sut/get-project {:id 1})]
                 (sut/get-projects-for-user {:user-id (:id user)}))))))))


(deftest test-create-project!
  (testing "Base"
    (tu/with-db
      (let [project (sut/create-project! {:name "FF" :description "DD"})]
        (is (= "FF" (:name project)))
        (is (= "DD" (:description project)))
        (is (int? (:id project))))
      (is (= 1 (tu/count! "FROM projects WHERE name=? AND description=?" "FF" "DD"))))))

(deftest test-assign-user!
  (testing "Base"
    (tu/with-db
      (sut/assign-user! {:user-id 1 :project-id 2})
      (is (= 1 (tu/count! "FROM usersProjects WHERE userId=? AND projectId=?" 1 2))))))


(deftest test-update-project!
  (tu/with-db
    (let [old-project (tu/gen-save! tu/project {:id 1 :name "ON" :description "OD"})
          new-project (sut/update-project! {:id 1 :name "NN" :description "ND"})]

      (testing "Returns updated project"
        (is (= {:id 1 :name "NN" :description "ND"} new-project)))

      (testing "Actually changes db"
        (is (zero? (tu/count! "FROM projects WHERE id=? AND name=? AND description=?"
                              1 "ON" "OD")))
        (is (= 1 (tu/count! "FROM projects WHERE id=? AND name=? AND description=?"
                            1 "NN" "ND")))))))

(deftest test-delete-project!

  (tu/with-db
    (let [user (tu/gen-save! tu/user)
          project (tu/gen-save! tu/project)
          user-project (tu/gen-save! tu/users-projects {:project-id (:id project)
                                                        :user-id (:id user)})]

      (sut/delete-project project)

      (testing "Deletes the project"
        (is (zero? (tu/count! "FROM projects WHERE id=?" (:id project)))))

      (testing "Deletes the userProject"
        (is (zero? (tu/count! "FROM usersProjects WHERE projectId=?" (:id project))))))))
