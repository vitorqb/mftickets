(ns mftickets.domain.templates.sections-test
  (:require [mftickets.domain.templates.sections :as sut]
            [clojure.test :as t :refer [is are deftest testing use-fixtures]]
            [mftickets.db.templates.sections :as db.templates.sections]))

(deftest test-get-sections-for-template

  (with-redefs [db.templates.sections/get-sections-for-template identity]
    (is (= {:id 1} (sut/get-sections-for-template {:id 1})))))
