Scops:
 - We are a online languages school company (http://myngle.com) so we need BigBlueButton for our virtual class room
 - Each teacher have their own courses, each course has some presentation files (we called it is slides).
 - Every time teacher open BBB for a class, our system already know which course should be used and which slides should be pre-loaded.
 - So we have a folder to stored all slides for all teacher and their lessons, when teacher open BigBlueButton, it will load all slides for that lesson without ask teacher to re-upload them. Or system also don't have to reupload them by use pre-uploaded presentation.
 - The folder structure is: /var/bigbluebutton/<b>&lt;lessonsId&gt;</b>/Preloaded/&lt;presentation id&gt;/
 - They 99% same with the original BBB folder structure except the <b>&lt;lessonsId&gt;</b> is now ExternalID - the number we sent when create the room via API. Original BBB use internal room id for that, it is a unique hash string for each class room.
 
 So we did new custom features:

- Add the external room ID at the end of internal room id, it is used to help other module can know which is external room id and take the correct slide for that room. The room ID is now &lt;HASH&gt;-&lt;lessonsId&gt;, such as: 86f34d9799f9e6741b50dac37a9093357d772032-6924695.
- Change upload slide code to keep the original uploaded file name at begining of the file hash, so we can more easy to check it on hard disk.
- Remove duplicate default slides: By default, BBB copy the default.pdf every time we create new room via API, so our slides folder "/var/bigbluebutton/&lt;lessonsId&gt;/Preloaded" will have lot of duplicate of default.pdf. We corrected that issue so BBB is now only create default-slide for new teacher/course.
- Remove the "delete presentation" button on the upload list to avoid teacher delete their course's resource.
- Add new WebrtcModule on BBB-client to allow us to intergrate external Video/Audio into BBB. It will be intergrated inside an iframe.
 
 We also did:
- Add new rebuild script to build and deploy BBB's custom code. It will build and deploy our custom code and overwrite the ogirinal BBB' package installation.

Contact me at truong@chuongduong.net if you need more information.

BigBlueButton
=============
BigBlueButton is an open source web conferencing system.  

BigBlueButton supports real-time sharing of audio, video, slides (with whiteboard controls), chat, and the screen.  Instructors can engage remote students with polling, emojis, and breakout rooms.  BigBlueButton can record and playback all content shared in a session.

We designed BigBlueButton for online learning (though it can be used for many [other applications](http://www.c4isrnet.com/story/military-tech/disa/2015/02/11/disa-to-save-12m-defense-collaboration-services/23238997/)).  The educational use cases for BigBlueButton are

  * One-to-one on-line tutoring
  * Small group collaboration 
  * On-line classes

BigBlueButton runs on a Ubuntu 16.04 64-bit server.  If you follow the [installation instructions](http://docs.bigbluebutton.org/install/install.html), we guarantee you will have BigBlueButton installed and running within 30 minutes (or your money back :-).

For full technical documentation BigBlueButton -- including architecture, features, API, and GreenLight (the default front-end) -- see [http://docs.bigbluebutton.org/](http://docs.bigbluebutton.org/).

BigBlueButton and the BigBlueButton Logo are trademarks of [BigBlueButton Inc](http://bigbluebutton.org) .
