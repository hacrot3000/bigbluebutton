cd ~/dev/bigbluebutton/bbb-common-web && \
svn up .. && \
./deploy.sh && \
cd ~/dev/bigbluebutton/bigbluebutton-web && \
svn up .. && \
./build.sh && \
grails -Dserver.port=8989 run-war

