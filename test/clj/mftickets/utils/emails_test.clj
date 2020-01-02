(ns mftickets.utils.emails-test
  (:require [mftickets.utils.emails :as sut]
            [mftickets.config :as config]
            [clj-http.client :as http.client]
            [clojure.test :as t :refer [is are testing deftest use-fixtures]]
            [mftickets.test-utils :as tu]))

(use-fixtures :once tu/common-fixture)

(deftest test-gen-mailgun-url
  (let [gen-mailgun-url #'sut/gen-mailgun-url]
    (is (= "https://api.mailgun.net/v3/foo/messages"
           (gen-mailgun-url "foo")))))

(deftest test-post-email!

  (let [post-email! #'sut/post-email!
        gen-mailgun-url #'sut/gen-mailgun-url]
    (with-redefs [http.client/post #(vector %1 %2)]

      (testing "Sends correct url"
        (tu/with-app
          (let [[url _] (post-email! {:url "foo"})]
            (is (= "foo" url)))))

      (testing "Sends with basic auth"
        (tu/with-app
          (let [[_ {:keys [basic-auth]}] (post-email! {})]
            (is (= ["api" (config/env :mailgun-api-key)]
                   basic-auth)))))

      (testing "Sends with correct payload"
        (tu/with-app
          (let [[_ {:keys [form-params]}] (post-email! {:email "foo" :text-body "bar"})]
            (is (= {:from sut/email-sender :to "foo" :subject sut/email-subject :text "bar"}
                   form-params))))))))

(deftest test-send-email!
  (tu/with-app
    (let [send-email! #'sut/send-email!
          gen-mailgun-url #'sut/gen-mailgun-url]
      (with-redefs [sut/post-email! identity]
        (is (= {:email "foo" :text-body "bar" :url (gen-mailgun-url (config/env :mailgun-domain))}
               (send-email! {:email "foo" :text-body "bar"})))))))
