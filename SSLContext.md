## A Brief Guide to SSLContext

### SSLContext
SSL Context is needed by the jdk.incubator.httpclient to connect to TLS.  An SSL Context requires a JKS keystore that contains the credentials of the server.
See SimpleSSLContext for an example.

If your server does not support JKS, you may have a private key and a certificate.  These must be converted into a JKS.

This command converts a crt and private key to the p12 source format needed by a JKS

```bash
* openssl pkcs12 -export -in ca.crt -inkey ca.key -out server.p12 -name proxy -CAfile ca.crt -caname root
```

This command creates a new keystore from the p12 formatted source
```bash
keytool -importkeystore -deststorepass changeme -destkeypass changeme -destkeystore proxy.jks -srckeystore server.p12 -srcstoretype PKCS12 -srcstorepass changeme -alias proxy
```


