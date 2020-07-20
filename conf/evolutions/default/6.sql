# Update Comment

# --- !Ups
ALTER TABLE Comment alter column posted_at datetime default current_timestamp

# Update Post

# --- !Ups
ALTER TABLE Post alter column posted_at datetime default current_timestamp