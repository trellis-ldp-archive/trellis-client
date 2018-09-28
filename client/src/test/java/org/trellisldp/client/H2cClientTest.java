/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.trellisldp.client;

import static io.dropwizard.testing.ConfigOverride.config;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static javax.ws.rs.core.HttpHeaders.LINK;
import static org.apache.jena.arq.riot.WebContent.contentTypeJSONLD;
import static org.apache.jena.arq.riot.WebContent.contentTypeNTriples;
import static org.apache.jena.arq.riot.WebContent.contentTypeTextPlain;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.trellisldp.http.domain.HttpConstants.ACCEPT_PATCH;

import io.dropwizard.testing.DropwizardTestSupport;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.jena.JenaRDF;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.trellisldp.app.triplestore.AppConfiguration;
import org.trellisldp.app.triplestore.TrellisApplication;

/**
 * H2cClientTest.
 *
 * @author christopher-johnson
 */
public class H2cClientTest {
    private static final DropwizardTestSupport<AppConfiguration> APP = new DropwizardTestSupport<>(
            TrellisApplication.class, resourceFilePath("trellis-config.yml"), config("server"
            + ".applicationConnectors[2].port", "8446"), config("binaries", resourceFilePath("data")
            + "/binaries"), config("mementos", resourceFilePath("data") + "/mementos"), config("namespaces",
            resourceFilePath("data/namespaces.json")), config("server.applicationConnectors[1].keyStorePath",
            resourceFilePath("keystore/trellis.jks")));
    private static final JenaRDF rdf = new JenaRDF();
    private static String baseUrl;
    private static String pid;
    private static LdpClient h2client = null;

    @BeforeAll
    static void initAll() {
        APP.before();
        baseUrl = "http://localhost:8446/";
        h2client = new LdpClientImpl();
    }

    @AfterAll
    static void tearDownAll() {
        APP.after();
    }

    @BeforeEach
    void init() {
        pid = "ldp-test-" + UUID.randomUUID().toString();
    }

    @AfterEach
    void tearDown() {
    }

    private static Path getTestJsonResource() {
        return Paths.get(H2cClientTest.class.getResource("/webanno.complete-embedded.json").getPath());
    }

    private static InputStream getTestN3Resource() {
        return H2cClientTest.class.getResourceAsStream("/webanno.complete.nt");
    }

    private static InputStream getTestBinary() {
        return H2cClientTest.class.getResourceAsStream("/00000001.tif");
    }

    @Test
    void testHead() throws LdpClientException {
        try {
            final IRI identifier = rdf.createIRI(Objects.requireNonNull(baseUrl));
            final Map<String, List<String>> res = h2client.head(identifier);
            assertTrue(res.containsKey(ACCEPT_PATCH));
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @RepeatedTest(10)
    void testRepeatedPutH2N3Resource() throws Exception {
        try {
            final IRI base = rdf.createIRI(baseUrl);
            h2client.initUpgrade(base);
            final IRI identifier = rdf.createIRI(baseUrl + pid);
            h2client.put(identifier, getTestN3Resource(), contentTypeNTriples);
            final Map<String, List<String>> headers = h2client.head(identifier);
            assertTrue(headers.containsKey(LINK));
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @RepeatedTest(10)
    void testRepeatedPutH2JsonResource() throws Exception {
        try {
            final IRI base = rdf.createIRI(baseUrl);
            h2client.initUpgrade(base);
            final IRI identifier = rdf.createIRI(baseUrl + pid);
            h2client.putSupplier(identifier, fileInputStreamSupplier(getTestJsonResource()), contentTypeJSONLD);
            final Map<String, List<String>> headers = h2client.head(identifier);
            assertTrue(headers.containsKey(LINK));
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Test
    void testJoiningCompletableFuturePut() throws Exception {
        try {
            final Map<URI, InputStream> map = new HashMap<>();
            final int LOOPS = 400;
            for (int i = 0; i < LOOPS; i++) {
                pid = "ldp-test-" + UUID.randomUUID().toString();
                final IRI identifier = rdf.createIRI(baseUrl + pid);
                final URI uri = new URI(identifier.getIRIString());
                final InputStream is = getTestN3Resource();
                map.put(uri, is);
            }
            final IRI base = rdf.createIRI(baseUrl);
            h2client.initUpgrade(base);
            h2client.joiningCompletableFuturePut(map, contentTypeNTriples);
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Test
    void testJoiningCompletableFuturePutBinary() throws Exception {
        try {
            final Map<URI, InputStream> map = new HashMap<>();
            final int LOOPS = 20;
            for (int i = 0; i < LOOPS; i++) {
                pid = "ldp-test-" + UUID.randomUUID().toString();
                final IRI identifier = rdf.createIRI(baseUrl + pid + ".tif");
                final URI uri = new URI(identifier.getIRIString());
                final InputStream is = getTestBinary();
                map.put(uri, is);
            }
            final IRI base = rdf.createIRI(baseUrl);
            h2client.initUpgrade(base);
            h2client.joiningCompletableFuturePut(map, "image/tiff");
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @DisplayName("Delete")
    @Test
    void testDelete() throws LdpClientException {
        try {
            final IRI identifier = rdf.createIRI(baseUrl + pid);
            final IRI base = rdf.createIRI(baseUrl);
            h2client.initUpgrade(base);
            assertTrue(h2client.putWithResponse(identifier, getTestBinary(), contentTypeTextPlain));
            h2client.delete(identifier);
            HttpResponse res = h2client.getResponse(identifier);
            assertEquals(410, res.statusCode());
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    static Supplier<FileInputStream> fileInputStreamSupplier(Path f) {
        return new Supplier<>() {
            Path file = f;
            @Override
            public FileInputStream get() {
                try {
                    PrivilegedExceptionAction<FileInputStream> pa =
                            () -> new FileInputStream(file.toFile());
                    return AccessController.doPrivileged(pa);
                } catch (PrivilegedActionException x) {
                    throw new UncheckedIOException((IOException)x.getCause());
                }
            }
        };
    }
}
