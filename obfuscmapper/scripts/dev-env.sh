#!/usr/bin/env bash
# Source this file to load JAVA_HOME and MAVEN on PATH (git-bash usage).
export JAVA_HOME="/c/Program Files/Microsoft/jdk-17.0.19.10-hotspot"
export MAVEN_HOME="/c/Users/rtoma/tools/apache-maven-3.9.15"
export PATH="$JAVA_HOME/bin:$MAVEN_HOME/bin:$PATH"
echo "dev-env loaded: java=$(java -version 2>&1 | head -1), mvn=$(mvn -version 2>&1 | head -1)"
