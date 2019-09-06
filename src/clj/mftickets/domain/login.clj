(ns mftickets.domain.login
  (:require
   [mftickets.db.core :as db.core]
   [mftickets.db.login :as db.login]
   [mftickets.utils.emails :as utils.emails]))

(def uppercase-letters (map char (range 65 91)))
(def lowercase-letters (map char (range 97 123)))
(def numbers (map char (range 48 58)))
(def chars-sample (-> (concat uppercase-letters lowercase-letters numbers)))
(def key-value-length 30)

(defn send-key-email-text-body
  "The text body to be sent in an email."
  [value]
  (str "Your key is: \n \n" value))

(defn generate-random-key-value
  "Generates a new random key value."
  []
  (->> #(rand-nth chars-sample)
       (repeatedly key-value-length)
       (apply str)))

(defn create-user-key!
  "Creates a new key for a user, invalidating all other user keys."
  [{:keys [user-id value]}]
  (db.core/run-effects!
   [db.login/invalidate-user-keys! {:user-id user-id}]
   [db.login/create-user-key! {:user-id user-id :value value}]))

(defn send-key!
  "Sends a key to an email"
  [{:keys [email user-key]}]
  (let [text-body (-> user-key :value send-key-email-text-body)]
    (utils.emails/send-email! {:email email :text-body text-body})))
