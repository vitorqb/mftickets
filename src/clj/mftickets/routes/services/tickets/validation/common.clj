(ns mftickets.routes.services.tickets.validation.common
  (:require [mftickets.domain.templates :as domain.templates]))

(def validations
  "Common validations for when creating/updating a ticket."
  [{:validation/id
    ::valid-template

    :validation/message
    "Invalid template id!"

    :validation/check-fn
    (fn [{{:keys [created-by-user-id template-id]} :new-ticket}]
      (not (domain.templates/user-has-access-to-template? {:id created-by-user-id} {:id template-id})))}])
