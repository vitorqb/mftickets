(ns mftickets.domain.templates-test
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.test :as t :refer [are deftest is testing use-fixtures]]
            [mftickets.db.core :as db.core]
            [mftickets.db.projects :as db.projects]
            [mftickets.db.templates :as db.templates]
            [mftickets.domain.templates :as sut]
            [mftickets.domain.templates.inject :as domain.templates.inject]
            [mftickets.domain.templates.properties :as domain.templates.properties]
            [mftickets.domain.templates.sections :as domain.templates.sections]
            [mftickets.domain.templates.sections.inject
             :as
             domain.templates.sections.inject]
            [mftickets.middleware.pagination :as middleware.pagination]
            [mftickets.test-utils :as tu]))

(use-fixtures :once tu/common-fixture)

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

(deftest test-assoc-properties-to-template

  (testing "Assocs empty vector if no properties"
    (let [section {:id 1}
          template {:id 2 :sections [section]}
          properties []]
      (is (= (assoc template :sections [(assoc section :properties [])])
             (sut/assoc-properties-to-template template properties)))))

  (testing "Assoc properties"
    (let [property {:id 1 :template-section-id 2}
          section {:id 2}
          template {:id 3 :sections [section]}
          section-with-property (assoc section :properties [property])
          template-with-property (assoc template :sections [section-with-property])]
      (is (= template-with-property (sut/assoc-properties-to-template template [property]))))))

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

(deftest test-update-raw-template!

  (let [template {:id 1}
        update-raw-template! #'sut/update-raw-template!]
    (with-redefs [db.templates/update-raw-template! (fn [x] [::update-raw-template! x])]
      (is (= [::update-raw-template! template] (update-raw-template! template))))))

(deftest test-compare-template-sections

  (let [compare-template-sections #'sut/compare-template-sections
        old-sections [{:id 1} {:id 2}]
        old-template {:sections old-sections}
        new-sections [{:id 2} {:id 3}]
        new-template {:sections new-sections}]

    (testing "Delete..."
      (is (= [(first old-sections)]
             (compare-template-sections :delete old-template new-template))))

    (testing "Update..."
      (is (= [[(second old-sections) (first new-sections)]]
             (compare-template-sections :update old-template new-template))))

    (testing "Create..."
      (is (= [(second new-sections)]
             (compare-template-sections :create old-template new-template))))))

(deftest test-update-template!

  (let [properties [{:id 789
                     :name "Property"
                     :template-section-id 456
                     :value-type :templates.properties.types/radio
                     :is-multiple false
                     :order 0}]
        sections [{:id 456
                   :template-id 123
                   :name "Section"
                   :order 999
                   :properties properties}]
        new-template {:id 123
                      :project-id 456
                      :name "Template"
                      :creation-date "2019-01-01T00:00:00"
                      :sections sections}
        old-template new-template
        update-section! domain.templates.sections/update-section!
        create-section! domain.templates.sections/create-section!
        delete-section! domain.templates.sections/delete-section!
        create-property! domain.templates.properties/create-property!
        update-property! domain.templates.properties/update-property!
        delete-property! domain.templates.properties/delete-property!
        get-properties-for-section domain.templates.properties/get-properties-for-section
        inject {::domain.templates.inject/update-section! update-section!
                ::domain.templates.inject/create-section! create-section!
                ::domain.templates.inject/delete-section! delete-section!
                ::domain.templates.sections.inject/delete-property! delete-property!
                ::domain.templates.sections.inject/create-property! create-property!
                ::domain.templates.sections.inject/update-property! update-property!
                ::domain.templates.sections.inject/get-properties-for-section
                get-properties-for-section}]

    (testing "Updates name"
      (let [new-template (assoc new-template :name "FOO")]
        (tu/with-db
          (tu/gen-save! tu/template (dissoc old-template :sections))
          (tu/gen-save! tu/template-section (-> sections first (dissoc :properties)))
          (tu/gen-save! tu/template-section-property (first properties))
          (sut/update-template! inject old-template new-template)
          (is (= "FOO" (-> new-template :id sut/get-raw-template :name))))))

    (testing "Removes one section and appends a new one"
      (let [new-sections [{:template-id 123 :name "000" :order 888}]
            new-template (assoc new-template :sections new-sections)]
        (tu/with-db
          (tu/gen-save! tu/template (dissoc old-template :sections))
          (tu/gen-save! tu/template-section (-> sections first (dissoc :properties)))
          (tu/gen-save! tu/template-section-property (first properties))
          (sut/update-template! inject old-template new-template)
          (let [new-sections* (domain.templates.sections/get-sections-for-template new-template)]
            (is (= 1 (count new-sections*)))
            (is (= (first new-sections) (-> new-sections* first (dissoc :id)))))
          (is (= [] (domain.templates.properties/get-properties-for-template new-template))))))

    (testing "Removes one property and appends a new one"
      (let [new-properties [{:name "New Property"
                             :template-section-id 456
                             :value-type :templates.properties.types/text
                             :is-multiple true
                             :order 0}]
            new-sections [(-> sections first (assoc :properties new-properties))]
            new-template (assoc new-template :sections new-sections)]
        (tu/with-db
          (tu/gen-save! tu/template (dissoc old-template :sections))
          (tu/gen-save! tu/template-section (-> sections first (dissoc :properties)))
          (tu/gen-save! tu/template-section-property (first properties))
          (sut/update-template! inject old-template new-template)
          (let [new-properties* (domain.templates.properties/get-properties-for-template new-template)]
            (is (= 1 (count new-properties*)))
            (is (= (first new-properties)
                   (-> new-properties* first (dissoc :id))))))))

    (testing "Renames one property"
      (let [new-properties [(-> properties first (assoc :name "New Porperty Name"))]
            new-sections [(-> sections first (assoc :properties new-properties))]
            new-template (assoc new-template :sections new-sections)]
        (tu/with-db
          (tu/gen-save! tu/template (dissoc old-template :sections))
          (tu/gen-save! tu/template-section (-> sections first (dissoc :properties)))
          (tu/gen-save! tu/template-section-property (first properties))
          (sut/update-template! inject old-template new-template)
          (is (= new-properties
                 (domain.templates.properties/get-properties-for-template new-template))))))))

(deftest test-get-template
  (let [template {:id 1}
        sections [{:id 2}]
        get-sections (constantly sections)
        properties [{:id 3}]
        get-properties (constantly properties)
        inject {::domain.templates.inject/get-sections-for-templates get-sections
                ::domain.templates.inject/get-properties-for-templates get-properties}]
    (with-redefs [sut/get-raw-template (constantly template)
                  sut/raw-template->template (fn [& xs] [::->template xs])]
      (is (= [::->template [template properties sections]]
             (sut/get-template inject (:id template)))))))

(deftest test-create-sections-for-new-template

  (with-redefs [db.core/run-effects! (fn [& xs] xs)]

    (let [create-section! #(do ::create-section)
          inject {::domain.templates.inject/create-section! create-section!}
          created-template {:id 1}]

      (testing "Empty"
        (is (= [[sut/get-template inject (:id created-template)]]
               (sut/create-sections-for-new-template inject [] created-template))))

      (testing "Two sections"
        (let [section1 {:id 1}
              section2 {:id 2}
              sections [section1 section2]]
          (is (= [[create-section! inject (assoc section1 :template-id (:id created-template))]
                  [create-section! inject (assoc section2 :template-id (:id created-template))]
                  [sut/get-template inject (:id created-template)]]
                 (sut/create-sections-for-new-template inject sections created-template))))))))

(deftest test-create-template!

  (with-redefs [db.core/run-effects! (fn [& xs] xs)]

    (let [template {:id 1 :sections {:id 2}}
          inject {::foo 1}]

      (testing "Calls db create template"
        (is (= [db.templates/create-template! template]
               (->> template (sut/create-template! inject) first))))

      (testing "Calls create-new-template-sections"
        (is (= [sut/create-sections-for-new-template inject (:sections template) ::db.core/<]
               (->> template (sut/create-template! inject) second)))))))

(deftest test-unique-template-name-for-project?
  (with-redefs [db.templates/unique-template-name-for-project?
                (fn [new-name project-id] [::unique-name new-name project-id])]
    (is (= [::unique-name "x" 1] (sut/unique-template-name-for-project? "x" 1)))))

(deftest test-delete-template
  (with-redefs [db.templates/delete-template! (fn [x] [::delete-template! x])]
    (is (= [::delete-template! {:id 1}] (sut/delete-template! {:id 1})))))

(deftest test-user-has-access

  (testing "Dispatches to db call"
    (let [user {:id 1} template {:id 2}]
      (with-redefs [db.projects/user-has-access-to-template? (fn [u t] [::access? u t])]
        (is (= [::access? user template] (sut/user-has-access-to-template? user template)))))))
