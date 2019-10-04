(ns mftickets.routes.services.app-metadata-test
  (:require [clojure.test :as t :refer [are deftest is testing use-fixtures]]
            [mftickets.routes.services.app-metadata :as sut]
            [mftickets.test-utils :as tu]
            [ring.mock.request :as mock.request]
            [mftickets.handler :refer [app]]))

(deftest test-handle-get

  (testing "Integration"
    (tu/with-db
      (tu/with-app
        (tu/with-user-and-token [user token]

          (testing "No projects"
            (is (= {:projects []}
                   (-> (mock.request/request :get "/api/app-metadata")
                       (tu/auth-header token)
                       ((app))
                       (tu/decode-response-body)))))

          (let [project (tu/gen-save! tu/project)]
            (tu/gen-save! tu/users-projects {:user-id (:id user) :project-id (:id project)})

            (testing "With projects"
              (is (= {:projects [project]}
                     (-> (mock.request/request :get "/api/app-metadata")
                         (tu/auth-header token)
                         ((app))
                         (tu/decode-response-body)))))))))))
