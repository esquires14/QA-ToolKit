#!/bin/sh
# ----------------------------------------------------------------------------
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
# ----------------------------------------------------------------------------

# ----------------------------------------------------------------------------
# Apache Maven Wrapper startup batch script, version 3.2.0
# ----------------------------------------------------------------------------

set -e

# Find the project base dir, i.e. the closest directory that contains a ".mvn" directory.
find_maven_basedir() {
    if [ -z "$1" ]; then
        echo "Path not specified to find_maven_basedir"
        return 1
    fi

    basedir="$1"
    wdir="$1"
    while [ "$wdir" != '/' ] ; do
        if [ -d "$wdir"/.mvn ] ; then
            basedir=$wdir
            break
        fi
        # workaround for JBEAP-8937 (on Solaris 10/Sparc)
        if [ -d "${wdir}" ]; then
            wdir=$(cd "$wdir/.."; pwd)
        fi
        # end of workaround
    done
    printf '%s' "$(cd "$basedir"; pwd)"
}

# concatenates all lines of a file
concat_lines() {
    if [ -f "$1" ]; then
        # Remove \r in case we run on Windows within Git Bash
        # 1. Remove any trailing \r character.
        # 2. Avoid NUL bytes: https://unix.stackexchange.com/a/648505/36211
        tr -s '\r\n' '\n' < "$1"
    fi
}

log() {
    if [ "$MVNW_VERBOSE" = true ]; then
        printf '%s\n' "$1"
    fi
}

BASE_DIR=$(find_maven_basedir "$(dirname "$0")")

##########################################################################################
# Extension to allow automatically downloading the maven-wrapper.jar from Maven-central
# This allows using the maven wrapper in projects that prohibit checking in binary data.
##########################################################################################
wrapperJarPath="$BASE_DIR/.mvn/wrapper/maven-wrapper.jar"
if [ -r "$wrapperJarPath" ]; then
    log "Found $wrapperJarPath"
else
    log "Couldn't find $wrapperJarPath, downloading it ..."

    if [ -n "$MVNW_REPOURL" ]; then
        wrapperUrl="$MVNW_REPOURL/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"
    else
        wrapperUrl="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"
    fi
    while IFS="=" read -r key value; do
        # Remove whitespace
        key=$(echo "$key" | tr -d ' ')
        value=$(echo "$value" | tr -d ' ')
        if [ "$key" = "wrapperUrl" ]; then
            wrapperUrl="$value"
            break
        fi
    done < "$BASE_DIR/.mvn/wrapper/maven-wrapper.properties"
    log "Downloading from: $wrapperUrl"

    if $javaCmd -version > /dev/null 2>&1; then
        javaSource="$BASE_DIR/.mvn/wrapper/MavenWrapperDownloader.java"
        javaClass="$BASE_DIR/.mvn/wrapper/MavenWrapperDownloader"
        # For Cygwin, switch paths to Windows format before running javac and java
        if $cygwin; then
            javaSource=$(cygpath --path --windows "$javaSource")
            javaClass=$(cygpath --path --windows "$javaClass")
        fi
        if [ -e "$javaSource" ]; then
            if [ ! -e "$javaClass.class" ]; then
                log " - Compiling MavenWrapperDownloader.java ..."
                ("$JAVA_HOME/bin/javac" "$javaSource")
            fi
            if [ -e "$javaClass.class" ]; then
                log " - Running MavenWrapperDownloader.java ..."
                ("$JAVA_HOME/bin/java" -cp .mvn/wrapper MavenWrapperDownloader "$wrapperUrl" "$wrapperJarPath") || rm -f "$wrapperJarPath"
            fi
        fi
    fi

    if [ ! -r "$wrapperJarPath" ]; then
        if command -v wget > /dev/null; then
            log " - Downloading with wget ..."
            wget -q -O "$wrapperJarPath" "$wrapperUrl" || rm -f "$wrapperJarPath"
        elif command -v curl > /dev/null; then
            log " - Downloading with curl ..."
            curl -o "$wrapperJarPath" "$wrapperUrl" -s || rm -f "$wrapperJarPath"
        else
            log " - Couldn't download maven wrapper jar: wget or curl is required"
        fi
    fi
fi

##########################################################################################
# End of extension
##########################################################################################

# If specified, validate the SHA-256 sum of the Maven wrapper jar file
MVNW_SHA256_SUM=${MVNW_SHA256_SUM:-}
wrapperSha256Sum=""
while IFS="=" read -r key value; do
    key=$(echo "$key" | tr -d ' ')
    value=$(echo "$value" | tr -d ' ')
    if [ "$key" = "wrapperSha256Sum" ]; then
        wrapperSha256Sum=$value
        break
    fi
done < "$BASE_DIR/.mvn/wrapper/maven-wrapper.properties"

if [ -n "$wrapperSha256Sum" ]; then
    wrapperSha256Result=""
    if command -v sha256sum > /dev/null; then
        wrapperSha256Result=$(sha256sum "$wrapperJarPath" | awk '{print $1}')
    elif command -v shasum > /dev/null; then
        wrapperSha256Result=$(shasum -a 256 "$wrapperJarPath" | awk '{print $1}')
    else
        echo "Checksum validation was requested but neither 'sha256sum' nor 'shasum' are available."
        echo "Please install one or the other, or disable validation by removing 'wrapperSha256Sum' from your maven-wrapper.properties."
        exit 1
    fi
    if [ "$wrapperSha256Sum" != "$wrapperSha256Result" ]; then
        echo "Error: Failed to validate Maven wrapper SHA-256, your Maven wrapper distribution might be compromised."
        echo "Investigate or delete $wrapperJarPath to attempt a clean download."
        echo "If you updated your Maven version, you need to update the specified wrapperSha256Sum property."
        exit 1
    fi
fi

MAVEN_OPTS="$(concat_lines "$BASE_DIR/.mvn/jvm.config") $MAVEN_OPTS"

# For Cygwin, switch paths to Windows format before running java
if $cygwin; then
    [ -n "$JAVA_HOME" ] &&
        JAVA_HOME=$(cygpath --unix "$JAVA_HOME")
    [ -n "$CLASSPATH" ] &&
        CLASSPATH=$(cygpath --path --unix "$CLASSPATH")
fi

# Provide a "standardized" way to retrieve the CLI args that will
# be passed to the JVM. This is a "de-facto" standard practice in the
# Maven/Jenkins/MyBatis and another 3rd party projects.
MAVEN_PROJECTBASEDIR=${MAVEN_BASEDIR:-"$BASE_DIR"}
export MAVEN_PROJECTBASEDIR
log "$MVNW_EXE_DIR : $MAVEN_PROJECTBASEDIR"

JAVAEXECUTABLE="${JAVA_HOME}/bin/java"

if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
    javaExecutable="$JAVA_HOME/bin/java"
    log "Found java executable in JAVA_HOME"
elif type -p java > /dev/null; then
    log "Found java executable in PATH"
    javaExecutable="java"
else
    echo "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH."
    echo "Please set the JAVA_HOME variable in your environment to match the location of your Java installation."
    exit 1
fi

javaVersion=$("$javaExecutable" -version 2>&1 | awk -F '"' '/version/ {print $2}')

# Provide a "standardized" way to retrieve the CLI args that will
# be passed to the JVM. This is a "de-facto" standard practice in the
# Maven/Jenkins/MyBatis and another 3rd party projects.
MAVEN_OPTS="$MAVEN_OPTS $MAVEN_DEBUG_OPTS"

exec "$javaExecutable" \
    $MAVEN_OPTS \
    $MAVEN_DEBUG_OPTS \
    -classpath "$wrapperJarPath" \
    "-Dmaven.multiModuleProjectDirectory=${MAVEN_PROJECTBASEDIR}" \
    org.apache.maven.wrapper.MavenWrapperMain "$@"
