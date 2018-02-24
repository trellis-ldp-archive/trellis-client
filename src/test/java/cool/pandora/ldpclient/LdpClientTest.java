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

package cool.pandora.ldpclient;

import static io.dropwizard.testing.ConfigOverride.config;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static java.time.Instant.now;
import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;
import static java.util.Arrays.stream;
import static java.util.Base64.getEncoder;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.HttpHeaders.ACCEPT;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.HttpHeaders.LINK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.trellisldp.http.domain.HttpConstants.ACCEPT_PATCH;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.testing.DropwizardTestSupport;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.ws.rs.core.Link;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.jena.JenaRDF;
import org.apache.jena.riot.WebContent;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.trellisldp.app.TrellisApplication;
import org.trellisldp.app.config.TrellisConfiguration;
import org.trellisldp.vocabulary.DC;
import org.trellisldp.vocabulary.JSONLD;
import org.trellisldp.vocabulary.LDP;
import org.trellisldp.vocabulary.Trellis;

/**
 * LdpClientTest.
 *
 * @author christopher-johnson
 */
class LdpClientTest {

    private static final DropwizardTestSupport<TrellisConfiguration> APP
            = new DropwizardTestSupport<>(
            TrellisApplication.class,
            resourceFilePath("trellis-config.yml"),
            config("server.applicationConnectors[0].port", "0"),
            config("binaries", resourceFilePath("data") + "/binaries"),
            config("mementos", resourceFilePath("data") + "/mementos"),
            config("namespaces", resourceFilePath("data/namespaces.json")));
    private static final JenaRDF rdf = new JenaRDF();
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static String baseUrl;
    private static String pid;
    private final LdpClientImpl client = new LdpClientImpl();

    @BeforeAll
    static void initAll() {
        APP.before();
        baseUrl = "http://localhost:" + APP.getLocalPort() + "/";
    }

    @BeforeEach
    void init() {
        pid = "ldp-test-" + UUID.randomUUID().toString();
    }

    @AfterEach
    void tearDown() {
    }

    @AfterAll
    static void tearDownAll() {
        APP.after();
    }

    private static InputStream getTestBinary() {
        return LdpClientTest.class.getResourceAsStream("/simpleData.txt");
    }

    private static InputStream getRevisedTestBinary() {
        return LdpClientTest.class.getResourceAsStream("/simpleDataRev.txt");
    }

    private static InputStream getTestResource() {
        return LdpClientTest.class.getResourceAsStream("/simpleTriple.ttl");
    }

    private static InputStream getRevisedTestResource() {
        return LdpClientTest.class.getResourceAsStream("/simpleTripleRev.ttl");
    }

    private static InputStream getTestGraph() {
        return LdpClientTest.class.getResourceAsStream("/graph.ttl");
    }

    private static InputStream getTestAcl() {
        return LdpClientTest.class.getResourceAsStream("/acl.ttl");
    }

    private static InputStream getSparqlUpdate() {
        return LdpClientTest.class.getResourceAsStream("/sparql-update.txt");
    }

    @Test
    void testHead() throws InterruptedException, IOException, URISyntaxException {
        final IRI identifier = rdf.createIRI(Objects.requireNonNull(baseUrl));
        final Map<String, List<String>> res = client.head(identifier);
        assertTrue(res.containsKey(ACCEPT_PATCH));
    }

    @Test
    void testGetJsonLd() throws InterruptedException, IOException, URISyntaxException {
        final IRI identifier = rdf.createIRI(baseUrl + pid);
        final Map<String, String> metadata = singletonMap(
                CONTENT_TYPE, WebContent.contentTypeTurtle);
        client.put(identifier, getTestResource(), metadata);
        final String res = client.getJson(identifier);
        final List<Map<String, Object>> obj = MAPPER.readValue(
                res,
                new TypeReference<List<Map<String, Object>>>() {
                });
        assertEquals(1L, obj.size());

        @SuppressWarnings("unchecked") final List<Map<String, String>> titles = (List<Map<String,
                String>>) obj.get(
                0)
                .get(DC.title.getIRIString());

        final List<String> titleVals = titles.stream().map(x -> x.get("@value")).collect(toList());

        assertEquals(1L, titleVals.size());
        assertTrue(titleVals.contains("A title"));
    }

    @Test
    void testGetDefaultType() throws InterruptedException, IOException, URISyntaxException {
        final IRI identifier = rdf.createIRI(baseUrl + pid);
        final Map<String, String> metadata = singletonMap(
                CONTENT_TYPE, WebContent.contentTypeTurtle);
        client.put(identifier, getTestResource(), metadata);
        final String res = client.getDefaultType(identifier);
        assertEquals(909, res.length());
    }

    @Test
    void testGetWithContentType() throws InterruptedException, IOException, URISyntaxException {
        final IRI identifier = rdf.createIRI(baseUrl + pid);
        final Map<String, String> metadata = singletonMap(
                CONTENT_TYPE, WebContent.contentTypeTurtle);
        client.put(identifier, getTestResource(), metadata);
        final String res = client.getWithContentType(identifier, WebContent.contentTypeNTriples);
        assertEquals(116, res.length());
    }

    @Test
    void testGetAcceptDatetime() throws InterruptedException, IOException, URISyntaxException {
        final IRI identifier = rdf.createIRI(baseUrl + pid);
        final Map<String, String> metadata = singletonMap(
                CONTENT_TYPE, WebContent.contentTypeTextPlain);
        client.put(identifier, getTestBinary(), metadata);
        client.put(identifier, getRevisedTestBinary(), metadata);
        final Long timestamp = now().toEpochMilli();
        final Map<String, List<String>> res = client.getAcceptDatetime(
                identifier, String.valueOf(timestamp));
        final List<Link> links = res.get(LINK).stream().map(Link::valueOf).collect(toList());
        assertTrue(links.stream().anyMatch(l -> l.getRels().contains("memento")));
    }

    @Test
    void testGetTimeMapLinkDefaultFormat() throws InterruptedException, IOException,
            URISyntaxException {
        final IRI identifier = rdf.createIRI(baseUrl + pid);
        final Map<String, String> metadata = singletonMap(
                CONTENT_TYPE, WebContent.contentTypeTextPlain);
        client.put(identifier, getTestBinary(), metadata);
        client.put(identifier, getRevisedTestBinary(), metadata);
        final String res = client.getTimeMapLinkDefaultFormat(identifier);
        final List<Link> entityLinks = stream(res.split(",\n")).map(Link::valueOf).collect(
                toList());
        assertTrue(entityLinks.stream().findFirst().filter(
                l -> l.getRel().contains("timemap")).isPresent());
    }

    @Test
    void testGetTimeMapLinkJsonProfile() throws InterruptedException, IOException,
            URISyntaxException {
        final IRI identifier = rdf.createIRI(baseUrl + pid);
        final Map<String, String> metadata = singletonMap(
                CONTENT_TYPE, WebContent.contentTypeTextPlain);
        client.put(identifier, getTestBinary(), metadata);
        client.put(identifier, getRevisedTestBinary(), metadata);
        final String profile = JSONLD.compacted.getIRIString();
        final String res = client.getTimeMapJsonProfile(identifier, profile);
        final Map<String, Object> obj = MAPPER.readValue(
                res,
                new TypeReference<Map<String, Object>>() {
                });

        @SuppressWarnings("unchecked") final List<Map<String, Object>> graph = (List<Map<String,
                Object>>) obj.get(
                "@graph");
        assertTrue(graph.stream().anyMatch(x -> x.containsKey("@id") &&
                x.get("@id").equals(baseUrl + pid) &&
                x.containsKey("timegate") && x.containsKey("timemap") && x.containsKey(
                "memento:memento")));
    }

    @Test
    void testGetVersionJson() throws InterruptedException, IOException, URISyntaxException {
        final IRI identifier = rdf.createIRI(baseUrl + pid);
        final Map<String, String> metadata = singletonMap(
                CONTENT_TYPE, WebContent.contentTypeTurtle);
        client.put(identifier, getTestResource(), metadata);
        client.put(identifier, getRevisedTestResource(), metadata);
        final List<Link> links = client.head(identifier).get(LINK).stream().map(
                Link::valueOf).collect(
                toList());
        final List<String> dates = links.stream().map(l -> l.getParams().get("datetime")).collect(
                Collectors.toList());
        final String timestamp = String.valueOf(Instant.ofEpochSecond(
                LocalDateTime.parse(dates.get(1), RFC_1123_DATE_TIME).toEpochSecond(
                        ZoneOffset.UTC)).toEpochMilli());
        final String profile = JSONLD.compacted.getIRIString();
        final String res = client.getVersionJson(identifier, profile, timestamp);
        final Map<String, Object> obj = MAPPER.readValue(
                res,
                new TypeReference<Map<String, Object>>() {
                });
        final List<Object> objs = new ArrayList<>(obj.values());
        assertEquals("A new title", objs.get(1));
    }

    @Test
    void testGetBinary() throws InterruptedException, IOException, URISyntaxException {
        final IRI identifier = rdf.createIRI(baseUrl + pid);
        final Map<String, String> metadata = singletonMap(
                CONTENT_TYPE, WebContent.contentTypeTextPlain);
        client.put(identifier, getTestBinary(), metadata);
        final Path tempDir = Files.createTempDirectory("test");
        final Path tempFile = Files.createTempFile(tempDir, "test-binary", ".txt");
        client.getBinary(identifier, tempFile);
    }

    @Test
    void testGetBinaryDigest() throws InterruptedException, IOException, URISyntaxException {
        final IRI identifier = rdf.createIRI(baseUrl + pid);
        final Map<String, String> metadata = singletonMap(
                CONTENT_TYPE, WebContent.contentTypeTextPlain);
        client.put(identifier, getTestBinary(), metadata);
        final String digest = client.getBinaryDigest(identifier, "MD5");
    }

    @Test
    void testGetBinaryVersion() throws InterruptedException, IOException, URISyntaxException {
        final IRI identifier = rdf.createIRI(baseUrl + pid);
        final Map<String, String> metadata = singletonMap(
                CONTENT_TYPE, WebContent.contentTypeTextPlain);
        client.put(identifier, getTestBinary(), metadata);
        client.put(identifier, getRevisedTestBinary(), metadata);
        final List<Link> links = client.head(identifier).get(LINK).stream().map(
                Link::valueOf).collect(
                toList());
        final List<String> dates = links.stream().map(l -> l.getParams().get("datetime")).collect(
                Collectors.toList());
        final String date = dates.get(1);
        final String timestamp = String.valueOf(Instant.ofEpochSecond(
                LocalDateTime.parse(date, RFC_1123_DATE_TIME).toEpochSecond(
                        ZoneOffset.UTC)).toEpochMilli());
        final Path tempDir = Files.createTempDirectory("test");
        final Path tempFile = Files.createTempFile(tempDir, "test-binary", ".txt");
        client.getBinaryVersion(identifier, tempFile, timestamp);
    }

    @Test
    void testGetRange() throws InterruptedException, IOException, URISyntaxException {

    }

    @Test
    void testNewLdpDc() throws InterruptedException, IOException, URISyntaxException {
        final IRI identifier = rdf.createIRI(baseUrl);
        final String slug = pid;
        client.newLdpDc(identifier, slug, identifier);
    }

    @Test
    void testGetPrefer() throws InterruptedException, IOException, URISyntaxException {
        final IRI identifier = rdf.createIRI(baseUrl);
        final String slug = pid;
        client.newLdpDc(identifier, slug, identifier);
        final IRI containerIri = rdf.createIRI(baseUrl + pid);
        final String prefer =
                "return=representation; omit=\"" + LDP.PreferContainment.getIRIString()
                        + "\"";
        final String res = client.getPrefer(containerIri, prefer);
    }

    @Test
    void testGetPreferServerManaged() throws InterruptedException, IOException, URISyntaxException {
        final IRI identifier = rdf.createIRI(baseUrl);
        final String slug = pid;
        client.newLdpDc(identifier, slug, identifier);
        final IRI containerIri = rdf.createIRI(baseUrl + pid);
        final String res = client.getPreferServerManaged(containerIri);
    }

    @Test
    void testGetPreferMinimal() throws InterruptedException, IOException, URISyntaxException {
        final IRI identifier = rdf.createIRI(baseUrl);
        final String slug = pid;
        client.newLdpDc(identifier, slug, identifier);
        final IRI containerIri = rdf.createIRI(baseUrl + pid);
        final String res = client.getPreferMinimal(containerIri);
    }

    @Test
    void testGetJsonProfile() throws InterruptedException, IOException, URISyntaxException {
        final IRI identifier = rdf.createIRI(baseUrl + pid);
        final Map<String, String> metadata = singletonMap(
                CONTENT_TYPE, WebContent.contentTypeTurtle);
        client.put(identifier, getTestResource(), metadata);
        final String profile = JSONLD.expanded.getIRIString();
        final String res = client.getJsonProfile(identifier, profile);
    }

    @Test
    void testGetJsonLDF() throws InterruptedException, IOException, URISyntaxException {
        final IRI identifier = rdf.createIRI(baseUrl + pid);
        final Map<String, String> metadata = singletonMap(
                CONTENT_TYPE, WebContent.contentTypeTurtle);
        client.put(identifier, getTestGraph(), metadata);
        final String object = URLEncoder.encode("A Body", StandardCharsets.UTF_8.toString());
        //final String predicate = URLEncoder.encode(OA.hasBody.getIRIString(), StandardCharsets
        // .UTF_8.toString());
        // NOTE: params with special characters (even if encoded) do not work (???).
        final String res = client.getJsonLDF(identifier, null, null, object);
    }

    @Test
    void testGetAcl() throws InterruptedException, IOException, URISyntaxException {
        final IRI aclIdentifier = rdf.createIRI(baseUrl + pid + "?ext=acl");
        final IRI identifier = rdf.createIRI(baseUrl + pid);
        final String keyString = "secret";
        final String key = getEncoder().encodeToString(keyString.getBytes());
        final String token = Jwts.builder().setSubject("test-user").setIssuer("http://localhost")
                .signWith(SignatureAlgorithm.HS512, key).compact();
        final Map<String, String> metadata = new HashMap<>();
        metadata.put(AUTHORIZATION, "Bearer " + token);
        metadata.put(CONTENT_TYPE, WebContent.contentTypeTurtle);
        client.putWithAuth(aclIdentifier, getTestAcl(), metadata);
        final String res = client.getAcl(identifier, metadata);
    }

    @Test
    void testGetCORS() throws InterruptedException, IOException, URISyntaxException {
        final IRI identifier = rdf.createIRI(baseUrl + pid);
        final IRI origin = rdf.createIRI(baseUrl);
        final Map<String, String> metadata = singletonMap(
                CONTENT_TYPE, WebContent.contentTypeTurtle);
        client.put(identifier, getTestGraph(), metadata);
        final Map<String, List<String>> headers = client.getCORS(identifier, origin);
    }

    @Test
    void testGetCORSSimple() throws InterruptedException, IOException, URISyntaxException {
        final IRI identifier = rdf.createIRI(baseUrl + pid);
        final IRI origin = rdf.createIRI(baseUrl);
        final Map<String, String> metadata = singletonMap(
                CONTENT_TYPE, WebContent.contentTypeTurtle);
        client.put(identifier, getTestGraph(), metadata);
        final Map<String, List<String>> headers = client.getCORSSimple(identifier, origin);
    }

    @Test
    void testGetWithEntrySet() throws InterruptedException, IOException, URISyntaxException {
        final IRI identifier = rdf.createIRI(baseUrl + pid);
        final Map<String, String> metadata = singletonMap(
                CONTENT_TYPE, WebContent.contentTypeTurtle);
        client.put(identifier, getTestGraph(), metadata);
        final Map<String, String> headers = new HashMap<>();
        headers.put("Prefer", "return=representation; include=\""
                + Trellis.PreferServerManaged.getIRIString() + "\"");
        headers.put(ACCEPT, WebContent.contentTypeJSONLD);
        final String res = client.getWithMetadata(identifier, headers);
    }

    @Test
    void testOptions() throws InterruptedException, IOException, URISyntaxException {
        final IRI identifier = rdf.createIRI(baseUrl + pid);
        final Map<String, String> metadata = singletonMap(
                CONTENT_TYPE, WebContent.contentTypeTurtle);
        client.put(identifier, getTestGraph(), metadata);
        final Map<String, List<String>> headers = client.options(identifier);
    }

    @Test
    void testPost() throws InterruptedException, IOException, URISyntaxException {
        final IRI identifier = rdf.createIRI(baseUrl + pid);
        final IRI container = rdf.createIRI(baseUrl);
        final Map<String, String> metadata = singletonMap(
                CONTENT_TYPE, WebContent.contentTypeTurtle);
        client.newLdpDc(container, pid, container);
        client.post(identifier, getTestResource(), metadata);
    }

    @Test
    void testPostWithMetadata() throws InterruptedException, IOException, URISyntaxException {
        final IRI identifier = rdf.createIRI(baseUrl + pid);
        final IRI container = rdf.createIRI(baseUrl);
        final Map<String, String> metadata = new HashMap<>();
        metadata.put(CONTENT_TYPE, WebContent.contentTypeTextPlain);
        metadata.put("Digest", "md5=1VOyRwUXW1CPdC5nelt7GQ==");
        client.newLdpDc(container, pid, container);
        client.postWithMetadata(identifier, getTestBinary(), metadata);
    }

    @Test
    void testPostwithAuth() throws InterruptedException, IOException, URISyntaxException {
        final IRI identifier = rdf.createIRI(baseUrl + pid);
        final IRI container = rdf.createIRI(baseUrl);
        final String keyString = "secret";
        final String key = getEncoder().encodeToString(keyString.getBytes());
        final String token = Jwts.builder().setSubject("test-user").setIssuer("http://localhost")
                .signWith(SignatureAlgorithm.HS512, key).compact();
        final Map<String, String> metadata = new HashMap<>();
        metadata.put(AUTHORIZATION, "Bearer " + token);
        metadata.put(CONTENT_TYPE, WebContent.contentTypeTurtle);
        client.newLdpDc(container, pid, container);
        client.post(identifier, getTestResource(), metadata);
    }

    @Test
    void testPostSlug() throws InterruptedException, IOException, URISyntaxException {
        final IRI identifier = rdf.createIRI(baseUrl + pid);
        final IRI container = rdf.createIRI(baseUrl);
        final Map<String, String> metadata = singletonMap(
                CONTENT_TYPE, WebContent.contentTypeTurtle);
        client.newLdpDc(container, pid, container);
        final String slug = "namedResource";
        client.postSlug(identifier, slug, getTestResource(), metadata);
    }

    @Test
    void testPostBinarywithDigest() throws InterruptedException, IOException, URISyntaxException {
        final IRI identifier = rdf.createIRI(baseUrl + pid);
        final IRI container = rdf.createIRI(baseUrl);
        final Map<String, String> metadata = singletonMap(
                CONTENT_TYPE, WebContent.contentTypeTextPlain);
        final String digest = "md5=1VOyRwUXW1CPdC5nelt7GQ==";
        client.newLdpDc(container, pid, container);
        client.postBinaryWithDigest(identifier, getTestBinary(), metadata, digest);
    }

    @Test
    void testPutWithMetadata() throws InterruptedException, IOException, URISyntaxException {
        final IRI identifier = rdf.createIRI(baseUrl + pid);
        final Map<String, String> metadata = new HashMap<>();
        metadata.put(CONTENT_TYPE, WebContent.contentTypeTurtle);
        metadata.put("Etag", "053036f0a8a95b3ecf4fee30b9c3145f");
        client.put(identifier, getTestResource(), metadata);
        client.putWithMetadata(identifier, getRevisedTestResource(), metadata);
    }

    @Test
    void testPutIfMatch() throws InterruptedException, IOException, URISyntaxException {
        final IRI identifier = rdf.createIRI(baseUrl + pid);
        final Map<String, String> metadata = singletonMap(
                CONTENT_TYPE, WebContent.contentTypeTurtle);
        final String etag = "053036f0a8a95b3ecf4fee30b9c3145f";
        client.put(identifier, getTestResource(), metadata);
        client.putIfMatch(identifier, getRevisedTestResource(), metadata, etag);
    }

    @Test
    void testPutBinarywithDigest() throws InterruptedException, IOException, URISyntaxException {
        final IRI identifier = rdf.createIRI(baseUrl + pid);
        final Map<String, String> metadata = singletonMap(
                CONTENT_TYPE, WebContent.contentTypeTextPlain);
        final String digest = "md5=1VOyRwUXW1CPdC5nelt7GQ==";
        client.putBinaryWithDigest(identifier, getTestBinary(), metadata, digest);
    }

    @Test
    void testPutIfUnmodified() throws InterruptedException, IOException, URISyntaxException {
        final IRI identifier = rdf.createIRI(baseUrl + pid);
        final Map<String, String> metadata = singletonMap(
                CONTENT_TYPE, WebContent.contentTypeTextPlain);
        final String time = "Wed, 19 Oct 2016 10:15:00 GMT";
        client.putIfUnmodified(identifier, getTestBinary(), metadata, time);
    }

    @Test
    void testDelete() throws InterruptedException, IOException, URISyntaxException {
        final IRI identifier = rdf.createIRI(baseUrl + pid);
        final Map<String, String> metadata = singletonMap(
                CONTENT_TYPE, WebContent.contentTypeTextPlain);
        client.put(identifier, getTestBinary(), metadata);
        client.delete(identifier);
    }

    @Test
    void testPatch() throws InterruptedException, IOException, URISyntaxException {
        final IRI identifier = rdf.createIRI(baseUrl + pid);
        final Map<String, String> metadata = singletonMap(
                CONTENT_TYPE, WebContent.contentTypeSPARQLUpdate);
        client.put(identifier, getTestResource(), metadata);
        client.patch(identifier, getSparqlUpdate());
    }
}
