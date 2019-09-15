(ns mftickets.db.prefill.example-template
  "A ns responsible for prefilling an example template on the db.")

(def example-template-prefill
  "A sequence of args for jdbc/insert!providing an example template to be prefilled on the db."
  [:templates
   {:id 1 :projectId 1 :name "First Template" :creationDate "2019-09-14T19:08:45"}

   :templateSections
   {:id 1 :templateId 1 :name "First Template Single Section"}

   :templateSectionProperties
   {:id 1
    :templateSectionId 1
    :name "Title"
    :isMultiple false
    :valueType "section.property.value.types/text"}

   :templateSectionProperties
   {:id 2
    :templateSectionId 1
    :name "State"
    :isMultiple false
    :valueType "section.property.value.types/radio"}

   :templateSectionProperties
   {:id 3
    :templateSectionId 1
    :name "Start Date"
    :isMultiple false
    :valueType "section.property.value.types/date"}

   :templateSectionProperties
   {:id 4
    :templateSectionId 1
    :name "Description"
    :isMultiple false
    :valueType "section.property.value.types/text"}

   :templateSectionProperties
   {:id 5
    :templateSectionId 1
    :name "Updates"
    :isMultiple true
    :valueType "section.property.value.types/text"}

   :templateSectionPropertyTextValue
   {:id 1
    :templateSectionPropertyId 1
    :length ":short"}

   :templateSectionPropertyTextValue
   {:id 2
    :templateSectionPropertyId 4
    :length ":large"}

   :templateSectionPropertyTextValue
   {:id 3
    :templateSectionPropertyId 5
    :length ":large"}

   :templateSectionPropertyRadioValue
   {:id 1 :templateSectionPropertyId 2}

   :templateSectionPropertyRadioValueOptions
   {:id 1 :templateSectionPropertyRadioValueId 1 :value "Open"}

   :templateSectionPropertyRadioValueOptions
   {:id 2 :templateSectionPropertyRadioValueId 1 :value "OnGoing"}

   :templateSectionPropertyRadioValueOptions
   {:id 3 :templateSectionPropertyRadioValueId 1 :value "Closed"}

   :templateSectionPropertyDateValue
   {:id 1 :templateSectionPropertyId 3}])

