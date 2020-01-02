(ns mftickets.middleware.context-test
  (:require [clojure.test :as t :refer [are deftest is testing use-fixtures]]
            [mftickets.domain.projects :as domain.projects]
            [mftickets.domain.users :as domain.users]
            [mftickets.middleware.context :as sut]
            [mftickets.test-utils :as tu]))

(use-fixtures :once tu/common-fixture)

(deftest test-wrap-get-project

  (let [wrap-get-project #'sut/wrap-get-project]

    (testing "Project not found"
      (with-redefs [domain.projects/get-project #(when-not (= % 1) ::found)]
        (let [opts {:not-found {:status 404}}
              handler (wrap-get-project identity opts)
              request {:parameters {:query {:project-id 1}}}
              response (handler {:parameters {:query {:project-id 1}}})]
          (is (= (:not-found opts) response)))))

    (testing "Project found"
      (with-redefs [domain.projects/get-project #(when (= % 1) ::project)]
        (let [handler (wrap-get-project identity)
              request {:parameters {:query {:project-id 1}}}
              response (handler request)]
          (is (= (assoc request ::sut/project ::project) response)))))

    (testing "Project found with specific request->project-id"
      (with-redefs [domain.projects/get-project #(when (= % 1) ::project)]
        (let [request {::custom-params {::project-id 1}}
              opts {:request->project-id #(-> % ::custom-params ::project-id)}
              handler (wrap-get-project identity opts)
              response (handler request)]
          (is (= (assoc request ::sut/project ::project) response)))))))

(deftest test-user-has-access-to-project?

  (let [user-has-access-to-project? #'sut/user-has-access-to-project?
        user-id 999
        user {:id user-id}]

    (with-redefs [domain.users/get-projects-ids-for-user #(if (= (:id %) user-id) #{1 2})]

      (testing "True"
        (is (true? (user-has-access-to-project? user {:id 1})))
        (is (true? (user-has-access-to-project? user {:id 2}))))

      (testing "False"
        (is (false? (user-has-access-to-project? user {:id 3})))
        (is (false? (user-has-access-to-project? user {:id 4})))))))

(deftest test-wrap-user-has-access-to-project?

  (let [user {:id 1}
        project {:id 2}]
    (with-redefs [sut/user-has-access-to-project?
                  (fn [user* project*] (and (= user user*) (= project project*)))]

      (testing "True"
        (let [handler (sut/wrap-user-has-access-to-project? identity)
              request {:mftickets.auth/user user ::sut/project project}
              response (handler request)]
          (is (= response request))))

      (testing "False"
        (let [opts {:no-access {:status 406}}
              handler (sut/wrap-user-has-access-to-project? identity opts)
              request {:mftickets.auth/user {:id 3}
                       ::sut/project project}
              response (handler request)]
          (is (= (:no-access opts) response)))))))
