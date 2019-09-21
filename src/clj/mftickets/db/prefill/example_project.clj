(ns mftickets.db.prefill.example-project
  "A ns responsible for prefilling an example project on the db.")

(def example-project
  "A sequence of args for jdbc/insert! providing an example project."
  [:projects
   {:id 1 :name "My First Project" :description "This is my first project!"}])
