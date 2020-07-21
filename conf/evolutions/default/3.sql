# Add Comment

# --- !Ups
CREATE TABLE Comment (
    id UUID DEFAULT RANDOM_UUID(),
    user_id varchar(36) NOT NULL,
    text varchar(255) NOT NULL,
    parent_post_id varchar(36) NOT NULL,
    comment_count int NOT NULL,
    posted_at datetime DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES User(id),
    FOREIGN KEY (parent_post_id) REFERENCES Post(id),
    PRIMARY KEY (id)
);

# --- !Downs
DROP TABLE IF EXISTS Comment;


