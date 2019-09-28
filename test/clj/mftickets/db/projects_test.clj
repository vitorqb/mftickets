(ns mftickets.db.projects-test
  (:require [mftickets.db.projects :as sut]
            [clojure.test :as t :refer [is are deftest testing use-fixtures]]
            [mftickets.test-utils :as tu]))

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
