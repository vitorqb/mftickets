(ns mftickets.system
  "Helper ns with commands to be run from shell during development."
  (:require [clojure.java.io :as io]
            clojure.test
            [clojure.tools.namespace.find :as find]
            [luminus-migrations.core :as migrations]
            [mftickets.db.core :as db.core]
            [mftickets.test-utils :as tu]))

(defn run-tests
  "Migrate and run all tests."
  []
  (migrations/migrate ["migrate"] {:database-url tu/test-db})
  (let [nses (find/find-namespaces [(io/file "test")])]
    (dorun (map require nses))
    (let [{:keys [fail error]} (apply clojure.test/run-tests nses)]
      (when-not (= [0 0] [fail error])
        (System/exit 1)))))
