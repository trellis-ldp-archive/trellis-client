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

import static cool.pandora.ldpclient.LdpClientImpl.buildLDFQuery;
import static io.dropwizard.testing.ConfigOverride.config;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static java.time.Instant.now;
import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;
import static java.util.Arrays.stream;
import static java.util.Base64.getEncoder;
import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.HttpHeaders.ACCEPT;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.HttpHeaders.LINK;
import static org.apache.jena.riot.WebContent.contentTypeJSONLD;
import static org.apache.jena.riot.WebContent.contentTypeNTriples;
import static org.apache.jena.riot.WebContent.contentTypeTextPlain;
import static org.apache.jena.riot.WebContent.contentTypeTurtle;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.trellisldp.http.domain.HttpConstants.ACCEPT_PATCH;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.testing.DropwizardTestSupport;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.ws.rs.core.Link;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.jena.JenaRDF;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.trellisldp.app.TrellisApplication;
import org.trellisldp.app.config.TrellisConfiguration;
import org.trellisldp.vocabulary.ACL;
import org.trellisldp.vocabulary.DC;
import org.trellisldp.vocabulary.JSONLD;
import org.trellisldp.vocabulary.LDP;
import org.trellisldp.vocabulary.OA;
import org.trellisldp.vocabulary.RDF;

/**
 * LdpClientTest.
 *
 * @author christopher-johnson
 */
class LdpClientTest {

    private static final DropwizardTestSupport<TrellisConfiguration> APP = new DropwizardTestSupport<>(
            TrellisApplication.class, resourceFilePath("trellis-config.yml"),
            config("server.applicationConnectors[0].port", "0"),
            config("binaries", resourceFilePath("data") + "/binaries"),
            config("mementos", resourceFilePath("data") + "/mementos"),
            config("namespaces", resourceFilePath("data/namespaces.json")),
            config("server.applicationConnectors[1].keyStorePath", resourceFilePath("keystore/trellis.jks")));
    private static final JenaRDF rdf = new JenaRDF();
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static String baseUrl;
    private static String pid;
    private final LdpClient client = new LdpClientImpl();

    @BeforeAll
    static void initAll() {
        APP.before();
        baseUrl = "http://localhost:" + APP.getLocalPort() + "/";
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

    private static InputStream getTestJsonResource() {
        return LdpClientTest.class.getResourceAsStream("/webanno.complete.json");
    }

    private static InputStream getRevisedTestResource() {
        return LdpClientTest.class.getResourceAsStream("/simpleTripleRev.ttl");
    }

    private static InputStream getTestGraph() {
        return LdpClientTest.class.getResourceAsStream("/graph.ttl");
    }

    private static InputStream getSparqlUpdate() {
        return LdpClientTest.class.getResourceAsStream("/sparql-update.txt");
    }

    @BeforeEach
    void init() {
        pid = "ldp-test-" + UUID.randomUUID().toString();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testBuildLDFQuery() {
        final String subject = "http://some.annotation.resource";
        final String predicate = RDF.type.getIRIString();
        final String object = OA.Annotation.getIRIString();
        final String q = buildLDFQuery(subject, predicate, object);
        final String qp = buildLDFQuery(null, predicate, null);
        assertEquals("?subject=http://some.annotation.resource&predicate=http://www"
                + ".w3.org/1999/02/22-rdf-syntax-ns#type&object=http://www.w3.org/ns/oa#Annotation", q);
        assertEquals("?predicate=http://www.w3.org/1999/02/22-rdf-syntax-ns#type", qp);
    }

    @Test
    void testHead() throws LdpClientException {
        try {
            final IRI identifier = rdf.createIRI(Objects.requireNonNull(baseUrl));
            final Map<String, List<String>> res = client.head(identifier);
            assertTrue(res.containsKey(ACCEPT_PATCH));
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Test
    void testGetJson() throws LdpClientException {
        try {
            final IRI identifier = rdf.createIRI(baseUrl + pid);
            client.put(identifier, getTestResource(), contentTypeTurtle);
            final String res = client.getJson(identifier);
            final List<Map<String, Object>> obj = MAPPER.readValue(res, new TypeReference<List<Map<String, Object>>>() {
            });
            assertEquals(1L, obj.size());

            @SuppressWarnings("unchecked")

            final List<Map<String, String>> titles = (List<Map<String, String>>) obj.get(0).get(
                    DC.title.getIRIString());

            final List<String> titleVals = titles.stream().map(x -> x.get("@value")).collect(toList());

            assertEquals(1L, titleVals.size());
            assertTrue(titleVals.contains("A title"));
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Test
    void testGetDefaultType() throws LdpClientException {
        try {
            final IRI identifier = rdf.createIRI(baseUrl + pid);
            client.put(identifier, getTestResource(), contentTypeTurtle);
            final String res = client.getDefaultType(identifier);
            assertTrue(res.contains("dc:title"));
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Test
    void testGetWithContentType() throws LdpClientException {
        try {
            final IRI identifier = rdf.createIRI(baseUrl + pid);
            client.put(identifier, getTestResource(), contentTypeTurtle);
            final String res = client.getWithContentType(identifier, contentTypeNTriples);
            assertEquals(116, res.length());
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Test
    void testGetAcceptDatetime() throws LdpClientException {
        try {
            final IRI identifier = rdf.createIRI(baseUrl + pid);
            client.put(identifier, getTestBinary(), contentTypeTextPlain);
            client.put(identifier, getRevisedTestBinary(), contentTypeTextPlain);
            final Long timestamp = now().toEpochMilli();
            final Map<String, List<String>> res = client.getAcceptDatetime(identifier, String.valueOf(timestamp));
            final List<Link> links = res.get(LINK).stream().map(Link::valueOf).collect(toList());
            assertTrue(links.stream().anyMatch(l -> l.getRels().contains("memento")));
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Test
    void testGetTimeMapLinkDefaultFormat() throws LdpClientException {
        try {
            final IRI identifier = rdf.createIRI(baseUrl + pid);
            client.put(identifier, getTestBinary(), contentTypeTextPlain);
            client.put(identifier, getRevisedTestBinary(), contentTypeTextPlain);
            final String res = client.getTimeMapLinkDefaultFormat(identifier);
            final List<Link> entityLinks = stream(res.split(",\n")).map(Link::valueOf).collect(toList());
            assertTrue(entityLinks.stream().findFirst().filter(l -> l.getRel().contains("timemap")).isPresent());
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Test
    void testGetTimeMapLinkJsonProfile() throws LdpClientException {
        try {
            final IRI identifier = rdf.createIRI(baseUrl + pid);
            client.put(identifier, getTestBinary(), contentTypeTextPlain);
            client.put(identifier, getRevisedTestBinary(), contentTypeTextPlain);
            final String profile = JSONLD.compacted.getIRIString();
            final String res = client.getTimeMapJsonProfile(identifier, profile);
            final Map<String, Object> obj = MAPPER.readValue(res, new TypeReference<Map<String, Object>>() {
            });

            @SuppressWarnings("unchecked")

            final List<Map<String, Object>> graph = (List<Map<String, Object>>) obj.get("@graph");
            assertTrue(graph.stream().anyMatch(
                    x -> x.containsKey("@id") && x.get("@id").equals(baseUrl + pid) && x.containsKey("timegate")
                            && x.containsKey("timemap") && x.containsKey("memento:memento")));
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Test
    void testGetVersionJson() throws LdpClientException {
        try {
            final IRI identifier = rdf.createIRI(baseUrl + pid);
            client.put(identifier, getTestResource(), contentTypeTurtle);
            client.put(identifier, getRevisedTestResource(), contentTypeTurtle);
            final List<Link> links = client.head(identifier).get(LINK).stream().map(Link::valueOf).collect(toList());
            final List<String> dates = links.stream().map(l -> l.getParams().get("datetime")).collect(
                    Collectors.toList());
            final String date = dates.stream().filter(Objects::nonNull).findFirst().orElse("");
            final String timestamp = String.valueOf(Instant.ofEpochSecond(
                    LocalDateTime.parse(date, RFC_1123_DATE_TIME).toEpochSecond(ZoneOffset.UTC)).toEpochMilli());
            final String profile = JSONLD.compacted.getIRIString();
            final String res = client.getVersionJson(identifier, profile, timestamp);
            final Map<String, Object> obj = MAPPER.readValue(res, new TypeReference<Map<String, Object>>() {
            });
            final List<Object> objs = new ArrayList<>(obj.values());
            assertEquals("A new title", objs.get(1));
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Test
    void testGetBinary() throws LdpClientException {
        try {
            final IRI identifier = rdf.createIRI(baseUrl + pid);
            client.put(identifier, getTestBinary(), contentTypeTextPlain);
            final Path tempDir = Files.createTempDirectory("test");
            final Path tempFile = Files.createTempFile(tempDir, "test-binary", ".txt");
            client.getBinary(identifier, tempFile);
            assertTrue(tempFile.toFile().exists());
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Test
    void testGetBinaryBytes() throws LdpClientException {
        try {
            final IRI identifier = rdf.createIRI(baseUrl + pid);
            client.put(identifier, getTestBinary(), contentTypeTextPlain);
            final byte[] bytes = client.getBinary(identifier);
            assertEquals(10, bytes.length);
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Test
    void testGetBinaryDigest() throws LdpClientException {
        try {
            final IRI identifier = rdf.createIRI(baseUrl + pid);
            client.put(identifier, getTestBinary(), contentTypeTextPlain);
            final String digest = client.getBinaryDigest(identifier, "MD5");
            assertEquals("md5=1VOyRwUXW1CPdC5nelt7GQ==", digest);
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Test
    void testGetBinaryVersion() throws LdpClientException {
        try {
            final IRI identifier = rdf.createIRI(baseUrl + pid);
            client.put(identifier, getTestBinary(), contentTypeTextPlain);
            client.put(identifier, getRevisedTestBinary(), contentTypeTextPlain);
            final List<Link> links = client.head(identifier).get(LINK).stream().map(Link::valueOf).collect(toList());
            final List<String> dates = links.stream().map(l -> l.getParams().get("datetime")).collect(
                    Collectors.toList());
            final String date = dates.stream().filter(Objects::nonNull).findFirst().orElse("");
            final String timestamp = String.valueOf(Instant.ofEpochSecond(
                    LocalDateTime.parse(date, RFC_1123_DATE_TIME).toEpochSecond(ZoneOffset.UTC)).toEpochMilli());
            final Path tempDir = Files.createTempDirectory("test");
            final Path tempFile = Files.createTempFile(tempDir, "test-binary", ".txt");
            client.getBinaryVersion(identifier, tempFile, timestamp);
            assertTrue(tempFile.toFile().exists());
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Test
    void testGetBinaryVersionBytes() throws LdpClientException {
        try {
            final IRI identifier = rdf.createIRI(baseUrl + pid);
            client.put(identifier, getTestBinary(), contentTypeTextPlain);
            client.put(identifier, getRevisedTestBinary(), contentTypeTextPlain);
            final List<Link> links = client.head(identifier).get(LINK).stream().map(Link::valueOf).collect(toList());
            final List<String> dates = links.stream().map(l -> l.getParams().get("datetime")).collect(
                    Collectors.toList());
            final String date = dates.stream().filter(Objects::nonNull).findFirst().orElse("");
            final String timestamp = String.valueOf(Instant.ofEpochSecond(
                    LocalDateTime.parse(date, RFC_1123_DATE_TIME).toEpochSecond(ZoneOffset.UTC)).toEpochMilli());
            final byte[] bytes = client.getBinaryVersion(identifier, timestamp);
            assertEquals(14, bytes.length);
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Test
    void testGetRange() throws LdpClientException {
        try {
            final IRI identifier = rdf.createIRI(baseUrl + pid);
            client.put(identifier, getTestBinary(), contentTypeTextPlain);
            final byte[] bytes = client.getRange(identifier, "bytes=3-10");
            assertEquals(7, bytes.length);
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Test
    void testGetPrefer() throws LdpClientException {
        try {
            final IRI identifier = rdf.createIRI(baseUrl);
            final String slug = pid;
            client.newLdpDc(identifier, slug, identifier);
            final IRI containerIri = rdf.createIRI(baseUrl + pid);
            final IRI memberIri = rdf.createIRI(baseUrl + pid + "/test-member");
            client.put(memberIri, getTestResource(), contentTypeTurtle);
            final String prefer = "return=representation; omit=\"" + LDP.PreferContainment.getIRIString() + "\"";
            final String res = client.getPrefer(containerIri, prefer);
            assertFalse(res.contains("ldp:contains"));
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Test
    void testGetPreferMinimal() throws LdpClientException {
        try {
            final IRI identifier = rdf.createIRI(baseUrl);
            final String slug = pid;
            client.newLdpDc(identifier, slug, identifier);
            final IRI containerIri = rdf.createIRI(baseUrl + pid);
            final IRI memberIri = rdf.createIRI(baseUrl + pid + "/test-member");
            client.put(memberIri, getTestResource(), contentTypeTurtle);
            final String res = client.getPreferMinimal(containerIri);
            assertFalse(res.contains("ldp:contains"));
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Test
    void testGetJsonProfile() throws LdpClientException {
        try {
            final IRI identifier = rdf.createIRI(baseUrl + pid);
            client.put(identifier, getTestResource(), contentTypeTurtle);
            final String profile = JSONLD.expanded.getIRIString();
            final String res = client.getJsonProfile(identifier, profile);
            assertEquals(137, res.length());
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    //test requires that profile be included in contextWhitelist
    @Test
    void testGetCustomJsonProfile() throws LdpClientException {
        try {
            final IRI identifier = rdf.createIRI(baseUrl + pid);
            client.put(identifier, getTestJsonResource(), contentTypeJSONLD);
            final String profile = "http://www.w3.org/ns/anno.jsonld";
            final String res = client.getJsonProfile(identifier, profile);
            assertTrue(res.contains("@graph"));
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Test
    void testGetJsonProfileLDF() throws LdpClientException {
        try {
            final IRI identifier = rdf.createIRI(baseUrl + pid);
            client.put(identifier, getTestGraph(), contentTypeTurtle);
            final String object = URLEncoder.encode("A Body", StandardCharsets.UTF_8.toString());
            final String profile = JSONLD.compacted.getIRIString();
            // NOTE: params with reserved characters like # (even if encoded) do not work (???).
            final String res = client.getJsonProfileLDF(identifier, profile, null, null, object);
            assertTrue(res.contains("hasBody"));
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Test
    void testGetJsonLDF() throws LdpClientException {
        try {
            final IRI identifier = rdf.createIRI(baseUrl + pid);
            client.put(identifier, getTestGraph(), contentTypeTurtle);
            final String object = URLEncoder.encode("A Body", StandardCharsets.UTF_8.toString());
            //final String predicate = URLEncoder.encode(OA.hasBody.getIRIString(), StandardCharsets
            //.UTF_8.toString());
            // NOTE: params with reserved characters like # (even if encoded) do not work (Incident Report 9117860).
            final String res = client.getJsonLDF(identifier, null, null, object);
            assertEquals(137, res.length());
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Test
    void testGetAcl() throws LdpClientException {
        try {
            final IRI aclIdentifier = rdf.createIRI(baseUrl + pid + "?ext=acl");
            final IRI identifier = rdf.createIRI(baseUrl + pid);
            final String keyString = "secret";
            final String key = getEncoder().encodeToString(keyString.getBytes());
            final String token =
                    "Bearer " + Jwts.builder().setSubject("test-user").setIssuer("http://localhost").signWith(
                            SignatureAlgorithm.HS512, key).compact();
            final Set<IRI> modes = new HashSet<>();
            modes.add(ACL.Read);
            modes.add(ACL.Write);
            modes.add(ACL.Control);
            final IRI agent = rdf.createIRI("http://localhost/test-user");
            final IRI accessTo = rdf.createIRI(baseUrl + pid);
            final ACLStatement acl = new ACLStatement(modes, agent, accessTo);
            client.put(aclIdentifier, new ByteArrayInputStream(acl.getACL().toByteArray()), contentTypeNTriples);
            client.putWithAuth(identifier, getTestResource(), contentTypeTurtle, token);
            final String res = client.getAcl(identifier, contentTypeNTriples);
            assertEquals(1017, res.length());
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Test
    void testGetCORS() throws LdpClientException {
        try {
            final IRI identifier = rdf.createIRI(baseUrl + pid);
            final IRI origin = rdf.createIRI(baseUrl);
            client.put(identifier, getTestGraph(), contentTypeTurtle);
            final Map<String, List<String>> headers = client.getCORS(identifier, origin);
            assertTrue(headers.containsKey("Access-Control-Expose-Headers"));
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Test
    void testGetCORSSimple() throws LdpClientException {
        try {
            final IRI identifier = rdf.createIRI(baseUrl + pid);
            final IRI origin = rdf.createIRI(baseUrl);
            client.put(identifier, getTestGraph(), contentTypeTurtle);
            final Map<String, List<String>> headers = client.getCORSSimple(identifier, origin);
            assertTrue(headers.containsKey("Access-Control-Allow-Origin"));
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Test
    void testGetWithMetadata() throws LdpClientException {
        try {
            final IRI identifier = rdf.createIRI(baseUrl + pid);
            client.put(identifier, getTestGraph(), contentTypeTurtle);
            final Map<String, String> metadata = new HashMap<>();
            metadata.put("Prefer", "return=representation; omit=\"" + LDP.PreferContainment.getIRIString() + "\"");
            metadata.put(ACCEPT, contentTypeJSONLD);
            final String res = client.getWithMetadata(identifier, metadata);
            assertTrue(res.contains("http://www.w3.org/ns/oa#HttpRequestState"));
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Test
    void testGetBytesWithMetadata() throws LdpClientException {
        try {
            final IRI identifier = rdf.createIRI(baseUrl + pid);
            client.put(identifier, getTestGraph(), contentTypeTurtle);
            final Map<String, String> metadata = new HashMap<>();
            metadata.put("Prefer", "return=representation; omit=\"" + LDP.PreferContainment.getIRIString() + "\"");
            metadata.put(ACCEPT, contentTypeJSONLD);
            final byte[] res = client.getBytesWithMetadata(identifier, metadata);
            assertEquals(282, res.length);
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Test
    void testGetResponse() throws LdpClientException {
        try {
            final IRI identifier = rdf.createIRI(baseUrl + pid);
            client.put(identifier, getTestGraph(), contentTypeTurtle);
            final Map<String, String> metadata = new HashMap<>();
            metadata.put("Prefer", "return=representation; omit=\"" + LDP.PreferContainment.getIRIString() + "\"");
            metadata.put(ACCEPT, contentTypeJSONLD);
            final Map<String, Map<String, List<String>>> res = client.getResponse(identifier, metadata);
            final String body = res.entrySet().stream().map(Map.Entry::getKey).findFirst().orElse("");
            final Map<String, List<String>> headers = res.entrySet().stream().map(
                    Map.Entry::getValue).findFirst().orElse(null);
            final List<List<String>> header = Objects.requireNonNull(headers).entrySet().stream().filter(
                    h -> h.getKey().equals("content-type")).map(Map.Entry::getValue).collect(Collectors.toList());
            final String contentType = header.stream().flatMap(List::stream).collect(
                    Collectors.toList()).stream().findAny().orElse("");
            assertEquals(contentTypeJSONLD, contentType);
            assertTrue(body.contains("A title"));
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Test
    void testOptions() throws LdpClientException {
        try {
            final IRI identifier = rdf.createIRI(baseUrl + pid);
            client.put(identifier, getTestGraph(), contentTypeTurtle);
            final Map<String, List<String>> headers = client.options(identifier);
            assertTrue(headers.containsKey("Allow"));
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Test
    void testPost() throws LdpClientException {
        try {
            final IRI identifier = rdf.createIRI(baseUrl + pid);
            final IRI container = rdf.createIRI(baseUrl);
            client.newLdpDc(container, pid, container);
            client.post(identifier, getTestResource(), contentTypeTurtle);
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Test
    void testPostWithMetadata() throws LdpClientException {
        try {
            final IRI identifier = rdf.createIRI(baseUrl + pid);
            final IRI container = rdf.createIRI(baseUrl);
            final Map<String, String> metadata = new HashMap<>();
            metadata.put(CONTENT_TYPE, contentTypeTextPlain);
            metadata.put("Digest", "md5=1VOyRwUXW1CPdC5nelt7GQ==");
            client.newLdpDc(container, pid, container);
            client.postWithMetadata(identifier, getTestBinary(), metadata);
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Test
    void testPostWithAuth() throws LdpClientException {
        try {
            final IRI identifier = rdf.createIRI(baseUrl + pid);
            final IRI container = rdf.createIRI(baseUrl);
            final String keyString = "secret";
            final String key = getEncoder().encodeToString(keyString.getBytes());
            final String token =
                    "Bearer " + Jwts.builder().setSubject("test-user").setIssuer("http://localhost").signWith(
                            SignatureAlgorithm.HS512, key).compact();
            client.newLdpDc(container, pid, container);
            client.postWithAuth(identifier, getTestResource(), contentTypeTurtle, token);
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Test
    void testPostSlug() throws LdpClientException {
        try {
            final IRI identifier = rdf.createIRI(baseUrl + pid);
            final IRI container = rdf.createIRI(baseUrl);
            client.newLdpDc(container, pid, container);
            final String slug = "namedResource";
            client.postSlug(identifier, slug, getTestResource(), contentTypeTurtle);
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Test
    void testPostBinarywithDigest() throws LdpClientException {
        try {
            final IRI identifier = rdf.createIRI(baseUrl + pid);
            final IRI container = rdf.createIRI(baseUrl);
            final String digest = "md5=1VOyRwUXW1CPdC5nelt7GQ==";
            client.newLdpDc(container, pid, container);
            client.postBinaryWithDigest(identifier, getTestBinary(), contentTypeTextPlain, digest);
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Test
    void testNewLdpDc() throws LdpClientException {
        try {
            final IRI identifier = rdf.createIRI(baseUrl);
            final String slug = pid;
            client.newLdpDc(identifier, slug, identifier);
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Test
    void testNewLdpDcWithAuth() throws LdpClientException {
        try {
            final IRI baseUri = rdf.createIRI(baseUrl);
            final String keyString = "secret";
            final String key = getEncoder().encodeToString(keyString.getBytes());
            final String token =
                    "Bearer " + Jwts.builder().setSubject("test-user").setIssuer("http://localhost").signWith(
                            SignatureAlgorithm.HS512, key).compact();
            final String slug = pid;
            client.newLdpDcWithAuth(baseUri, slug, baseUri, token);
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Test
    void testPutWithFalseResponse() throws LdpClientException {
        try {
            final IRI falseIdentifier = rdf.createIRI("http://a.fictitious.org");
            final Boolean res = client.putWithResponse(falseIdentifier, getRevisedTestResource(), contentTypeTurtle);
            assertEquals(res, false);
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Test
    void testPutWithMetadata() throws LdpClientException {
        try {
            final IRI identifier = rdf.createIRI(baseUrl + pid);
            final Map<String, String> metadata = new HashMap<>();
            metadata.put(CONTENT_TYPE, contentTypeTurtle);
            metadata.put("Etag", "053036f0a8a95b3ecf4fee30b9c3145f");
            client.put(identifier, getTestResource(), contentTypeTurtle);
            client.putWithMetadata(identifier, getRevisedTestResource(), metadata);
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Test
    void testPutIfMatch() throws LdpClientException {
        try {
            final IRI identifier = rdf.createIRI(baseUrl + pid);
            final String etag = "053036f0a8a95b3ecf4fee30b9c3145f";
            client.put(identifier, getTestResource(), contentTypeTurtle);
            client.putIfMatch(identifier, getRevisedTestResource(), contentTypeTurtle, etag);
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Test
    void testPutBinarywithDigest() throws LdpClientException {
        try {
            final IRI identifier = rdf.createIRI(baseUrl + pid);
            final String digest = "md5=1VOyRwUXW1CPdC5nelt7GQ==";
            client.putBinaryWithDigest(identifier, getTestBinary(), contentTypeTextPlain, digest);
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Test
    void testPutIfUnmodified() throws LdpClientException {
        try {
            final IRI identifier = rdf.createIRI(baseUrl + pid);
            final String time = "Wed, 19 Oct 2016 10:15:00 GMT";
            client.putIfUnmodified(identifier, getTestBinary(), contentTypeTextPlain, time);
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Test
    void testDelete() throws LdpClientException {
        try {
            final IRI identifier = rdf.createIRI(baseUrl + pid);
            client.put(identifier, getTestBinary(), contentTypeTextPlain);
            client.delete(identifier);
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Test
    void testPatch() throws LdpClientException {
        try {
            final IRI identifier = rdf.createIRI(baseUrl + pid);
            client.put(identifier, getTestResource(), contentTypeTurtle);
            client.patch(identifier, getSparqlUpdate());
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }
}
