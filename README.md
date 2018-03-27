## LDP Client

[![Build Status](https://travis-ci.org/pan-dora/ldp-client.png?branch=master)](https://travis-ci.org/pan-dora/ldp-client)

![Maven Central](https://img.shields.io/maven-central/v/cool.pandora/ldp-client.svg)


An JDK incubator HTTP client for use with the [Trellis Linked Data Platform](https://trellis-ldp.github.io/trellis/apidocs/).

## Requirements
* [JDK 10](http://jdk.java.net/10/) or higher

### Building
    $ gradle build

### Test Requirements
* Trellis Application version [0.7.0-SNAPSHOT](https://github.com/trellis-ldp/trellis/tree/jpms) published in Maven Local
* a JUnit runner must be used with the VM option `--add-modules jdk.incubator.httpclient`

## API
See [LDPClient](https://github.com/pan-dora/ldp-client/blob/master/src/main/java/cool/pandora/ldpclient/LdpClient.java)