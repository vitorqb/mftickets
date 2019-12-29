(ns mftickets.routes.services.templates-test
  (:require [clojure.test :as t :refer [are deftest is testing use-fixtures]]
            [com.rpl.specter :as s]
            [mftickets.db.core :as db.core]
            [mftickets.db.prefill :as db.prefill]
            [mftickets.domain.projects :as domain.projects]
            [mftickets.domain.templates :as domain.templates]
            [mftickets.domain.templates.properties :as domain.templates.properties]
            [mftickets.domain.templates.sections :as domain.templates.sections]
            [mftickets.domain.templates.transform :as d.templates.transform]
            [mftickets.domain.users :as domain.users]
            [mftickets.handler :refer [app]]
            [mftickets.http.responses :as http.responses]
            [mftickets.inject :refer [inject]]
            [mftickets.middleware.auth :as middleware.auth]
            [mftickets.middleware.context :as middleware.context]
            [mftickets.middleware.pagination :as middleware.pagination]
            [mftickets.routes.services.templates :as sut]
            [mftickets.routes.services.templates.validation.common
             :as
             templates.validation.common]
            [mftickets.routes.services.templates.validation.update
             :as
             templates.validation.update]
            [mftickets.test-utils :as tu]
            [mftickets.utils.kw :as utils.kw]
            [ring.mock.request :as mock.request]))

(deftest test-validate-template-update

  (with-redefs [domain.templates/unique-template-name-for-project? (constantly true)]

    (let [old-template {:id 1
                        :project-id 1
                        :name "foo"
                        :creation-date "2019-01-01T00:00:00"
                        :sections []}
          validate-template-update #'sut/validate-template-update]

      (testing "Valid if equal"
        (let [new-template old-template]
          (is (= :validation/success (validate-template-update old-template new-template)))))

      (testing "Valid and not equal"
        (let [new-sections [{:id 2 :template-id 1 :properties [{:template-section-id 2}]}]
              new-name "BAR"
              new-template (assoc old-template :name new-name :sections new-sections)]
          (is (= :validation/success (validate-template-update old-template new-template)))))

      (testing "Invalid..."

        (testing "Project id does not match"
          (let [new-template (assoc old-template :project-id 2)
                result (validate-template-update old-template new-template)]
            (is (= ::templates.validation.update/project-id-missmatch (first result)))))

        (testing "Id does not match"
          (let [new-template (assoc old-template :id 2)
                result (validate-template-update old-template new-template)]
            (is (= ::templates.validation.update/id-missmatch (first result)))))

        (testing "Section id does not match"
          (let [new-template (assoc-in old-template [:sections 0] {:template-id 2})
                result (validate-template-update old-template new-template)]
            (is (= ::templates.validation.update/section-template-id-missmatch (first result)))))

        (testing "Section Property id does not match"
          (let [sections [{:id 9 :template-id 1 :properties [{:template-section-id 8}]}]
                new-template (assoc old-template :sections sections)
                result (validate-template-update old-template new-template)]
            (is (= ::templates.validation.update/property-section-id-missmatch (first result)))))

        (testing "Created at does not match"
          (let [new-template (assoc old-template :creation-date "2000-01-12T00:11:00")
                result (validate-template-update old-template new-template)]
            (is (= ::templates.validation.update/created-at-missmatch (first result)))))

        (testing "Duplicated template name for project..."
          (with-redefs [domain.templates/unique-template-name-for-project? (constantly false)]

            (testing "Skip check if name did not change"
              (is (= :validation/success (validate-template-update old-template old-template))))

            (testing "Fails if name is new and repeated"
              (let [new-template (update old-template :name #(str % "1"))
                    result (validate-template-update old-template new-template)]
                (is (= ::templates.validation.common/repeated-name (first result)))))))))))

(deftest test-user-has-access-to-template?

  (let [user-has-access-to-template? #'sut/user-has-access-to-template?]

    (testing "True"
      (with-redefs [domain.users/get-projects-ids-for-user (constantly #{1 2})
                    domain.templates/get-projects-ids-for-template (constantly #{3 4 1})]
        (is (true? (user-has-access-to-template? nil nil)))))

    (testing "True"
      (with-redefs [domain.users/get-projects-ids-for-user (constantly #{1 2})
                    domain.templates/get-projects-ids-for-template (constantly #{3 4})]
        (is (false? (user-has-access-to-template? nil nil)))))))

(deftest test-wrap-user-has-access-to-template??

  (let [wrap-user-has-access-to-template? #'sut/wrap-user-has-access-to-template?]

    (testing "No template -> calls handler."
      (let [handler (wrap-user-has-access-to-template? identity)
            request {::foo ::bar}]
        (is (= request (handler request)))))

    (testing "Template and user not allowed."
      (with-redefs [sut/user-has-access-to-template? (constantly false)]
        (let [handler (wrap-user-has-access-to-template? identity)
              request {::sut/template {:id 1}}
              response (handler request)]
          (is (= {:status 404} response)))))

    (testing "Template id and user has access."
      (with-redefs [sut/user-has-access-to-template? (fn [user template]
                                           (and (= user ::user) (= template ::template)))]
        (let [request {:mftickets.auth/user ::user ::sut/template ::template}
              handler (wrap-user-has-access-to-template? #(hash-map ::param %))
              response (handler request)]
          (is (= {::param request} response)))))))

(deftest test-get-template

  (let [wrap-get-template #'sut/wrap-get-template
        wrap-user-has-access-to-template? #'sut/wrap-user-has-access-to-template?]

    (testing "Non-existing template"
      (with-redefs [domain.templates/get-template (constantly nil)]
        (is (= {:status 404}
               (sut/handle-get {:parameters {:path {:id 1}}})))))

    (testing "Existing template"
      (let [template {:id 7
                      :sections [{:id 9 :properties [{:id 8 :template-section-id 9}]}]}]
        (with-redefs [domain.templates/get-template (fn [_ _] template)]

          (let [handler (-> sut/handle-get wrap-get-template)
                response (handler {:parameters {:path {:id 9}}})]
            (are [ks f] (f (get-in response ks ::nf))
              [:status]   #(= 200 %)
              [:body :id] #(= 7 %)
              [:body :sections] #(= (:sections template) %))))))))

(deftest test-wrap-get-template

  (let [wrap-get-template #'sut/wrap-get-template]

    (testing "Missing template-id"
      (let [request {}
            handler (wrap-get-template identity)
            result (handler request)]
        (is (= request result))))

    (testing "template-id mapping to no template."
      (with-redefs [sut/get-template #(if (= % 9) nil ::wrong-argument)]
        (let [request {:parameters {:path {:id 9}}}
              handler (wrap-get-template identity)
              result (handler request)]
          (is (= request result)))))

    (testing "template-id mapping to template"
      (with-redefs [sut/get-template #(when (= % 9) ::template)]
        (let [request {:parameters {:path {:id 9}}}
              handler (wrap-get-template identity)
              result (handler request)]
          (is (= (assoc request ::sut/template ::template) result)))))))

(deftest test-handle-get

  (testing "Inexistant template returns 404"
    (is (= {:status 404} (sut/handle-get {}))))

  (testing "Existing template is returned"
    (let [template-id 999]
      (is (= {:status 200 :body ::template}
             (sut/handle-get {::sut/template ::template}))))))

(deftest test-app-integration-get-template

  (testing "Existing template"
    (tu/with-app
      (tu/with-db
        (tu/with-user-and-token [user token]
          (with-redefs [sut/user-has-access-to-template? (fn [user* _] (= user* user))]
            (tu/gen-save! tu/template
                          {:id 1 :project-id 1 :creation-date "2019-09-14T19:08:45"})
            (tu/gen-save! tu/template-section {:id 1 :template-id 1})
            (tu/gen-save! tu/template-section-property {:id 1 :template-section-id 1})
            (tu/gen-save! tu/template-section-property {:id 2 :template-section-id 1})
            (let [req (-> (mock.request/request :get "/api/templates/1")
                          (tu/auth-header token))
                  resp ((app) req)
                  body (tu/decode-response-body resp)]

              (testing "Returns 200"
                (is (= 200 (:status resp))))

              (testing "Returns body with keys"
                (are [ks f] (f (get-in body ks ::nf))
                  [:id] #(= 1 %)
                  [:project-id] #(= 1 %)
                  [:creation-date] #(= "2019-09-14T19:08:45" %)
                  [:sections] #(= (count %) 1)
                  [:sections 0 :properties] #(= (count %) 2))))))))))

(deftest test-app-integration-get-template-list

  (testing "Base integration"
    (tu/with-app
      (tu/with-db
        (tu/with-user-and-token [user token]

          ;; Adds a project
          (tu/gen-save! tu/project {:id 99})

          ;; Assign to user
          (tu/gen-save! tu/users-projects {:user-id (:id user) :project-id 99})
          
          (let [raw-project-template
                (tu/gen-save! tu/template {:id 1 :project-id 99})

                project-template-section
                (tu/gen-save! tu/template-section {:id 2 :template-id 1})

                project-template-section-property
                (tu/gen-save! tu/template-section-property {:template-section-id 2})

                project-template
                (-> raw-project-template
                    (#'domain.templates/assoc-sections-to-template
                     [project-template-section])
                    (#'domain.templates/assoc-properties-to-template
                     [project-template-section-property]))

                other-templates
                [(tu/gen-save! tu/template {:id 2 :project-id 100})]

                request
                (-> (mock.request/request :get "/api/templates")
                    (mock.request/query-string {:project-id 99})
                    (tu/auth-header token))

                resp
                ((app) request)

                {:keys [page-number page-size total-items-count items] :as body}
                (tu/decode-response-body resp)]

            (testing "Returns 200"
              (is (= 200 (:status resp))))

            (testing "Returns paginated response"
              (is (= middleware.pagination/default-page-number page-number))
              (is (= middleware.pagination/default-page-size page-size))
              (is (= 1 total-items-count)))

            (testing "Returns list of templates inside :items"
              ;; keyword is returned as string
              (let [project-template*
                    (s/transform
                     [:sections s/ALL :properties s/ALL :value-type]
                     utils.kw/full-name
                     project-template)]
                (is (= [project-template*] items))))

            ;; Now adds another template
            (tu/gen-save! tu/template {:id 3 :project-id 99})

            (testing "Pagination with more than one items"
              (let [request
                    (-> (mock.request/request :get "/api/templates")
                        (mock.request/query-string {:project-id 99
                                                    :pageNumber 1
                                                    :pageSize 1})
                        (tu/auth-header token))

                    resp
                    ((app) request)

                    {:keys [page-number page-size total-items-count items] :as body}
                    (tu/decode-response-body resp)]
                (is (= 1 page-number))
                (is (= 1 page-size))
                (is (= 1 (count items)))
                (is (= 2 total-items-count))))

            ;; Now adds a template with a funny name
            (tu/gen-save! tu/template {:id 4 :name "funny NaMe!" :project-id 99})

            (testing "Filtering by name"
              (let [request
                    (-> (mock.request/request :get "/api/templates")
                        (mock.request/query-string {:project-id 99 :name-like "funny name"})
                        (tu/auth-header token))

                    resp
                    ((app) request)

                    {:keys [page-number page-size total-items-count items] :as body}
                    (tu/decode-response-body resp)]
                (is (= 1 (count items)))
                (is (= 4 (-> items first :id)))
                (is (= 1 total-items-count))))))))))

(deftest test-integration-edit-template

  (tu/with-app
    (tu/with-db
      (tu/with-user-and-token [user token]

        (testing "Editing template and property name with post..."
          (let [template-id
                99

                section-id
                88

                name
                "Foo Template"

                project
                (tu/gen-save! tu/project {})

                _
                (tu/gen-save! tu/users-projects {:user-id (:id user) :project-id (:id project)})
                
                _
                (tu/gen-save! tu/template {:id template-id :name name :project-id (:id project)})

                _
                (tu/gen-save! tu/template-section {:id section-id :template-id template-id})

                _
                (tu/gen-save! tu/template-section-property {:template-section-id section-id})

                template
                (-> (mock.request/request :get (str "/api/templates/" template-id))
                    (tu/auth-header token)
                    ((app))
                    (tu/decode-response-body))
                
                new-name
                "Bar Template"

                new-property-name
                "New Property Name"

                new-order
                99

                new-template
                (-> template
                    (assoc :name new-name)
                    (assoc-in [:sections 0 :properties 0 :name] new-property-name)
                    (assoc-in [:sections 0 :properties 0 :order] new-order))
                
                request
                (-> (mock.request/request :post (str "/api/templates/" template-id))
                    (mock.request/json-body new-template)
                    (tu/auth-header token))

                response
                ((app) request)

                body
                (tu/decode-response-body response)]

            (testing "Old name was retrieved before post"
              (is (= name (:name template))))

            (testing "Post returned 200"
              (is (= 200 (:status response))))

            (testing "New template was returned"
              (is (= new-template body)))

            (testing "Sees new template when getting"
              (is (= new-template
                     (-> (mock.request/request :get (str "/api/templates/" template-id))
                         (tu/auth-header token)
                         ((app))
                         (tu/decode-response-body)))))))))))

(deftest test-integration-edit-template-section-ordering

  (tu/with-app
    (tu/with-db
      (tu/with-user-and-token [user token]

        (testing "Editing a template section ordering"
          (let [template-id
                5

                section1-id
                6

                section2-id
                7

                project
                (tu/gen-save! tu/project {})

                _
                (tu/gen-save! tu/users-projects {:user-id (:id user)
                                                 :project-id (:id project)})

                _
                (tu/gen-save! tu/template {:id template-id
                                           :project-id (:id project)})

                _
                (tu/gen-save! tu/template-section {:id section1-id
                                                   :template-id template-id
                                                   :order 0})

                _
                (tu/gen-save! tu/template-section {:id section2-id
                                                   :template-id template-id
                                                   :order 1})

                template
                (-> (mock.request/request :get (str "/api/templates/" template-id))
                    (tu/auth-header token)
                    ((app))
                    (tu/decode-response-body))

                new-template
                (-> template
                    (d.templates.transform/set-section-order section1-id 1)
                    (d.templates.transform/set-section-order section2-id 0))
                
                request
                (-> (mock.request/request :post (str "/api/templates/" template-id))
                    (mock.request/json-body new-template)
                    (tu/auth-header token))

                response
                ((app) request)

                body
                (tu/decode-response-body response)]

            (testing "Same template id is returned"
              (is (= template-id (:id body))))

            (testing "Ordering is changed"
              (is (= 1 (d.templates.transform/get-section-order body section1-id)))
              (is (= 0 (d.templates.transform/get-section-order body section2-id))))))))))

(deftest test-integration-create-template

  (testing "Success requests: "
    (tu/with-db
      (tu/with-app
        (tu/with-user-and-token [user token]
          (let [project
                (tu/gen-save! tu/project)

                _
                (tu/gen-save! tu/users-projects {:user-id (:id user) :project-id (:id project)})

                property
                {:id nil
                 :template-section-id nil
                 :name "Foo Property"
                 :is-multiple false
                 :value-type :templates.properties.types/text
                 :order 0}

                section
                {:id nil
                 :template-id nil
                 :name "Foo Section"
                 :properties [property]
                 :order 0}

                new-template
                {:id nil
                 :project-id (:id project)
                 :name "Foo"
                 :creation-date nil
                 :sections [section]}

                request
                (-> (mock.request/request :post "/api/templates")
                    (mock.request/query-string {:project-id (:id project)})
                    (mock.request/json-body new-template)
                    (tu/auth-header token))

                response
                ((app) request)

                body
                (tu/decode-response-body response)]

            (testing "Returns body with... "

              (testing "Id"
                (is (int? (:id body))))

              (testing "project-id"
                (is (= (:id project) (:project-id body))))

              (testing "name"
                (is (= (:name new-template) (:name body))))

              (testing "creation-date"
                (is (string? (:creation-date body))))

              (testing "sections... "

                (testing "A single one"
                  (is (= 1 (-> body :sections count))))

                (testing "id"
                  (is (int? (-> body :sections first :id))))

                (testing "template-id"
                  (is (= (:id body) (-> body :sections first :template-id))))

                (testing "name"
                  (is (= (:name section) (-> body :sections first :name))))

                (testing "properties..."

                  (testing "A single one"
                    (is (= 1 (-> body :sections first :properties count))))

                  (testing "id"
                    (is (int? (-> body :sections first :properties first :id))))

                  (testing "template-section-id"
                    (is (= (-> body :sections first :id)
                           (-> body :sections first :properties first :template-section-id))))

                  (testing "name"
                    (is (= (:name property) (-> body :sections first :properties first :name))))

                  (testing "is-multiple"
                    (is (= (:is-multiple property)
                           (-> body :sections first :properties first :is-multiple))))

                  (testing "value-type"
                    (is (= (-> property :value-type utils.kw/full-name)
                           (-> body :sections first :properties first :value-type))))

                  (testing "order"
                    (is (= (:order property)
                           (-> body :sections first :properties first :order)))))))))))))

(deftest test-handle-get-project-templates

  (testing "Base"
    (let [pagination-data
          #::middleware.pagination{:page-number 2 :page-size 3}
          
          project
          {:id 9}

          name-like
          "foo"

          request
          {::middleware.pagination/page-number 2
           ::middleware.pagination/page-size 3
           ::middleware.context/project project
           :parameters {:query {:name-like name-like}}}

          templates
          [{:id 11}]

          templates-count
          99]

      (with-redefs [domain.templates/get-templates-for-project
                    (fn [_ opts]
                      (and (= project (:project opts))
                           (= 2 (::middleware.pagination/page-number opts))
                           (= 3 (::middleware.pagination/page-size opts))
                           (= name-like (:name-like opts))
                           templates))

                    domain.templates/count-templates-for-project
                    (fn [opts] (when (= project (:project opts)) templates-count))]
        
        (is (= {:status 200
                ::middleware.pagination/items templates
                ::middleware.pagination/total-items-count templates-count}
               (sut/handle-get-project-templates request)))))))

(deftest test-handle-post

  (testing "Fails if invalid template..."
    (let [new-template {:id 1 :name "foo"}
          error [::invalid-name "The name is invalid."]]
      (with-redefs [sut/validate-template-update (constantly error)]
        (let [result (sut/handle-post {:parameters {:body new-template}})]

          (testing "Returns 400"
            (is (= 400 (:status result))))

          (testing "Returns error key and message"
            (is (= {:error-key (first error) :error-message (second error)}
                   (:body result))))))))

  (testing "Works if valid template..."
    (let [template {:id 1
                    :project-id 1
                    :name "foo"
                    :creation-date "2019-01-01T00:00:00"
                    :sections []}
          new-template (assoc template :name "Foo")]

      (with-redefs [sut/validate-template-update (constantly :validation/success)
                    domain.templates/update-template! (constantly nil)
                    sut/get-template (constantly new-template)]
        (let [result (sut/handle-post {:parameters {:body new-template}})]

          (testing "Returns 200"
            (is (= 200 (:status result))))

          (testing "Returns the new template on the body"
            (is (= new-template (:body result)))))))))

(deftest test-handler-creation-post

  (let [new-template {:id nil
                      :project-id 1
                      :name "Foo"
                      :creation-date nil
                      :sections []}
        request {:parameters {:body new-template}}]

    (testing "Returns validation error if validation fails"
      (let [validation-error [::err "Err"]]
        (with-redefs [sut/validate-new-template (constantly validation-error)]
          (is (= (http.responses/validation-error validation-error)
                 (sut/handle-creation-post request))))))

    (testing "Calls create-template! on success"
      (let [calls (atom [])]
        (with-redefs [domain.templates/create-template!
                      (fn [x y] (swap! calls conj [::create-template x y]))
                      sut/validate-new-template (constantly :validation/success)]
          (sut/handle-creation-post request)
          (is (= [[::create-template inject new-template]] @calls)))))

    (testing "On success, returns..."
      (with-redefs [domain.templates/create-template! (constantly ::new-template)
                    sut/validate-new-template (constantly :validation/success)]
        (let [response (sut/handle-creation-post request)]

          (testing "200"
            (is (= 200 (:status response))))

          (testing "The new template as body"
            (is (= ::new-template (:body response)))))))))

(deftest test-handle-delete

  (let [template {:id 1}
        request {::sut/template template}]

    (testing "Returns validation error if validation fails"
      (let [error [:foo "bar"]]
        (with-redefs [sut/validate-template-delete (constantly error)]
          (is (= (http.responses/validation-error error) (sut/handle-delete request))))))

    (testing "Calls deletion if validation succeeds."
      (let [calls (atom [])]
        (with-redefs [domain.templates/delete-template! #(swap! calls conj [::delete-template! %])]
          (is (= {:status 200} (sut/handle-delete request)))
          (is (= [[::delete-template! template]] @calls)))))))

(deftest test-handle-delete-integration

  (tu/with-db
    (tu/with-app
      (tu/with-user-and-token [user token]

        (let [project
              (tu/gen-save! tu/project)

              _
              (tu/gen-save! tu/users-projects {:user-id (:id user) :project-id (:id project)})

              template (tu/gen-save! tu/template {:user-id (:id user) :project-id (:id project)})

              get-request
              (-> (mock.request/request :get (str "/api/templates/" (:id template)))
                  (mock.request/query-string {:project-id (:id project)})
                  (tu/auth-header token))

              delete-request
              (-> (mock.request/request :delete (str "/api/templates/" (:id template)))
                  (tu/auth-header token))]

          (testing "Template exists before deletion"
            (let [response ((app) get-request)]
              (is (= 200 (:status response)))
              (is (= (:name template) (:name (tu/decode-response-body response))))))

          (testing "Sends delete request and response"
            (let [response ((app) delete-request)]
              (is (= 200 (:status response)))
              (is (empty? (:body response)))))

          (testing "Template no longer exists after deletion"
            (let [response ((app) get-request)]
              (is (= 404 (:status response))))))))))
