(ns mftickets.routes.services.templates.validation)

;; Helpers
(defmacro deferr
  "Defines an error (with keyword and name) and a checker function for that error.
  This runs `def` twice, first binding an array with [err-keyword err-message] for the
  message under the name `err-name`, and then binding the checker function to `err-name`
  prefixed by `check-`"
  [err-name err-msg checker]
  (let [err-name-str (str err-name)
        validation-fn-name (-> (str "check-" err-name-str) symbol)]
    `(do
       (def ~err-name
         [(keyword (str (ns-name ~*ns*)) ~err-name-str) ~err-msg])
       (def ~validation-fn-name ~checker))))

;; Errors
(deferr project-id-missmatch
  "The project id does not match the template project."
  (fn [old-template new-template]
    (not= (:project-id old-template) (:project-id new-template))))

(deferr id-missmatch
  "Can not change a template's id"
  (fn [old-template new-template]
    (not= (:id old-template) (:id new-template))))

(deferr section-template-id-missmatch
  "The template-id of at least one of the sections does not match the template's id."
  (fn [_ new-template]
    (some
     (fn [section] (not= (:template-id section) (:id new-template)))
     (:sections new-template))))

(deferr property-section-id-missmatch
  "The template-section-id of at least one of the properties does not match it's section id."
  (fn [_ new-template]
    (some
     (fn [section]
       (some
        (fn [property] (not= (:template-section-id property) (:id section)))
        (:properties section)))
     (:sections new-template))))

(deferr created-at-missmatch
  "Can not change the date of creation of a template."
  (fn [old-template new-template]
    (not= (:creation-date old-template) (:creation-date new-template))))

(def all-validations
  [[check-id-missmatch id-missmatch]
   [check-project-id-missmatch project-id-missmatch]
   [check-section-template-id-missmatch section-template-id-missmatch]
   [check-property-section-id-missmatch property-section-id-missmatch]
   [check-created-at-missmatch created-at-missmatch]])

;; Validation
(defn validate-template
  "Validates the change from old template to new template."
  [old-template new-template]
  (loop [[validation & todo] all-validations]
    (let [[validation-fn err] validation]
      (cond
        (and (nil? validation-fn) (empty? todo)) new-template
        (nil? validation) (recur todo)
        (validation-fn old-template new-template) err
        :else (recur todo)))))
