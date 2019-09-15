(ns mftickets.domain.templates-test
  (:require [mftickets.domain.templates :as sut]
            [clojure.test :as t :refer [is are deftest testing use-fixtures]]
            [mftickets.test-utils :as test-utils]
            [mftickets.db.core :as db.core]
            [clojure.java.jdbc :as jdbc]))

(deftest test-get-raw-template

  (testing "Base"
    (test-utils/with-db
      (jdbc/insert!
       db.core/*db*
       :templates
       {:id 1 :projectId 1 :name "Foo" :creationDate "2019-09-14T19:08:45"})
      (is (= {:id 1
              :project-id 1
              :name "Foo"
              :creation-date "2019-09-14T19:08:45"}
             (sut/get-raw-template 1))))))
