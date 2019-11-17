-- :name get-section* :result :1
-- :doc Return a section by id
SELECT id, templateId, name
FROM templateSections
WHERE id = :id;

-- :name get-sections-for-template* :result :*
-- :doc Return all sections for a template
SELECT id, templateId, name
FROM templateSections
WHERE templateId = :template-id;

-- :name get-sections-for-templates-ids* :result :*
-- :doc Return all sections for a list of template ids.
SELECT id, templateId, name
FROM templateSections
WHERE templateId IN (:v*:templates-ids);

-- :name update-raw-section!* :! :n
-- :doc Updates a section
UPDATE templateSections
SET name = :name
WHERE id = :id;

-- :name create-section!* :insert :raw
-- :doc Creates a section
INSERT INTO templateSections (templateId, name)
VALUES (:template-id, :name);

-- :name delete-section!* :! :n
-- :doc Deletes a section by id
DELETE FROM templateSections WHERE id = :id;
