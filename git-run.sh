#!/usr/bin/env zsh
export JAVA_PROGRAM_ARGS=`echo "$@"`
mvn exec:java -Dexec.mainClass=Git  -Dexec.args="$JAVA_PROGRAM_ARGS" -f /Users/lunafreya/Programming/JavaProjects/my-git/pom.xml
# Change -f location to local maven pom.xml location, this is specific to my machine only
# Remember to set correct permission to run this file