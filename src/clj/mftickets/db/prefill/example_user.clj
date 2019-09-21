(ns mftickets.db.prefill.example-user
  "A ns responsible for prefilling an example user on the db.")

(def example-user
  "A sequence of args for jdbc/insert! providing an example user."
  [:users {:id 1 :email "test@user.com"}
   :usersProjects {:userId 1 :projectId 1}])

