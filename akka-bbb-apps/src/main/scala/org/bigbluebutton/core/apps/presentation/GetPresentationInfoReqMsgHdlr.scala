package org.bigbluebutton.core.apps.presentation

import org.bigbluebutton.common2.msgs._
import org.bigbluebutton.common2.domain.PresentationVO
import org.bigbluebutton.core.apps.Presentation
import org.bigbluebutton.core.running.OutMsgRouter

//DuongTC: Added load existing presentations on hard disk
import org.bigbluebutton.core.running.MeetingActor
import java.io.File
import org.bigbluebutton.common2.domain.PageVO
import javax.imageio.ImageIO
import org.bigbluebutton.common2.util.JsonUtil
import scala.io.Source
//DuongTC: END Added load existing presentations on hard disk

trait GetPresentationInfoReqMsgHdlr {
  this: PresentationApp2x =>

  val outGW: OutMsgRouter

  def handleGetPresentationInfoReqMsg(msg: GetPresentationInfoReqMsg): Unit = {
    log.debug("Received GetPresentationInfoReqMsg")

    //DuongTC: Added load existing presentations on hard disk
    if (liveMeeting.presModel.getNumberOfPresentations() <= 1) {
      val meetingIds = liveMeeting.props.meetingProp.intId.split("-")
      MeetingActor.mettingId = liveMeeting.props.meetingProp.intId
      log.warning("=================================================================================\n  meetingId=" + liveMeeting.props.meetingProp.intId + "\n=============================================================================")

      var presentationData = new scala.collection.immutable.HashMap[String, Presentation]

      //Read bigbluebutton-web configuration file
      val propertiesFile = "/var/lib/tomcat7/webapps/bigbluebutton/WEB-INF/classes/bigbluebutton.properties"
      var presentationDir = ""
      var rootUri = ""
      var presentationBaseURL = ""
      var serverURL = ""

      val bufferedSource = Source.fromFile(propertiesFile)
      for (line <- bufferedSource.getLines) {
        if (line.startsWith("presentationDir=")) {
          presentationDir = line.substring(("presentationDir=").length()).trim()
        }
        if (line.startsWith("presentationBaseURL=")) {
          presentationBaseURL = line.substring(("presentationBaseURL=").length()).trim()
        }
        if (line.startsWith("bigbluebutton.web.serverURL=")) {
          serverURL = line.substring(("bigbluebutton.web.serverURL=").length()).trim()
        }
      }
      bufferedSource.close

      rootUri = presentationBaseURL.replace("${bigbluebutton.web.serverURL}", serverURL)

      if (!rootUri.endsWith("/")) {
        rootUri = rootUri + "/"
      }

      val path = /*"/usr/local/dimdim/Mediaserver/www/archive/slidedeck/"*/ presentationDir + meetingIds.last + "/Preloaded/"
      //val rootUri = "http://172.104.233.62/bigbluebutton/presentation/"

      log.warning("Base presentation URI " + rootUri + "\n")

      //Scan all slide in the presentations folder
      val d = new File(path)
      val presentationsFiles = d.listFiles.filter(_.isDirectory).toList

      log.warning("Openning folder " + path + "\n")

      var notFoundCurrent: Boolean = true

      for (f <- presentationsFiles) {
        log.warning("> Got file slides folder: " + f.getAbsolutePath() + "\n")

        //Check and ignore if found multi default slide
        if (notFoundCurrent || f.getName().indexOf("-d2d9a672040fbde2a47a10bf6c37b6a4b5ae187f-") < 0) {
          val d = new File(f.getAbsolutePath())
          val pageFiles = d.listFiles.filter(_.isFile).toList

          //Build a slide infomation
          val pages = new collection.mutable.HashMap[String, PageVO]
          var num = 1;
          for (p <- pageFiles) {
            if (p.getName().endsWith(".swf")) {
              val thumbUri = rootUri + liveMeeting.props.meetingProp.intId + "/" + liveMeeting.props.meetingProp.intId + "/" + f.getName() + "/thumbnail/" + num
              val swfUri = rootUri + liveMeeting.props.meetingProp.intId + "/" + liveMeeting.props.meetingProp.intId + "/" + f.getName() + "/slide/" + num
              val txtUri = rootUri + liveMeeting.props.meetingProp.intId + "/" + liveMeeting.props.meetingProp.intId + "/" + f.getName() + "/textfiles/" + num
              val svgUri = rootUri + liveMeeting.props.meetingProp.intId + "/" + liveMeeting.props.meetingProp.intId + "/" + f.getName() + "/svg/" + num

              val page = PageVO(f.getName() + "/" + num, num, thumbUri, swfUri, txtUri, svgUri, num == 1, 0, 0, 100, 100)
              pages += page.id -> page
              num = num + 1
            }
          }

          var isCurrent: Boolean = false

          //Check and ignore if found multi default slide
          if (notFoundCurrent &&
            (
              f.getName().indexOf("-d2d9a672040fbde2a47a10bf6c37b6a4b5ae187f-") >= 0 || f.getName().startsWith("default-pdf") || f.getName().startsWith("default-slide")
            )) {
            isCurrent = true
            notFoundCurrent = false
          }

          //Create new presentation and add to the meeting
          val pr = new Presentation(f.getName(), f.getName(), isCurrent, pages.toMap, false)
          presentationData += pr.id -> pr
          liveMeeting.presModel.addPresentation(pr)
        }
      }

      log.warning("Got presentation info: \n" + JsonUtil.toJson(presentationData.toMap))
    }
    //DuongTC: END Added load existing presentations on hard disk

    def broadcastEvent(msg: GetPresentationInfoReqMsg, presentations: Vector[Presentation]): Unit = {
      val routing = Routing.addMsgToClientRouting(MessageTypes.DIRECT, liveMeeting.props.meetingProp.intId, msg.header.userId)
      val envelope = BbbCoreEnvelope(GetPresentationInfoRespMsg.NAME, routing)
      val header = BbbClientMsgHeader(GetPresentationInfoRespMsg.NAME, liveMeeting.props.meetingProp.intId, msg.header.userId)

      val presVOs = presentations.map { p =>
        PresentationVO(p.id, p.name, p.current, p.pages.values.toVector, p.downloadable)
      }

      val body = GetPresentationInfoRespMsgBody(presVOs)
      val event = GetPresentationInfoRespMsg(header, body)
      val msgEvent = BbbCommonEnvCoreMsg(envelope, event)
      outGW.send(msgEvent)
    }

    broadcastEvent(msg, getPresentationInfo())
  }
}
