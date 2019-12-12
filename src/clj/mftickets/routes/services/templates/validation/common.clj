(ns mftickets.routes.services.templates.validation.common
  (:require [mftickets.domain.templates :as domain.templates]))

(def validations
  "Common validations for template create and upate."
  [{:validation/id
    ::repeated-name

    :validation/message
    "A template with this name and project already exists!"

    :validation/check-fn
    (fn [{{old-name :name} :old-template {new-name :name project-id :project-id} :new-template}]
      (when (not= old-name new-name)
        (not (domain.templates/unique-template-name-for-project? new-name project-id))))}])
