(ns mftickets.domain.templates-test
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.test :as t :refer [are deftest is testing use-fixtures]]
            [mftickets.db.core :as db.core]
            [mftickets.domain.templates :as sut]
            [mftickets.domain.templates.inject :as domain.templates.inject]
            [mftickets.middleware.pagination :as middleware.pagination]
            [mftickets.test-utils :as tu]))

(deftest test-get-raw-template

  (testing "Base"
    (tu/with-db
      (let [template (tu/gen-save! tu/template {})]
        (is (= template (sut/get-raw-template 1)))))))

(deftest test-get-raw-templates-for-project

  (let [get-raw-templates-for-project #'sut/get-raw-templates-for-project]

    (tu/with-db
      (let [templates [(tu/gen-save! tu/template {:id 1 :project-id 1})
                       (tu/gen-save! tu/template {:id 2 :project-id 1})
                       (tu/gen-save! tu/template {:id 3 :project-id 1 :name "zzzzz"})
                       (tu/gen-save! tu/template {:id 4 :project-id 2})]]

        (testing "Base"        
          (is (= (take 3 templates)
                 (get-raw-templates-for-project {:project {:id 1}})))
          (is (= [(last templates)]
                 (get-raw-templates-for-project {:project {:id 2}})))
          (is (= []
                 (get-raw-templates-for-project {:project {:id 3}}))))

        (testing "Paginated"

          (testing "First page"
            (let [opts {:project {:id 1}
                        ::middleware.pagination/page-number 1
                        ::middleware.pagination/page-size 2}]
              (is (= (take 2 templates)
                     (get-raw-templates-for-project opts)))))

          (testing "Second page"
            (let [opts {:project {:id 1}
                        ::middleware.pagination/page-number 2
                        ::middleware.pagination/page-size 2}]
              (is (= [(templates 2)]
                     (get-raw-templates-for-project opts))))))

        (testing "Filtered by name"
          (let [name-like "zzzzz"
                opts {:project {:id 1} :name-like name-like}]
            (is (= [(templates 2)]
                   (get-raw-templates-for-project opts)))))))))

(deftest test-get-projects-ids-for-template

  (testing "Base"
    (tu/with-db
      (tu/gen-save! tu/template {:id 2 :projectId 1})

      (testing "Existing" 
        (is (= #{1} (sut/get-projects-ids-for-template {:id 2}))))

      (testing "Non existing" 
        (is (= #{} (sut/get-projects-ids-for-template {:id 3})))))))

(deftest test-test-assoc-property-to-template

  (testing "Not found"
    (let [property {:id 1 :template-section-id 2}
          sections [{:id 3}]
          template {:sections sections}]
      (is (= template (sut/assoc-property-to-template template property)))))

  (testing "Base"
    (let [property {:id 10 :template-section-id 2}
          sections [{:id 1} {:id 2 :properties [{:id 11 :template-section-id 2}]}]
          template {:sections sections}]
      (is (= {:sections [{:id 1} {:id 2 :properties [{:id 11 :template-section-id 2} property]}]}
             (sut/assoc-property-to-template template property))))))

(deftest test-assoc-sections-to-template

  (testing "Base"
    (let [template {:id 1}
          sections [{:template-id 1} {:template-id 2}]]
      (is (= (assoc template :sections [(first sections)])
             (sut/assoc-sections-to-template template sections))))))

(deftest test-raw-template->template

  (testing "Base"
    (let [template {:id 1}
          sections [{:id 3 :template-id 1}]
          properties [{:id 4 :template-section-id 3}]]
      (is (= {:id 1 :sections [{:id 3 :template-id 1 :properties properties}]}
             (sut/raw-template->template template properties sections))))))

(deftest test-get-project-templates

  (let [project
        {:id 1}

        raw-template
        {:id 2}

        sections
        [{:id 3 :template-id 2} {:id 4 :template-id 1231231}]

        properties
        [{:id 4 :template-section-id 3} {:id 5 :template-section-id 991}]

        get-properties-for-templates
        (constantly properties)

        get-sections-for-templates
        (constantly sections)

        inject
        {::domain.templates.inject/get-properties-for-templates
         get-properties-for-templates
         ::domain.templates.inject/get-sections-for-templates
         get-sections-for-templates}

        expected-templates
        [{:id 2 :sections [{:template-id 2 :id 3 :properties [(first properties)]}]}]]

    (testing "Base"
      (with-redefs [sut/get-raw-templates-for-project #(and (= (:project %) project)
                                                            [raw-template])]

        (is (= expected-templates
               (sut/get-templates-for-project inject {:project project})))))

    (testing "Paged"
      (let [opts
            {:project project
             ::middleware.pagination/page-number 2
             ::middleware.pagination/page-size   2}]
        (with-redefs [sut/get-raw-templates-for-project
                      #(and (= 2 (::middleware.pagination/page-number %))
                            (= 2 (::middleware.pagination/page-size %))
                            (= project (:project %))
                            [raw-template])]
          (is (= expected-templates
                 (sut/get-templates-for-project inject opts))))))))
