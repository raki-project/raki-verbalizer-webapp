#!/bin/sh

export MAVEN_OPTS="-Xmx4G"
# export MAVEN_OPTS="-Xmx16G -Dlog4j.configuration=file:data/fox/log4j.properties"

echo "Check the $0.log file."

nohup mvn exec:java  -Dexec.mainClass="org.dice_research.raki.verbalizer.webapp.ServiceApp" > $0.log 2>&1 </dev/null &
