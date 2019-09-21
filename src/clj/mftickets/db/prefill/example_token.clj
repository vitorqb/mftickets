(ns mftickets.db.prefill.example-token
  "A ns responsible for prefilling an example token on the db.")

(def example-token
  "A sequence of args for jdbc/insert! providing an example token."
  [:userLoginTokens
   {:id 1 :userId 1 :value "foo" :createdAt "2019-09-21T00:00:00"}])

