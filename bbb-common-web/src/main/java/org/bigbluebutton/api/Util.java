package org.bigbluebutton.api;

import java.io.File;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.text.Normalizer;

public final class Util {

    private static Logger log = LoggerFactory.getLogger(Util.class);

    //DuongTC: Change BBB slide folder
    public static String flattenToAscii(String string) {
        char[] out = new char[string.length()];
        string = Normalizer.normalize(string, Normalizer.Form.NFD);
        int j = 0;
        for (int i = 0, n = string.length(); i < n; ++i) {
            char c = string.charAt(i);
            if (c <= '\u007F') {
                out[j++] = c;
            }
        }
        String o = new String(out);
        return o.replaceAll("[^a-zA-Z0-9]", "-");
    }
    //DuongTC: END Change BBB slide folder

    public static String generatePresentationId(String name) {

        if (name.equals("default.pdf"))
        {
            return "default-slide";
        }

        //DuongTC: Change BBB slide folder
        long timestamp = System.currentTimeMillis();
        //return DigestUtils.shaHex(name) + "-" + timestamp;
        return flattenToAscii(name) + "-" + DigestUtils.shaHex(name) + "-" + timestamp;
        //DuongTC: END Change BBB slide folder
    }

    public static String createNewFilename(String presId, String fileExt) {
        return presId + "." + fileExt;
    }

    public static File createPresentationDirectory(String meetingId, String presentationDir, String presentationId) {
        //log.debug("createPresentationDirectory staring\nmeetingId:" + meetingId + "\npresentationDir:" + presentationDir + "\npresentationId:" + presentationId);
        //println("createPresentationDirectory staring\nmeetingId:" + meetingId + "\npresentationDir:" + presentationDir + "\npresentationId:" + presentationId);
        //String meetingPath = presentationDir + File.separatorChar + meetingId + File.separatorChar + meetingId;

        //DuongTC: Change BBB slide folder
        /*
        Meeting meeting = meetingService.getMeeting(meetingId);
        String externalMeetingId = meeting.getExternalId();
         */
        String[] parts = meetingId.split("-");
        String externalMeetingId = parts[parts.length - 1];
        String meetingPath = presentationDir + File.separatorChar + externalMeetingId + File.separatorChar + "Preloaded";

        log.debug("createPresentationDirectory for presentationDir: " + presentationDir + " meetingId: " + meetingId + " presentationId: " + presentationId + " meetingPath: " + meetingPath);
        //DuongTC: END Change BBB slide folder

		String presPath = meetingPath + File.separatorChar + presentationId;
		File dir = new File(presPath);
		if (dir.mkdirs()) {
			return dir;
		}
		return null;
	}

	public static File downloadPresentationDirectory(String uploadDirectory) {
		File dir = new File(uploadDirectory + File.separatorChar + "download");
		if (dir.mkdirs()) {
			return dir;
		}
		return null;
	}
}
