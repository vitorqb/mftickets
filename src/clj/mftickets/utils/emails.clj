(ns mftickets.utils.emails
  "Misc. utilities to send emails."
  (:require
   [clojure.spec.alpha :as s]
   [clj-http.client :as http.client]
   [mftickets.config :as config]))

(s/def ::spec-email-and-text-body
  (s/keys :req-un [::email ::text-body]))

(def email-sender "mftickets@mftickets.com")
(def email-subject "mftickets key!")
(def mailgun-prefix "https://api.mailgun.net/v3/")
(def mailgun-sufix "/messages")

(defn- gen-mailgun-url
  "Returns the mailgun url to call a given mailgun domain."
  [mailgun-domain]
  (str mailgun-prefix mailgun-domain mailgun-sufix))

(defn- post-email!
  "Runs a post http request to send the email."
  [{:keys [url email text-body] :as params}]
  (http.client/post
   url
   {:basic-auth ["api" (config/env :mailgun-api-key)]
    :form-params {:from email-sender
                  :to email
                  :subject email-subject
                  :text text-body}}))

(defn send-email!
  "Sends an email!"
  [params]
  {:pre [(s/valid? ::spec-email-and-text-body params)]}
  (let [url (-> :mailgun-domain config/env gen-mailgun-url)]
    (post-email! (assoc params :url url))))
