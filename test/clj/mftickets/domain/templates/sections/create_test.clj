(ns mftickets.domain.templates.sections.create-test
  (:require [clojure.test :as t :refer [are deftest is testing use-fixtures]]
            [mftickets.db.core :as db.core]
            [mftickets.db.templates.sections :as db.templates.sections]
            [mftickets.domain.templates :as domain.templates]
            [mftickets.domain.templates.properties :as domain.templates.properties]
            [mftickets.domain.templates.properties.get
             :as
             domain.templates.properties.get]
            [mftickets.domain.templates.sections.create :as sut]
            [mftickets.domain.templates.sections.inject
             :as
             domain.templates.sections.inject]
            [mftickets.inject :as inject]
            [mftickets.test-utils :as tu]))

(use-fixtures :once tu/common-fixture)

(deftest test-create-section!

  (testing "Calls db create-section!"
    (let [properties [{:id nil}]
          section {:template-id 999 :name "888" :properties properties}
          inject #::domain.templates.sections.inject{:create-property! (constantly nil)
                                                     :update-property! (constantly nil)
                                                     :delete-property! (constantly nil)}]
      (with-redefs [db.core/run-effects! (fn [& xs] xs)]
        (is (= [[db.templates.sections/create-section! section]
                [@#'sut/create-properties-for-new-section! inject ::db.core/< properties]]
               (sut/create-section! inject section))))))

  (testing "Integration:"
    (tu/with-db
      (let [project (tu/gen-save! tu/project)
            template (tu/gen-save! tu/template {:project-id (:id project)})
            property {:id nil
                      :name "Foo"
                      :template-section-id nil
                      :is-multiple false
                      :value-type :templates.properties.types/text}
            section {:id nil
                     :name "Bar"
                     :template-id (:id template)
                     :properties [property]}
            section (sut/create-section! inject/inject section)]

        (testing "Saves the section: "
          (let [loaded-section (->> {:project project}
                                    (domain.templates/get-templates-for-project inject/inject)
                                    (mapcat :sections)
                                    (filter #(= (:id %) (:id section)))
                                    first)]

            (testing "Returns the correct section"
              (is (= section loaded-section)))

            (testing "With correct name"
              (is (= (:name section) (:name loaded-section))))

            (testing "With correct template id"
              (is (= (:template-id section) (:template-id loaded-section))))))

        (testing "Saves the property: "
          (let [loaded-properties (domain.templates.properties.get/get-properties-for-template
                                   template)]

            (testing "Saves a single one"
              (is (= 1 (count loaded-properties))))

            (testing "With correct name"
              (is (= (:name property) (-> loaded-properties first :name))))

            (testing "With correct template-section-id"
              (is (= (:id section) (-> loaded-properties first :template-section-id))))

            (testing "With correct is-multiple"
              (is (= (:is-multiple property) (-> loaded-properties first :is-multiple))))

            (testing "With correct value-type"
              (is (= (:value-type property) (-> loaded-properties first :value-type))))))))))
