sudo systemctl stop bbb-apps-akka
cd ~/dev/bigbluebutton/akka-bbb-apps
svn up ..
sbt clean
sbt run

