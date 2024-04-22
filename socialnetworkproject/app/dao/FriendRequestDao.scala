package dao
import enumeration.FriendRequestStatus
import model.{FriendRequest, User}
import dao.UserDao
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import java.sql.Timestamp
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FriendRequestDao @Inject() (
    protected val dbConfigProvider: DatabaseConfigProvider,
    val userDao: UserDao
)(implicit
    executionContext: ExecutionContext
) extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  val FriendRequests = TableQuery[FriendRequestTable]

  def all(): Future[Seq[FriendRequest]] = db.run(FriendRequests.result)

  def getById(requestID: Long): Future[Option[FriendRequest]] =
    db.run(FriendRequests.filter(_.requestID === requestID).result.headOption)

  def getBySentByAndFor(
      sentBy: Long,
      sentFor: Long
  ): Future[Option[FriendRequest]] =
    db.run(
      FriendRequests
        .filter(row => row.sentBy === sentBy && row.sentFor === sentFor)
        .result
        .headOption
    )

  def getFriends(userSentBy: Long): Future[Seq[User]] = {
    getFriendsIds(userSentBy).flatMap { friendIds =>
      val friendFutures: Seq[Future[Option[User]]] =
        friendIds.map(userDao.getById)
      val futureSeqOptionUsers: Future[Seq[Option[User]]] =
        Future.sequence(friendFutures)
      futureSeqOptionUsers.map(_.flatten)
    }
  }

  def getFriendsIds(userId: Long): Future[Seq[Long]] = {
    val sentRequestsFuture = db.run(
      FriendRequests
        .filter(req =>
          req.sentBy === userId && req.status === FriendRequestStatus.ACCEPTED
        )
        .map(_.sentFor)
        .result
    )

    val receivedRequestsFuture = db.run(
      FriendRequests
        .filter(req =>
          req.sentFor === userId && req.status === FriendRequestStatus.ACCEPTED
        )
        .map(_.sentBy)
        .result
    )

    for {
      sentRequests <- sentRequestsFuture
      receivedRequests <- receivedRequestsFuture
    } yield sentRequests ++ receivedRequests
  }

  def create(friendRequest: FriendRequest): Future[Int] =
    db.run(FriendRequests += friendRequest)

  def update(friendRequest: FriendRequest): Future[Unit] =
    db.run(
      FriendRequests
        .filter(_.requestID === friendRequest.RequestID)
        .update(friendRequest)
    ).map(_ => ())

  def delete(requestID: Long): Future[Unit] =
    db.run(FriendRequests.filter(_.requestID === requestID).delete).map(_ => ())

  implicit val statusMapper: BaseColumnType[FriendRequestStatus.Status] =
    MappedColumnType.base[FriendRequestStatus.Status, String](
      e => e.toString,
      s => FriendRequestStatus.withName(s)
    )

  class FriendRequestTable(tag: Tag)
      extends Table[FriendRequest](tag, "FriendRequest") {
    def requestID = column[Long]("REQUESTID", O.PrimaryKey)

    def createdAt = column[Timestamp]("CREATEDAT")

    def sentBy = column[Long]("SENTBY")

    def sentFor = column[Long]("SENTFOR")

    def sentByFk = foreignKey("sent_by_fk", sentBy, userDao.Users)(_.userID)

    def sentForFk = foreignKey("sent_for_fk", sentFor, userDao.Users)(_.userID)

    def status = column[FriendRequestStatus.Status]("STATUS")

    def * = (requestID, createdAt, sentBy, sentFor, status) <>
      (FriendRequest.tupled, FriendRequest.unapply)

  }

}
