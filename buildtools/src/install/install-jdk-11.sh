#!/bin/bash

set -e

JDK_FEATURE=11
JDK_ARCHIVE=jdk-${JDK_FEATURE}_linux-x64_bin.tar.gz

cd ~
wget --no-check-certificate -c --header "Cookie: oraclelicense=accept-securebackup-cookie" http://download.oracle.com/otn-pub/java/jdk/11+28/55eed80b163941c8885ad9298e6d786a/jdk-11_linux-x64_bin.tar.gz
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
