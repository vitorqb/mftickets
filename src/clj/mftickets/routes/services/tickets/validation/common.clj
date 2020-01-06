(ns mftickets.routes.services.tickets.validation.common
  (:require [mftickets.domain.templates :as domain.templates]
            [mftickets.domain.templates.properties :as domain.templates.properties]))

(def validations
  "Common validations for when creating/updating a ticket."
  [{:validation/id
    ::valid-template

    :validation/message
    "Invalid template id!"

    :validation/check-fn
    (fn [{{:keys [created-by-user-id template-id]} :new-ticket}]
      (not (domain.templates/user-has-access-to-template? {:id created-by-user-id} {:id template-id})))}

   {:validation/id
    ::all-properties-ids-belong-to-ticket

    :validation/message
    "The properties for all properties values must belong to the ticket's template!"

    :validation/check-fn
    (fn [{:keys [new-ticket]}]
      (let [new-ticket-properties-ids
            (->> new-ticket :properties-values (map :property-id))

            template-properties-ids-set
            (-> new-ticket
                :template-id
                domain.templates.properties/get-properties-ids-set-for-template-id)]

        (not (every? template-properties-ids-set new-ticket-properties-ids))))}])
