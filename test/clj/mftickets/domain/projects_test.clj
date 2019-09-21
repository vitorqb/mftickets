(ns mftickets.domain.projects-test
  (:require [mftickets.domain.projects :as sut]
            [mftickets.test-utils :as tu]
            [clojure.test :as t :refer [is are deftest testing use-fixtures]]))

(deftest test-get-project

  (tu/with-db

    (testing "Not found"
      (is (nil? (sut/get-project {:id 999}))))

    (testing "Found"
      (let [project (tu/gen-save! tu/project {:id 888})]
        (is (= project (sut/get-project 888)))))))
