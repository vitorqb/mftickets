(ns mftickets.validation.core
  "Defines the public API for the validation system of incoming data."
  (:require [clojure.core.match :as match]
            [clojure.spec.alpha :as spec]))

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

(defmacro if-let-err
  "Evaluates the validation expression. If if evaluates to an error, evaluates the `if`
  block with the binding. Else, evaluates the `else` block.
  Same as:
  `(match/match validation
     :validation/success
     else

     err
     then)`"
  {:style/indent 1}
  [[err-sym validate-expr] then else]
  {:pre [(symbol? err-sym)]}
  `(match/match ~validate-expr
     :validation/success
     ~else

     ~err-sym
     ~then))
