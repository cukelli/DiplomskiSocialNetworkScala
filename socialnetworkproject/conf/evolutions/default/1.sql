-- !Ups

CREATE TABLE Users (
   userID BIGINT NOT NULL AUTO_INCREMENT,
   email varchar(200) NOT NULL UNIQUE,
   lastName varchar(200) NOT NULL,
   firstName varchar(200) NOT NULL,
   password varchar(200) NOT NULL,
   gender ENUM('M','F'),
   role ENUM('USER','ADMIN'),
   profilePicture varchar(2000),
   PRIMARY KEY(userID)
);


CREATE TABLE Post (
   postID BIGINT NOT NULL AUTO_INCREMENT,
   dateTimeCreated TIMESTAMP NOT NULL,
   title varchar(200) NOT NULL,
   description varchar(500) NOT NULL,
   userPosted BIGINT NOT NULL,
   FOREIGN KEY (userPosted) REFERENCES Users(userID) ON DELETE CASCADE,
   primary key(postID)
);


CREATE TABLE FriendRequest (
   requestID BIGINT NOT NULL AUTO_INCREMENT,
   createdAt TIMESTAMP NOT NULL,
   sentBy BIGINT NOT NULL,
   sentFor BIGINT NOT NULL,
   status ENUM('PENDING','ACCEPTED', 'DECLINED'),
   FOREIGN KEY (sentBy) REFERENCES Users(userID) ON DELETE CASCADE,
   FOREIGN KEY (sentFor) REFERENCES Users(userID) ON DELETE CASCADE,
   PRIMARY KEY(requestID)
);

CREATE TABLE UserLike (
   userLiked BIGINT NOT NULL,
   postLiked BIGINT NOT NULL,
   FOREIGN KEY (userLiked) REFERENCES Users(userID) ON DELETE CASCADE,
   FOREIGN KEY (postLiked) REFERENCES Post(postID) ON DELETE CASCADE,
   PRIMARY KEY(userLiked, postLiked)
);

CREATE TABLE Image (
  postID BIGINT NOT NULL,
  imageID BIGINT NOT NULL,
  imagePath TEXT NOT NULL,
  PRIMARY KEY(postID, imageID)


)

-- !Downs

DROP TABLE UserLike;
DROP TABLE FriendRequest;
DROP TABLE Post;
DROP TABLE Users;
