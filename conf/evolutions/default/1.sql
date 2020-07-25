# Users schema

# --- !Ups

CREATE TABLE User (
    id varchar(36) NOT NULL,
    name varchar(255) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE (name)
);

SET @user_1 = '11111111-1111-1111-1111-111111111111';
SET @user_2 = '22222222-2222-2222-2222-222222222222';
SET @user_3 = '33333333-3333-3333-3333-333333333333';

INSERT INTO User(id, name) VALUES
     (@user_1, 'alice')
    ,(@user_2, 'bob')
    ,(@user_3, 'charlie');

# --- !Downs
DROP TABLE IF EXISTS User;

# Add Post

# --- !Ups
CREATE TABLE Post (
    id UUID NOT NULL DEFAULT RANDOM_UUID(),
    user_id varchar(36) NOT NULL,
    text varchar(255) NOT NULL,
    comment_count int NOT NULL,
    posted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES User(id),
    PRIMARY KEY (id)
);

# --- !Downs
DROP TABLE IF EXISTS Post;

# Add Comment

# --- !Ups
CREATE TABLE Comment (
    id UUID NOT NULL DEFAULT RANDOM_UUID(),
    user_id varchar(36) NOT NULL,
    text varchar(255) NOT NULL,
    parent_post_id UUID NOT NULL,
    comment_count int NOT NULL,
    posted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
    FOREIGN KEY (user_id) REFERENCES User(id),
    PRIMARY KEY (id)
);

# --- !Downs
DROP TABLE IF EXISTS Comment;
