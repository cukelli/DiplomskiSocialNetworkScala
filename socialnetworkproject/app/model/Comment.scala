package model

import java.time.LocalDateTime

case class Comment(
                    UserCommented: String,
                    DateTImeCreated: LocalDateTime,
                    CommentText: String
                  )
