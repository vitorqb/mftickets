(ns mftickets.db.templates-test
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.test :as t :refer [are deftest is testing use-fixtures]]
            [mftickets.db.core :as db.core]
            [mftickets.db.templates :as sut]
            [mftickets.test-utils :as tu]
            [mftickets.utils.date-time :as utils.date-time]))

(use-fixtures :once tu/common-fixture)

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

(deftest test-update-raw-template!

  (tu/with-db
    (let [template
          (tu/gen-save!
           tu/template
           {:id 1 :project-id 2 :name "Foo" :creation-date "2019-01-01T22:22:22"})]

      (testing "Updates name and project-id"
        (sut/update-raw-template! (assoc template :name "Bar" :project-id 99))
        (is (= "Bar" (-> template sut/get-raw-template :name)))
        (is (= 99 (-> template sut/get-raw-template :project-id))))

      (testing "Does not update creation-date"
        (sut/update-raw-template! (assoc template :creation-date "111"))
        (is (= template (sut/get-raw-template template)))))))

(deftest test-create-template!

  (let [now "1993-11-23T22:11:00"
        template {:id nil
                  :project-id 1
                  :name "Foo"
                  :creation-date nil
                  :sections []}]
    (with-redefs [utils.date-time/now-as-str (constantly now)]
      (tu/with-db
        (let [response (sut/create-template! template)]

          (testing "Returns the same as if I get the template"
            (is (= response (sut/get-raw-template {:id (:id response)}))))

          (testing "Returns correct project-id"
            (is (= (:project-id template) (:project-id response))))

          (testing "Returns correct name"
            (is (= (:name template) (:name response))))

          (testing "Returns correct creation date"
            (is (= now (:creation-date response)))))))))

(deftest test-unique-template-name-for-project?

  (tu/with-db
    (is (true? (sut/unique-template-name-for-project? "foo" 1)))
    (tu/gen-save! tu/template {:name "foo" :project-id 1})
    (is (false? (sut/unique-template-name-for-project? "foo" 1)))))

(deftest test-delete-template

  (tu/with-db
    (let [template (tu/gen-save! tu/template)]

      (testing "Exists before..."
        (is (= (:id template) (:id (sut/get-raw-template template)))))

      (testing "Does not exists after..."
        (sut/delete-template! template)
        (is (nil? (sut/get-raw-template template)))))))
