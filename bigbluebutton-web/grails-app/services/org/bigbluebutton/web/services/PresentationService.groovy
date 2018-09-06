/**
* BigBlueButton open source conferencing system - http://www.bigbluebutton.org/
*
* Copyright (c) 2014 BigBlueButton Inc. and by respective authors (see below).
*
* This program is free software; you can redistribute it and/or modify it under the
* terms of the GNU Lesser General Public License as published by the Free Software
* Foundation; either version 3.0 of the License, or (at your option) any later
* version.
*
* BigBlueButton is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
* PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License along
* with BigBlueButton; if not, see <http://www.gnu.org/licenses/>.
*
*/
package org.bigbluebutton.web.services

import java.util.concurrent.*;
import java.lang.InterruptedException
import org.bigbluebutton.presentation.DocumentConversionService
import org.bigbluebutton.presentation.UploadedPresentation
//DuongTC: Change BBB slide folder
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.bigbluebutton.api.MeetingService;
import org.bigbluebutton.api.domain.*;
//DuongTC: End Change BBB slide folder

class PresentationService {
//DuongTC: Change BBB slide folder
    private static Logger log = LoggerFactory.getLogger(PresentationService.class);
    MeetingService meetingService;
//DuongTC: END Change BBB slide folder

    static transactional = false
	DocumentConversionService documentConversionService
	def presentationDir
	def testConferenceMock
	def testRoomMock
	def testPresentationName
	def testUploadedPresentation
	def defaultUploadedPresentation
	def presentationBaseUrl

  def deletePresentation = {conf, room, filename ->
    		def directory = new File(roomDirectory(conf, room).absolutePath + File.separatorChar + filename)
    		deleteDirectory(directory)
	}

	def deleteDirectory = {directory ->
		log.debug "delete = ${directory}"
		/**
		 * Go through each directory and check if it's not empty.
		 * We need to delete files inside a directory before a
		 * directory can be deleted.
		**/
		File[] files = directory.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				deleteDirectory(files[i])
			} else {
				files[i].delete()
			}
		}
		// Now that the directory is empty. Delete it.
		directory.delete()
	}

    /*
     * DuongTC: This function is not being used anymore
	def listPresentations = {conf, room ->
		def presentationsList = []
		def directory = roomDirectory(conf, room)
		log.info "listPresentations directory ${directory.absolutePath}"
		if( directory.exists() ){
			directory.eachFile(){ file->
				if( file.isDirectory() )
                {
					presentationsList.add( file.name )
                    log.info "found ${file.name}"
                }
			}
		}
		return presentationsList
	}
    /**/

  def getPresentationDir = {
    return presentationDir
  }

    def processUploadedPresentation = {uploadedPres ->
        // Run conversion on another thread.
        Timer t = new Timer(uploadedPres.getName(), false)

        t.runAfter(5000) {
            try {
                documentConversionService.processDocument(uploadedPres)
            } finally {
            t.cancel()
            }
        }
    }

//DuongTC: Change BBB slide folder
	def showSlide(String conf, String room, String presentationName, String id) {
        def filePath = roomDirectory(conf, room).absolutePath + File.separatorChar + presentationName + File.separatorChar
        def file = new File(filePath + "slide-${id}.swf")
        if (file.exists())
        {
            log.debug "showSlide " + filePath + "slide-${id}.swf"
            return file
        }
        else
        {
            int idn = id.toInteger() - 1
            log.debug "showSlide " + filePath + "${idn}.swf"
            return new File(filePath + "${idn}.swf")
        }
	}

	def showSvgImage(String conf, String room, String presentationName, String id) {
		def f = new File(roomDirectory(conf, room).absolutePath + File.separatorChar + presentationName + File.separatorChar + "svgs" + File.separatorChar + "slide${id}.svg")
        if (f.exists())
        {
            return f
        }
        else
        {
            return new File(getPresentationDir() + "globalblank/slide.svg")
        }
	}

	def showPresentation = {conf, room, filename ->
		//new File(roomDirectory(conf, room).absolutePath + File.separatorChar + filename + File.separatorChar + "slides.swf")
        def filePath = roomDirectory(conf, room).absolutePath + File.separatorChar + filename + File.separatorChar
        def file = new File(filePath + "slide.swf")
        if (file.exists())
        {
            log.debug "showSlide " + filePath + "slide.swf"
            return file
        }
        else
        {
            return new File(getPresentationDir() + "globalblank/slide.swf")
        }

	}

	def showThumbnail = {conf, room, presentationName, thumb ->
        /*
		def thumbFile = roomDirectory(conf, room).absolutePath + File.separatorChar + presentationName + File.separatorChar +
					"thumbnails" + File.separatorChar + "thumb-${thumb}.png"
        /**/
        def filePath = roomDirectory(conf, room).absolutePath + File.separatorChar + presentationName + File.separatorChar

		def thumbFile = filePath + "thumbnails" + File.separatorChar + "thumb-${thumb}.png"

		def f = new File(thumbFile)

        if (f.exists())
        {
            log.debug "showing $thumbFile"
            return f
        }
        else
        {
            int idn = thumb.toInteger() - 1
            log.debug "showSlide " + filePath + "${idn}.png"
            f = new File(filePath + "${idn}.png")
            if (f.exists())
            {
                return f
            }
            else
            {

                return new File(getPresentationDir() + "globalblank/thumb.png")
            }
        }
	}

	def showTextfile = {conf, room, presentationName, textfile ->
		def txt = roomDirectory(conf, room).absolutePath + File.separatorChar + presentationName + File.separatorChar +
					"textfiles" + File.separatorChar + "slide-${textfile}.txt"
		log.debug "showing $txt"

		def f = new File(txt)
        if (f.exists())
        {
            return f
        }
        else
        {
            return new File(getPresentationDir() + "globalblank/slide.txt")
        }
	}
//DuongTC: END Change BBB slide folder

	def numberOfThumbnails = {conf, room, name ->
		def thumbDir = new File(roomDirectory(conf, room).absolutePath + File.separatorChar + name + File.separatorChar + "thumbnails")
		thumbDir.listFiles().length
	}

	def numberOfSvgs = {conf, room, name ->
		def SvgsDir = new File(roomDirectory(conf, room).absolutePath + File.separatorChar + name + File.separatorChar + "svgs")
		SvgsDir.listFiles().length
	}

	def numberOfTextfiles = {conf, room, name ->
		log.debug roomDirectory(conf, room).absolutePath + File.separatorChar + name + File.separatorChar + "textfiles"
		def textfilesDir = new File(roomDirectory(conf, room).absolutePath + File.separatorChar + name + File.separatorChar + "textfiles")
		textfilesDir.listFiles().length
	}

  def roomDirectory = {conf, room ->
      //DuongTC: Change BBB slide folder
      Meeting meeting = meetingService.getMeeting(room);
      String externalMeetingId = meeting.getExternalId();
      String finalExternalMeetingId = externalMeetingId.replaceAll("[^0-9]", "");
      if (finalExternalMeetingId.length() == 0)
      {
          finalExternalMeetingId = "21291";
      }

      log.debug "roomDirectory for presentationDir: " + presentationDir + " conf: " + conf + " room: " + room + " externalMeetingId: " + externalMeetingId + " with number finalExternalMeetingId:" + finalExternalMeetingId;
      return new File(presentationDir + File.separatorChar + finalExternalMeetingId + File.separatorChar + "Preloaded")
      //DuongTC: END Change BBB slide folder
  }

	def testConversionProcess() {
		File presDir = new File(roomDirectory(testConferenceMock, testRoomMock).absolutePath + File.separatorChar + testPresentationName)

		if (presDir.exists()) {
			File pres = new File(presDir.getAbsolutePath() + File.separatorChar + testUploadedPresentation)
			if (pres.exists()) {
				UploadedPresentation uploadedPres = new UploadedPresentation(testConferenceMock, testRoomMock, testPresentationName);
				uploadedPres.setUploadedFile(pres);
				// Run conversion on another thread.
				new Timer().runAfter(1000)
				{
					documentConversionService.processDocument(uploadedPres)
				}
			} else {
				log.error "${pres.absolutePath} does NOT exist"
			}
		} else {
			log.error "${presDir.absolutePath} does NOT exist."
		}

	}

	def getFile = {conf, room, presentationName ->
		println "download request for $presentationName"
		def fileDirectory = new File(roomDirectory(conf, room).absolutePath + File.separatorChar + presentationName + File.separatorChar +
"download")
		//list the files of the download directory ; it must have only 1 file to download
		def list = fileDirectory.listFiles()
		//new File(pdfFile)
		list[0]
	}
}

/*** Helper classes **/
import java.io.FilenameFilter;
import java.io.File;
class SvgFilter implements FilenameFilter {
    public boolean accept(File dir, String name) {
        return (name.endsWith(".svg"));
    }
}
