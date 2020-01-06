-- :name create-raw-ticket!* :insert :raw
-- :doc Creates a raw ticket, without any depending objects.
INSERT INTO tickets (templateId, createdAt, createdByUserId)
VALUES (:template-id, :created-at, :created-by-user-id);

-- :name get-raw-ticket* :result :1
-- :doc Gets a raw ticket (without related objects)
SELECT id, templateId, createdAt, createdByUserId FROM tickets WHERE id = :id;
