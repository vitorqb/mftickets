-- :name invalidate-user-keys! :! :n
-- :doc Invalid all keys for an user.
UPDATE userLoginKeys
SET isValid = FALSE
WHERE userId = :value:user-id;

-- :name create-user-key!* :insert :raw
-- :doc Creates an user key.
INSERT INTO userLoginKeys (userId, value, isValid, createdAt)
VALUES (:user-id, :value, :is-valid?, :created-at);

-- :name get-user-key* :result :1
-- :doc Retrieves an user key.
SELECT id, userId, value, isValid, createdAt
FROM userLoginKeys
WHERE id = :id

-- :name create-user-token!* :insert :raw
-- :doc Creates a token for an user
INSERT INTO userLoginTokens (userId, value, createdAt)
VALUES (:user-id, :value, :created-at)

-- :name get-user-token* :result :1
-- :doc Retrieves an user token.
SELECT id, userId, value, hasBeenInvalidated, createdAt
FROM userLoginTokens
WHERE id = :id

-- :name user-key-exists?* :result :1
-- :doc Exists query for wheter a valid user key exists.
SELECT EXISTS
(SELECT * FROM userLoginKeys
 WHERE userId = :user-id
 AND isValid IS TRUE
 AND value = :value
) AS response
FROM userLoginKeys

-- :name get-user-id-from-token-value* :result :1
-- :doc Retrieves the user-id given a token value.
SELECT userId
FROM userLoginTokens
WHERE value = :token-value
AND hasBeenInvalidated is FALSE

-- :name is-valid-token-value?* :result :1
-- :doc Exists query for whether a token value is valid.
SELECT EXISTS
(SELECT * FROM userLoginTokens
 WHERE value = :value
 AND hasBeenInvalidated IS FALSE
) AS response
FROM userLoginTokens
