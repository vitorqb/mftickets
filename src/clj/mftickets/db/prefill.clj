(ns mftickets.db.prefill
  "A namespace responsible for pre-filling information on the db."
  (:require
   [clojure.java.jdbc :as jdbc]
   [clojure.core.match :refer [match]]
   [mftickets.db.prefill.example-template :as db.prefill.example-template]))

(defn- parse-args
  "Parses a sequence of [:tableName {args}] into a list of jdbc/insert! calls."
  [[tableName args & nextArgs] {:keys [db]}]
  {:pre [(not (nil? db))]}
  (loop [tableName* tableName
         args* args
         todo nextArgs
         done []]
    (match [tableName* args*]
      [nil nil] done
      [nil _]   (throw (ex-info "Invalid arguments: args was nil but tableName not." {}))
      [_ nil]   (throw (ex-info "Invalid arguments: tableName was nil but args not." {}))
      [_ _]     (let [result [jdbc/insert! db tableName* args*]
                      nextDone (conj done result)
                      [nextTableName nextArgs & nextTodo] todo]
                  (recur nextTableName nextArgs nextTodo nextDone)))))

(defn prefill-effects
  "Returns a map of prefill effects, that can be run using db.core/run!"
  [{:keys [db] :as opts}]
  {:example-template (parse-args db.prefill.example-template/example-template-prefill opts)})

(defn run-prefills!
  "Runs all prefills registered."
  [db]
  (jdbc/with-db-transaction [tra db]
    (let [prefills (prefill-effects {:db tra})]
      (doseq [[key prefill] prefills]
        (println "Running prefill " key)
        (doseq [[fn & args] prefill]
          (clojure.pprint/pprint (drop 1 args))
          (apply fn args))))))
