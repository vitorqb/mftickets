(ns mftickets.db.templates-test
  (:require [mftickets.db.templates :as sut]
            [clojure.test :as t :refer [is are deftest testing use-fixtures]]
            [mftickets.test-utils :as test-utils]
            [clojure.java.jdbc :as jdbc]
            [mftickets.db.core :as db.core]))

(deftest test-get-raw-template

  (testing "When exists"
    (test-utils/with-db
      (test-utils/insert!
       :templates
       {:id 1 :projectId 1 :name "Foo" :creationDate "2019-09-14T19:08:45"})
      (is (= {:id 1
              :project-id 1
              :name "Foo"
              :creation-date "2019-09-14T19:08:45"}
             (sut/get-raw-template {:id 1}))))))
