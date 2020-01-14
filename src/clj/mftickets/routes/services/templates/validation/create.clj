(ns mftickets.routes.services.templates.validation.create
  (:require [com.rpl.specter :as s]
            [mftickets.routes.services.templates.validation.common :as common]))

;; Helpers
(def ^:private path-properties
  (s/path [:sections s/ALL :properties s/ALL]))

(def ^:private path-radio-options
  (s/path [path-properties :templates.properties.types.radio/options s/ALL]))

(defn- get-sections-ids [template]
  (s/select [:sections s/ALL :id] template))

(defn- get-sections-template-ids [template]
  (s/select [:sections s/ALL :template-id] template))

(defn- get-properties-ids [template]
  (s/select [path-properties :id] template))

(defn- get-properties-sections-ids [template]
  (s/select [path-properties :template-section-id] template))

(defn- get-radio-options-ids [template]
  (s/select [path-radio-options :id] template))

(defn- get-radio-options-property-ids [template]
  (s/select [path-radio-options :property-id] template))

;; Validations
(def ^:private create-validations
  [{:validation/id
    ::template-id-must-be-nil

    :validation/message
    "Can not set id for template creation."

    :validation/check-fn
    (fn [{{id :id} :new-template}]
      (not (nil? id)))}

   {:validation/id
    ::creation-date-must-be-nil

    :validation/message
    "Can not set Creation Date for template creation."

    :validation/check-fn
    (fn [{{creation-date :creation-date} :new-template}]
      (not (nil? creation-date)))}

   {:validation/id
    ::section-ids-must-be-nil

    :validation/message
    "Can not set Section Id for template creation."

    :validation/check-fn
    (fn [{new-template :new-template}]
      (not-every? nil? (get-sections-ids new-template)))}

   {:validation/id
    ::section-template-ids-must-be-nil

    :validation/message
    "Can not set Section Template Id for template creation."

    :validation/check-fn
    (fn [{new-template :new-template}]
      (not-every? nil? (get-sections-template-ids new-template)))}

   {:validation/id
    ::properties-ids-must-be-nil

    :validation/message
    "Can not set Property Id for template creation."

    :validation/check-fn
    (fn [{new-template :new-template}]
      (not-every? nil? (get-properties-ids new-template)))}

   {:validation/id
    ::properties-template-section-ids-must-be-nil

    :validation/message
    "Can not set Property Template Section Id for template creation."

    :validation/check-fn
    (fn [{new-template :new-template}]
      (not-every? nil? (get-properties-sections-ids new-template)))}

   {:validation/id
    ::radio-options-ids-must-be-nil

    :validation/message
    "Can not set Id for Radio Options during template creation."

    :validation/check-fn
    (fn [{new-template :new-template}]
      (not-every? nil? (get-radio-options-ids new-template)))}

   {:validation/id
    ::radio-options-properties-ids-must-be-nil

    :validation/message
    "Can not set Property Id for Radio Options during template creation."

    :validation/check-fn
    (fn [{new-template :new-template}]
      (not-every? nil? (get-radio-options-property-ids new-template)))}])

(def validations (concat common/validations create-validations))
