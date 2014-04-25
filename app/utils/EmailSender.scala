package utils

import javax.mail.internet.InternetAddress
import org.apache.commons.mail._

import scala.util.{Try, Failure, Success}
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

case class Email(
  bodyText: Option[String] = None,
  bodyHtml: Option[String] = None,
  charset: Option[String] = None,
  subject: Option[String] = None,
  from: Option[String] = None,
  replyTo: Option[String] = None,
  recipients: Seq[String] = Seq.empty,
  ccRecipients: Seq[String] = Seq.empty,
  bccRecipients: Seq[String] = Seq.empty,
  headers: Map[String, String] = Map.empty
)

trait EmailSender {
  def sendSync(email: Email): Try[Email]
  def sendAsync(email: Email)(implicit e: ExecutionContext): Future[Try[Email]] = Future { sendSync(email) }
}
object EmailSender extends EmailSender {

  private val confPrefix = "smtp"
  val conf = play.api.Play.current.configuration.getConfig(confPrefix).getOrElse(throwConfigurationNotFound)
  val mock: Boolean = conf.getBoolean("mock").getOrElse(false)
  val host: String = if (mock) "mock" else getString("host")
  val port: Int = conf.getInt("port").getOrElse(25)
  val ssl: Boolean = conf.getBoolean("ssl").getOrElse(false)
  val tls: Boolean = conf.getBoolean("tls").getOrElse(false)
  val user: Option[String] = conf.getString("user")
  val pass: Option[String] = conf.getString("pass")

  def get(log: String=>Unit = ((m: String) => play.api.Logger.debug(m))): EmailSender = {
    if (mock) EmailSenderMock(log)
    else EmailSenderReal(host, port, ssl, tls, user, pass)
  }

  def sendSync(email: Email): Try[Email] = get().sendSync(email)

  private def getString(key: String) = {
    conf.getString(key).getOrElse(throwMissingFieldError(key))
  }

  private def throwMissingFieldError(key: String) = {
    throw conf.globalError(
      "EmailSender - Unable to find key: [%s.%s]".format(confPrefix, key)
    )
  }

  private def throwConfigurationNotFound() = {
    throw play.api.Play.current.configuration.globalError(
      "EmailSender - Configuration not found: [%s]".format(confPrefix)
    )
  }

}

case class EmailSenderReal(
  host: String,
  port: Int,
  ssl: Boolean,
  tls: Boolean,
  user: Option[String],
  pass: Option[String]
) extends EmailSender {

  def sendSync(email: Email): Try[Email] = {
    Try {
      val aemail = createEmailer(email.bodyText, email.bodyHtml)
      aemail.setCharset(email.charset.getOrElse("utf-8"))
      aemail.setSubject(email.subject.getOrElse(""))
      email.from.foreach(setAddress(_) { (address, name) => aemail.setFrom(address, name) })
      email.replyTo.foreach(setAddress(_) { (address, name) => aemail.addReplyTo(address, name) })
      email.recipients.foreach(setAddress(_) { (address, name) => aemail.addTo(address, name) })
      email.ccRecipients.foreach(setAddress(_) { (address, name) => aemail.addCc(address, name) })
      email.bccRecipients.foreach(setAddress(_) { (address, name) => aemail.addBcc(address, name) })
      email.headers.foreach { case (k,v) => aemail.addHeader(k, v) }
      aemail.setHostName(host)
      aemail.setSmtpPort(port)
      aemail.setSSL(ssl)
      aemail.setTLS(tls)
      for(u <- user; p <- pass) yield aemail.setAuthenticator(new DefaultAuthenticator(u, p))
      aemail.setDebug(false)
      aemail.send
      email
    }
  }

  private def createEmailer(bodyText: Option[String], bodyHtml: Option[String]): MultiPartEmail = {
    (bodyHtml, bodyText) match {
      case (None, _) =>
        val e = new MultiPartEmail()
        e.setMsg(bodyText.getOrElse(""))
        e
      case (Some(html), None) =>
        val e = new HtmlEmail()
        e.setHtmlMsg(html)
        e
      case (Some(html), Some(text)) =>
        val e = new HtmlEmail()
        e.setHtmlMsg(html)
        e.setTextMsg(text)
        e
    }
  }

  private def setAddress(emailAddress: String)(setter: (String, String) => Unit) = {
    try {
      val iAddress = new InternetAddress(emailAddress);
      val address = iAddress.getAddress()
      val name = iAddress.getPersonal()
      setter(address, name)
    } catch {
      case e: Throwable =>
        setter(emailAddress, null)
    }
  }

}

case class EmailSenderMock(log: String=>Unit) extends EmailSender {
  def sendSync(email: Email): Try[Email] = {
    Try {
      log("MOCK MAILER: send email")
      email.from.foreach(from => log(s"FROM: $from"))
      email.replyTo.foreach(replyTo => log(s"REPLYTO: $replyTo"))
      email.recipients.foreach(to => log(s"TO: $to"))
      email.ccRecipients.foreach(cc => log(s"CC: $cc"))
      email.bccRecipients.foreach(bcc => log(s"BCC: $bcc"))
      email.subject.foreach(subject => log(s"SUBJECT: $subject"))
      email.bodyText.foreach(bodyText => log(s"TEXT:  $bodyText"))
      email.bodyHtml.foreach(bodyHtml => log(s"TEXT:  $bodyHtml"))
      email
    }
  }
}
