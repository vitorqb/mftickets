(ns mftickets.routes.services.projects-test
  (:require [mftickets.routes.services.projects :as sut]
            [clojure.test :as t :refer [is are deftest testing use-fixtures]]
            [mftickets.test-utils :as tu]
            [ring.mock.request :as mock.request]
            [mftickets.handler :refer [app]]
            [mftickets.middleware.context :as middleware.context]
            [mftickets.domain.projects :as domain.projects]))

(deftest test-handle-get
  (let [handle-get #'sut/handle-get]
    (is (= {:status 200 :body {:id 9}}
           (handle-get #::middleware.context{:project {:id 9}})))))

(deftest test-handle-put
  (let [handle-put #'sut/handle-put
        project {:id 1 :name "Old N" :description "Old D"}
        params {:name "New N" :description "New D"}
        request {::middleware.context/project project
                 :parameters {:body params}}]
    (with-redefs [domain.projects/update-project!
                  (fn [project* params*]
                    (if (and (= project* project) (= params* params))
                      (merge project* params*)))]
      (is (= {:status 200 :body (merge project params)} (handle-put request))))))

(deftest test-handler-post
  (let [handle-post #'sut/handle-post
        user {:id 921}
        body {:name "Foo" :description "Bar!"}
        request {:parameters {:body body} :mftickets.auth/user user}
        new-project (assoc body :id 2)]
    (with-redefs [domain.projects/create-project!
                  #(if (= % (assoc body :user user)) new-project)]
      (is (= (handle-post request)
             {:status 200 :body new-project})))))

(deftest test-handle-get-projects
  (let [handle-get-projects #'sut/handle-get-projects 
        user {:id 999}
        projects [{:id 1} {:id 2}]
        request {:mftickets.auth/user user}]
    (with-redefs [domain.projects/get-projects-for-user #(if (= % user) projects)]
      (is (= {:status 200 :body projects} (handle-get-projects request))))))

(deftest test-integration

  (testing "Base integration test for get, get list and post"
    (tu/with-app
      (tu/with-db
        (tu/with-user-and-token [user token]

          (testing "Get single returns 404 if project does not exist"
            (let [response (-> (mock.request/request :get "/api/projects/121321")
                               (tu/auth-header token)
                               ((app)))]
              (is (= 404 (:status response)))))

          ;; Adds a project
          (tu/gen-save! tu/project {:id 99})

          (testing "Get single returns 404 if user has no acces to project"
            (let [response (-> (mock.request/request :get "/api/projects/99")
                               (tu/auth-header token)
                               ((app)))]
              (is (= 404 (:status response)))))

          (testing "Get many returns empty list if user has no acces to any project"
            (let [response (-> (mock.request/request :get "/api/projects")
                               (tu/auth-header token)
                               ((app)))]
              (is (= 200 (:status response)))
              (is (= [] (-> response tu/decode-response-body)))))

          ;; Assign to the user
          (tu/gen-save! tu/users-projects {:user-id (:id user) :project-id 99})

          (testing "Get single returns 200 with some body if user has access to project"
            (let [response (-> (mock.request/request :get "/api/projects/99")
                               (tu/auth-header token)
                               ((app)))]
              (is (= 200 (:status response)))
              (is (not (nil? (:body response))))
              (is (= 99 (-> response tu/decode-response-body :id)))))

          (testing "Get many returns 200 with project user has access to"
            (let [response (-> (mock.request/request :get "/api/projects")
                               (tu/auth-header token)
                               ((app)))
                  body (-> response tu/decode-response-body)]
              (is (= 200 (:status response)))
              (is (= 1 (count body)))
              (is (= 99 (-> body first :id)))))

          ;; Now the user uses post to create a new project
          (testing "Creating a project returns 200 with the created project"
            (let [response (-> (mock.request/request :post "/api/projects")
                               (mock.request/json-body {:name "Foo123" :description "Bar"})
                               (tu/auth-header token)
                               ((app)))]
              (is (= 200 (:status response)))
              (is (= (-> response tu/decode-response-body (dissoc :id))
                     {:name "Foo123" :description "Bar"}))))

          ;; And when he queries for this projects, he sees it!
          (testing "User can see projects he creates with post"
            (let [body (-> (mock.request/request :get "/api/projects")
                           (tu/auth-header token)
                           ((app))
                           tu/decode-response-body)]
              (is (some #(and (= (:name %) "Foo123") (= (:description %) "Bar") )
                        body))))

          ;; Finally, he modifies the name and sees it updated
          (testing "Modifying the name of a project with PUT returns 200 with modified project."
            (let [response (-> (mock.request/request :put "/api/projects/99")
                               (mock.request/json-body {:name "NEW NAME"
                                                        :description "NEW DESCRIPTION"})
                               (tu/auth-header token)
                               ((app)))
                  body (tu/decode-response-body response)]
              (is (= 200 (:status response)))
              (is (= 99 (:id body)))
              (is (= "NEW NAME" (:name body)))
              (is (= "NEW DESCRIPTION" (:description body))))))))))
