-- :snip select-snip
SELECT
  sections.id,
  sections.templateId,
  sections.name,
  sections.orderIndex as 'order'
FROM templateSections as sections

-- :name get-section* :result :1
-- :doc Return a section by id
:snip:select WHERE sections.id = :id;

-- :name get-sections-for-template* :result :*
-- :doc Return all sections for a template
:snip:select WHERE sections.templateId = :template-id;

-- :name get-sections-for-templates-ids* :result :*
-- :doc Return all sections for a list of template ids.
:snip:select WHERE sections.templateId IN (:v*:templates-ids);

-- :name update-raw-section!* :! :n
-- :doc Updates a section
UPDATE templateSections
SET name = :name, orderIndex = :order
WHERE id = :id;

-- :name create-section!* :insert :raw
-- :doc Creates a section
INSERT INTO templateSections (templateId, name, orderIndex)
VALUES (:template-id, :name, :order);

-- :name delete-section!* :! :n
-- :doc Deletes a section by id
DELETE FROM templateSections WHERE id = :id;
