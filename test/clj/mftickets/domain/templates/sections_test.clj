(ns mftickets.domain.templates.sections-test
  (:require [clojure.test :as t :refer [are deftest is testing use-fixtures]]
            [mftickets.db.core :as db.core]
            [mftickets.db.templates.sections :as db.templates.sections]
            [mftickets.domain.templates :as domain.templates]
            [mftickets.domain.templates.properties :as domain.templates.properties]
            [mftickets.domain.templates.sections :as sut]
            [mftickets.domain.templates.sections.inject
             :as
             domain.templates.sections.inject]
            [mftickets.domain.templates.sections.update
             :as
             domain.templates.sections.update]
            [mftickets.inject :refer [inject]]
            [mftickets.test-utils :as tu]))

(use-fixtures :once tu/common-fixture)

(deftest test-get-sections-for-template

  (with-redefs [db.templates.sections/get-sections-for-template identity]
    (is (= {:id 1} (sut/get-sections-for-template {:id 1})))))

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
