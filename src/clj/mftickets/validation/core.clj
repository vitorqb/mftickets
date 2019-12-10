(ns mftickets.validation.core
  "Defines the public API for the validation system of incoming data."
  (:require [clojure.spec.alpha :as spec]))

(spec/def :validation/id keyword?)
(spec/def :validation/message string?)
(spec/def :validation/check-fn ifn?)
(spec/def :validation/validations
  (spec/coll-of
   (spec/keys
    :req [:validation/id :validation/message :validation/check-fn])))

(defn validate
  "Runs all validators agains a validation payload.
  Returns :validation/success if the validation was a success.
  If any validation fails, returns `[:error-key \"error message\"]`

  Each validation must be an object with
  - `id`: An identifier for this validation.
  - `message`: A user-friendly message if validation fails.
  - `check-fn`: A function that receives `data` and returns true if the validation failed."
  [validations data]

  {:pre [(spec/assert :validation/validations validations)]}

  (loop [[{:validation/keys [id message check-fn] :as validation} & todo] validations]
    (cond
      (and (nil? validation) (empty? todo)) :validation/success
      (nil? validation) (recur todo)
      (check-fn data) [id message]
      :else (recur todo))))
