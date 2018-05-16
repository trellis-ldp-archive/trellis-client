#!/bin/bash

# Based on JDK 10 installation script from the junit5 project

set -e

JDK_FEATURE=11
JDK_ARCHIVE=openjdk-${JDK_FEATURE}-ea+13_linux-x64_bin.tar.gz

cd ~
wget https://download.java.net/java/early_access/jdk11/13/GPL/openjdk-11-ea+13_linux-x64_bin.tar.gz
tar -xzf ${JDK_ARCHIVE}
export JAVA_HOME=~/jdk-${JDK_FEATURE}
export PATH=${JAVA_HOME}/bin:$PATH
cd -
java --version

# Install Amazon's root cacerts
wget https://www.amazontrust.com/repository/AmazonRootCA1.pem
keytool -import -alias amazon1 -storepass changeit -noprompt -keystore ${JAVA_HOME}/lib/security/cacerts -file AmazonRootCA1.pem
wget https://www.amazontrust.com/repository/AmazonRootCA2.pem
keytool -import -alias amazon2 -storepass changeit -noprompt -keystore ${JAVA_HOME}/lib/security/cacerts -file AmazonRootCA2.pem
wget https://www.amazontrust.com/repository/AmazonRootCA3.pem
keytool -import -alias amazon3 -storepass changeit -noprompt -keystore ${JAVA_HOME}/lib/security/cacerts -file AmazonRootCA3.pem
wget https://www.amazontrust.com/repository/AmazonRootCA4.pem
keytool -import -alias amazon4 -storepass changeit -noprompt -keystore ${JAVA_HOME}/lib/security/cacerts -file AmazonRootCA4.pem
