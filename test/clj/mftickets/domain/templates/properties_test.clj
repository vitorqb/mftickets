(ns mftickets.domain.templates.properties-test
  (:require [mftickets.domain.templates.properties :as sut]
            [clojure.test :as t :refer [is are deftest testing use-fixtures]]
            [mftickets.db.templates.properties :as db.templates.properties]))

(deftest test-get-properties-for-template

  (with-redefs [db.templates.properties/get-properties-for-template identity]
    (is (= {:id 1} (sut/get-properties-for-template {:id 1})))))
