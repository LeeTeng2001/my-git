#!/usr/bin/env zsh
# For local testing, add -q to suppress maven INFO log
# Change -f location to local maven pom.xml location, this is specific to my machine only
# Remember to set correct permission to run this file
export JAVA_PROGRAM_ARGS=`echo "$@"`
mvn exec:java -Dexec.mainClass=Git  -Dexec.args="$JAVA_PROGRAM_ARGS" -f ~/Programming/JavaProjects/my-git/pom.xml
