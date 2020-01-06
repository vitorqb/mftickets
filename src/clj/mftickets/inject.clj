(ns mftickets.inject
  (:require [mftickets.domain.projects :as domain.projects]
            [mftickets.domain.templates :as domain.templates]
            [mftickets.domain.templates.inject :as domain.templates.inject]
            [mftickets.domain.templates.properties :as domain.templates.properties]
            [mftickets.domain.templates.sections :as domain.templates.sections]
            [mftickets.domain.templates.sections.inject
             :as
             domain.templates.sections.inject]
            [mount.core :refer [defstate]]
            [mftickets.domain.tickets.inject :as domain.tickets.inject]
            [mftickets.domain.tickets.properties-values
             :as
             domain.tickets.properties-values]
            [mftickets.domain.tickets.properties-values.create
             :as
             domain.tickets.properties-values.create]
            [mftickets.domain.tickets.properties-values.create.inject
             :as
             domain.tickets.properties-values.create.inject]
            [mftickets.domain.tickets.properties-values.get
             :as
             domain.tickets.properties-values.get]
            [mftickets.domain.tickets.properties-values.get.inject
             :as
             domain.tickets.properties-values.get.inject]))

;; !!!! TODO -> Don't use injection for hierarchical imports, like
;; !!!!         templates -> templates.properties
(defstate inject
  :start 
  {::domain.projects/count-templates
   domain.templates/count-templates-for-project

   ::domain.templates.inject/get-properties-for-templates
   domain.templates.properties/get-properties-for-templates

   ::domain.templates.inject/get-sections-for-templates
   domain.templates.sections/get-sections-for-templates

   ::domain.templates.inject/update-section!
   domain.templates.sections/update-section!

   ::domain.templates.inject/create-section!
   domain.templates.sections/create-section!

   ::domain.templates.inject/delete-section!
   domain.templates.sections/delete-section!

   ::domain.templates.sections.inject/create-property!
   domain.templates.properties/create-property!

   ::domain.templates.sections.inject/update-property!
   domain.templates.properties/update-property!

   ::domain.templates.sections.inject/delete-property!
   domain.templates.properties/delete-property!

   ::domain.tickets.properties-values.get.inject/get-property
   domain.templates.properties/get-property

   ::domain.templates.sections.inject/get-properties-for-section
   domain.templates.properties/get-properties-for-section

   ::domain.tickets.inject/create-property-value!
   domain.tickets.properties-values.create/create-property-value!

   ::domain.tickets.inject/get-properties-for-ticket
   domain.templates.properties/get-properties-for-ticket
   
   ::domain.tickets.properties-values.create.inject/get-property
   domain.templates.properties/get-property
   })
