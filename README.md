## LDP Client

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/cool.pandora/ldp-client/badge.svg)](https://maven-badges.herokuapp.com/maven-central/cool.pandora/ldp-client)

An JDK incubator HTTP client for use with the [Trellis Linked Data Platform](https://trellis-ldp.github.io/trellis/apidocs/).

## Requirements
* [JDK 10](http://jdk.java.net/10/) or higher

### Building
    $ gradle processBuildTools
    $ gradle build

### Test Requirements
* Trellis Application version `0.6.0-SNAPSHOT` published in Maven Local
* a JUnit runner must be used with the VM option `--add-modules jdk.incubator.httpclient`

## API
See [LDPClient](https://github.com/pan-dora/ldp-client/blob/master/src/main/java/cool/pandora/ldpclient/LdpClient.java)