(ns mftickets.routes.services.templates.validation.update
  (:require [mftickets.domain.templates :as domain.templates]
            [mftickets.routes.services.templates.validation.common :as templates.validation.common]))

(def validations*

  [{:validation/id
    ::project-id-missmatch

    :validation/message
    "The project id does not match the template project."

    :validation/check-fn
    (fn [{:keys [old-template new-template]}]
      (not= (:project-id old-template) (:project-id new-template)))}


   {:validation/id
    ::id-missmatch

    :validation/message
    "Can not change a template's id"

    :validation/check-fn
    (fn [{:keys [old-template new-template]}]
      (not= (:id old-template) (:id new-template)))}


   {:validation/id
    ::section-template-id-missmatch

    :validation/message
    "The template-id of at least one of the sections does not match the template's id."

    :validation/check-fn
    (fn [{:keys [new-template]}]
      (some
       (fn [section] (not= (:template-id section) (:id new-template)))
       (:sections new-template)))}


   {:validation/id
    ::property-section-id-missmatch

    :validation/message
    "The template-section-id of at least one of the properties does not match it's section id."

    :validation/check-fn
    (fn [{:keys [new-template]}]
      (some
       (fn [section]
         (some
          (fn [property] (not= (:template-section-id property) (:id section)))
          (:properties section)))
       (:sections new-template)))}


   {:validation/id
    ::created-at-missmatch

    :validation/message
    "Can not change the date of creation of a template."

    :validation/check-fn
    (fn [{:keys [old-template new-template]}]
      (not= (:creation-date old-template) (:creation-date new-template)))}])

(def validations (concat templates.validation.common/validations validations*))
