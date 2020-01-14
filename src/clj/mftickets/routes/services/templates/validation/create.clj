(ns mftickets.routes.services.templates.validation.create
  (:require [com.rpl.specter :as s]
            [mftickets.routes.services.templates.validation.common :as common]))

;; Validations
(def ^:private create-validations [])

(def validations (concat common/validations create-validations))
