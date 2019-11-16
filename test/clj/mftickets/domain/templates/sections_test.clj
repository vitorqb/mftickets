(ns mftickets.domain.templates.sections-test
  (:require [clojure.test :as t :refer [are deftest is testing use-fixtures]]
            [mftickets.db.core :as db.core]
            [mftickets.db.templates.sections :as db.templates.sections]
            [mftickets.domain.templates.sections :as sut]
            [mftickets.domain.templates.sections.inject :as domain.templates.sections.inject]))

(deftest test-get-sections-for-template

  (with-redefs [db.templates.sections/get-sections-for-template identity]
    (is (= {:id 1} (sut/get-sections-for-template {:id 1})))))


(deftest test-sections-getter

  (let [templates [{:id 1} {:id 2}]
        sections [{:id 1 :template-id 1} {:id 2 :template-id 1} {:id 3 :template-id 3}]]
    (with-redefs [db.templates.sections/get-sections-for-templates-ids
                  #(when (= % [1 2]) sections)]
      (let [getter (sut/sections-getter templates)]
        (is (= [{:id 1 :template-id 1} {:id 2 :template-id 1}]
               (getter {:id 1})))
        (is (= []
               (getter {:id 2})))))))

(deftest test-update-section-properties!

  (let [create-property! (fn [_] nil)
        update-property! (fn [_] nil)
        delete-property! (fn [_] nil)
        inject {::domain.templates.sections.inject/create-property! create-property!
                ::domain.templates.sections.inject/update-property! update-property!
                ::domain.templates.sections.inject/delete-property! delete-property!}
        old-properties [{:id 1} {:id 2} {:id 3}]
        old-section {:properties old-properties}
        new-properties [{:id 3} {:id nil ::count 1} {:id nil ::count 2}]
        new-section {:properties new-properties}]

    (with-redefs [db.core/run-effects! (fn [& xs] xs)]

      (let [effects (@#'sut/update-section-properties! inject old-section new-section)]
    
        (testing "Creates new properties"
          (is (some #{[create-property! (get new-properties 1)]} effects))
          (is (some #{[create-property! (get new-properties 2)]} effects)))

        (testing "Deletes deleted properties"
          (is (some #{[delete-property! {:id 1}]} effects))
          (is (some #{[delete-property! {:id 2}]} effects)))

        (testing "Updates properties"
          (is (some #{[update-property! {:id 3}]} effects)))))))

(deftest test-update-raw-section!

  (let [new-section {:id 1 :template-id 3 :name "Bar"}]
    (with-redefs [db.templates.sections/update-raw-section! (fn [x] [::update-raw-section x])]
      (is (= [::update-raw-section new-section] (@#'sut/update-raw-section! new-section))))))

(deftest test-update-section!

  (let [inject {}
        section {:id 1 :properties [{:id 2 :section-id 1}]}
        new-section (assoc section :properties [{:id 3 :section-id 1}])]
    (with-redefs [db.core/run-effects! (fn [& xs] xs)]
      (is (= [[@#'sut/update-raw-section! new-section]
              [@#'sut/update-section-properties! inject section new-section]]
             (sut/update-section! inject section new-section))))))

(deftest test-create-section!

  (let [section {:template-id 999 :name "888"}]
    (with-redefs [db.templates.sections/create-section! (fn [x] [::create-section x])]
      (is (= [::create-section section] (sut/create-section! section))))))

(deftest test-delete-properties-for-section!

  (let [delete-property! (fn [_] nil)
        inject {::domain.templates.sections.inject/delete-property! delete-property!}
        properties [{:id 1} {:id 2}]
        section {:id 3 :properties properties}
        expected-effects [[delete-property! (first properties)]
                          [delete-property! (second properties)]]]
    (with-redefs [db.core/run-effects! (fn [& xs] xs)]
      (is (= expected-effects (sut/delete-properties-for-section! inject section))))))

(deftest test-delete-section

  (let [properties [{:id 2}]
        section {:id 1 :properties properties}
        expected-effects [[sut/delete-properties-for-section! {} section]
                          [db.templates.sections/delete-section! section]]]
    (with-redefs [db.core/run-effects! (fn [& xs] xs)]
      (is (= expected-effects (sut/delete-section! {} section))))))
