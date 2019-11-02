(ns mftickets.db.templates-test
  (:require [mftickets.db.templates :as sut]
            [clojure.test :as t :refer [is are deftest testing use-fixtures]]
            [mftickets.test-utils :as tu]
            [clojure.java.jdbc :as jdbc]
            [mftickets.db.core :as db.core]))

(deftest test-get-raw-template

  (testing "When exists"
    (tu/with-db
      (let [template (tu/gen-save! tu/template {:id 1})]
        (is (= template (sut/get-raw-template {:id 1})))
        (is (nil? (sut/get-raw-template {:id 2})))))))

(deftest test-get-raw-templates-for-project

  (testing "With name-line"

    (let [project-id 1]

      (tu/with-db
        (tu/gen-save! tu/template {:id 1 :name "foo bar baz" :project-id project-id})
        (tu/gen-save! tu/template {:id 2 :name "boza" :project-id project-id})
        (tu/gen-save! tu/template {:id 3 :name "FunnY Name!" :project-id project-id})

        (testing "No matches"
          (let [name-like "fb"
                opts {:project-id project-id :name-like name-like}]
            (is (empty? (sut/get-raw-templates-for-project opts)))))

        (testing "All matches"
          (let [name-like "a"
                opts {:project-id project-id :name-like name-like}]
            (is (= 3 (-> opts sut/get-raw-templates-for-project count)))))

        (testing "All matches (no name-like)"
          (let [opts {:project-id project-id}]
            (is (= 3 (-> opts sut/get-raw-templates-for-project count)))))

        (testing "One matches"
          (let [name-like "foo baz"
                opts {:project-id project-id :name-like name-like}
                templates (sut/get-raw-templates-for-project opts)]
            (is (= 1 (count templates)))
            (is (= "foo bar baz" (-> templates first :name)))))

        (testing "Funny name"
          (let [name-like "funny name"
                opts {:project-id project-id :name-like name-like}
                templates (sut/get-raw-templates-for-project opts)]
            (is (= 1 (count templates)))
            (is (= "FunnY Name!" (-> templates first :name)))))))))

(deftest test-count-templates-for-project

  (testing "No templates"
    (tu/with-db
      (is (= 0 (sut/count-templates-for-project {:project-id 1})))))

  (testing "Two templates"
    (tu/with-db
      (tu/gen-save! tu/template {:id 1 :project-id 1 :name "bar"})
      (tu/gen-save! tu/template {:id 2 :project-id 1 :name "baz"})

      (testing "Matching all"
        (is (= 2 (sut/count-templates-for-project {:project-id 1})))
        (is (= 2 (sut/count-templates-for-project {:project-id 1 :name-like "ba"}))))

      (testing "Matching one"
        (is (= 1 (sut/count-templates-for-project {:project-id 1 :name-like "bar"}))))

      (testing "Matching none"
        (is (= 0 (sut/count-templates-for-project {:project-id 1 :name-like "zzz"})))))))
