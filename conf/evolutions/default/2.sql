# Update Post

# --- !Ups
ALTER TABLE Post ALTER COLUMN posted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP;
