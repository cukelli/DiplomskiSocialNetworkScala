package util

import org.mindrot.jbcrypt.BCrypt

object Utils {

  def hashPassword(password: String): String = BCrypt.hashpw(password, BCrypt.gensalt())

  def checkPassword(password: String, hashedPassword: String): Boolean = BCrypt.checkpw(password, hashedPassword)

  def encodePassword(password: String): String = hashPassword(password)


}
