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

import static java.net.http.HttpClient.Version.HTTP_2;
import static java.net.http.HttpRequest.BodyPublishers.noBody;
import static java.net.http.HttpRequest.BodyPublishers.ofInputStream;
import static java.net.http.HttpRequest.BodyPublishers.ofString;
import static java.net.http.HttpResponse.BodyHandlers.discarding;
import static java.net.http.HttpResponse.BodyHandlers.ofByteArray;
import static java.net.http.HttpResponse.BodyHandlers.ofFile;
import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static java.time.Instant.ofEpochMilli;
import static java.time.ZoneOffset.UTC;
import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;
import static java.util.Collections.synchronizedList;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static javax.ws.rs.core.HttpHeaders.ACCEPT;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.HttpHeaders.ETAG;
import static javax.ws.rs.core.HttpHeaders.LINK;
import static org.apache.jena.arq.riot.WebContent.contentTypeJSONLD;
import static org.apache.jena.arq.riot.WebContent.contentTypeNTriples;
import static org.apache.jena.arq.riot.WebContent.contentTypeSPARQLUpdate;
import static org.apache.jena.arq.riot.WebContent.contentTypeTurtle;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.net.ssl.SSLContext;

import org.apache.commons.rdf.api.IRI;
import org.slf4j.Logger;
import org.trellisldp.vocabulary.DC;
import org.trellisldp.vocabulary.LDP;


/**
 * LdpClientImpl.
 *
 * @author christopher-johnson
 */
public class LdpClientImpl implements LdpClient {

    private static final Logger log = getLogger(LdpClientImpl.class);
    private static final String NON_NULL_IDENTIFIER = "Identifier may not be null!";
    private static HttpClient client = null;

    private LdpClientImpl(final HttpClient client) {
        requireNonNull(client, "HTTP client may not be null!");
        LdpClientImpl.client = client;
    }

    /**
     * LdpClientImpl.
     */
    public LdpClientImpl() {
        this(getClient());
    }

    /**
     * LdpClientImpl.
     *
     * @param sslContext an {@link SSLContext}
     */
    public LdpClientImpl(final SSLContext sslContext) {
        this(getH2Client(sslContext));
    }

    static HttpClient getClient() {
        final ExecutorService exec = Executors.newCachedThreadPool();
        return HttpClient.newBuilder().executor(exec).build();
    }

    static HttpClient getH2Client(final SSLContext sslContext) {
        final ExecutorService exec = Executors.newCachedThreadPool();
        return HttpClient.newBuilder().executor(exec).sslContext(sslContext).version(HTTP_2).build();
    }

    static String buildLDFQuery(final String subject, final String predicate, final String object) {
        String sq = "";
        String pq = "";
        String oq = "";

        if (nonNull(subject)) {
            sq = "subject=" + subject;
        }

        if (nonNull(predicate) && nonNull(subject)) {
            pq = "&predicate=" + predicate;
        } else if (nonNull(predicate)) {
            pq = "predicate=" + predicate;
        }

        if (nonNull(predicate) && nonNull(object) || (nonNull(subject) && nonNull(object))) {
            oq = "&object=" + object;
        } else if (nonNull(object)) {
            oq = "object=" + object;
        }

        return "?" + sq + pq + oq;
    }

    private synchronized String[] buildHeaderEntryList(final Map<String, String> metadata) {
        final List<String> h = synchronizedList(new ArrayList<>());
        metadata.forEach((key, value) -> {
            h.add(key);
            h.add(value);
        });
        return h.toArray(new String[h.size()]);
    }

    @Override
    public Map<String, List<String>> head(final IRI identifier) throws LdpClientException {
        try {
            requireNonNull(identifier, NON_NULL_IDENTIFIER);
            final URI uri = new URI(identifier.getIRIString());
            final HttpRequest req = HttpRequest.newBuilder(uri).method("HEAD", noBody()).build();
            final HttpResponse<String> response = client.send(req, ofString());
            log.info(String.valueOf(response.version()) + " HEAD request to {} returned {}", identifier,
                    String.valueOf(response.statusCode()));
            return response.headers().map();
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Override
    public String getJson(final IRI identifier) throws LdpClientException {
        try {
            requireNonNull(identifier, NON_NULL_IDENTIFIER);
            final URI uri = new URI(identifier.getIRIString());
            final String[] headers = new String[]{ACCEPT, contentTypeJSONLD};
            final HttpRequest req = HttpRequest.newBuilder(uri).headers(headers).GET().build();
            final HttpResponse<String> response = client.send(req, ofString());
            log.info(String.valueOf(response.version()) + " GET request to {} returned {}", identifier,
                    String.valueOf(response.statusCode()));
            log.debug("Response Body: " + response.body());
            return response.body();
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Override
    public String getDefaultType(final IRI identifier) throws LdpClientException {
        try {
            requireNonNull(identifier, NON_NULL_IDENTIFIER);
            final URI uri = new URI(identifier.getIRIString());
            final HttpRequest req = HttpRequest.newBuilder(uri).GET().build();
            final HttpResponse<String> response = client.send(req, ofString());
            log.info(String.valueOf(response.version()) + " GET request to {} returned {}", identifier,
                    String.valueOf(response.statusCode()));
            log.debug("Response Body: " + response.body());
            return response.body();
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Override
    public HttpResponse getResponse(final IRI identifier) throws LdpClientException {
        try {
            requireNonNull(identifier, NON_NULL_IDENTIFIER);
            final URI uri = new URI(identifier.getIRIString());
            final HttpRequest req = HttpRequest.newBuilder(uri).GET().build();
            return client.send(req, ofString());
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Override
    public String getWithContentType(final IRI identifier, final String contentType) throws LdpClientException {
        try {
            requireNonNull(identifier, NON_NULL_IDENTIFIER);
            final URI uri = new URI(identifier.getIRIString());
            final HttpRequest req = HttpRequest.newBuilder(uri).headers(ACCEPT, contentType).GET().build();
            final HttpResponse<String> response = client.send(req, ofString());
            log.info(String.valueOf(response.version()) + " GET request to {} returned {}", identifier,
                    String.valueOf(response.statusCode()));
            log.debug("Response Body: " + response.body());
            return response.body();
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Override
    public Map<String, List<String>> getAcceptDatetime(final IRI identifier, final String timestamp) throws
            LdpClientException {
        try {
            requireNonNull(identifier, NON_NULL_IDENTIFIER);
            final URI uri = new URI(identifier.getIRIString());
            final String datetime = RFC_1123_DATE_TIME.withZone(UTC).format(ofEpochMilli(Long.parseLong(timestamp)));
            final HttpRequest req = HttpRequest.newBuilder(uri).headers("Accept-Datetime", datetime).GET().build();
            final HttpResponse<String> response = client.send(req, ofString());
            log.info(String.valueOf(response.version()) + " GET request to {} returned {}", identifier,
                    String.valueOf(response.statusCode()));
            return response.headers().map();
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Override
    public String getTimeMapLinkDefaultFormat(final IRI identifier) throws LdpClientException {
        try {
            requireNonNull(identifier, NON_NULL_IDENTIFIER);
            final URI uri = new URI(identifier.getIRIString() + "?ext=timemap");
            final HttpRequest req = HttpRequest.newBuilder(uri).GET().build();
            final HttpResponse<String> response = client.send(req, ofString());
            log.info(String.valueOf(response.version()) + " GET request to {} returned {}", identifier,
                    String.valueOf(response.statusCode()));
            log.debug("Response Body: " + response.body());
            return response.body();
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Override
    public String getTimeMapJsonProfile(final IRI identifier, final String profile) throws LdpClientException {
        try {
            requireNonNull(identifier, NON_NULL_IDENTIFIER);
            final URI uri = new URI(identifier.getIRIString() + "?ext=timemap");
            final String[] headers = new String[]{ACCEPT, contentTypeJSONLD + "; " + "profile=\"" + profile + "\""};
            final HttpRequest req = HttpRequest.newBuilder(uri).headers(headers).GET().build();
            final HttpResponse<String> response = client.send(req, ofString());
            log.info(String.valueOf(response.version()) + " GET request to {} returned {}",
                    identifier.getIRIString() + "?ext=timemap", String.valueOf(response.statusCode()));
            log.debug("Response Body: " + response.body());
            return response.body();
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Override
    public String getVersionJson(final IRI identifier, final String profile, final String timestamp) throws
            LdpClientException {
        try {
            requireNonNull(identifier, NON_NULL_IDENTIFIER);
            final URI uri = new URI(identifier.getIRIString() + "?version=" + timestamp);
            final String[] headers = new String[]{ACCEPT, contentTypeJSONLD + "; " + "profile=\"" + profile + "\""};
            final HttpRequest req = HttpRequest.newBuilder(uri).headers(headers).GET().build();
            final HttpResponse<String> response = client.send(req, ofString());
            log.info(String.valueOf(response.version()) + " GET request to {} returned {}",
                    identifier.getIRIString() + "?version=" + timestamp, String.valueOf(response.statusCode()));
            log.debug("Response Body: " + response.body());
            return response.body();
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Override
    public Path getBinary(final IRI identifier, final Path file) throws LdpClientException {
        try {
            requireNonNull(identifier, NON_NULL_IDENTIFIER);
            final URI uri = new URI(identifier.getIRIString());
            final HttpRequest req = HttpRequest.newBuilder(uri).GET().build();
            final HttpResponse<Path> response = client.send(req, ofFile(file));
            log.info(String.valueOf(response.version()) + " GET request to {} returned {}", identifier,
                    String.valueOf(response.statusCode()));
            return response.body();
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Override
    public byte[] getBinary(final IRI identifier) throws LdpClientException {
        try {
            requireNonNull(identifier, NON_NULL_IDENTIFIER);
            final URI uri = new URI(identifier.getIRIString());
            final HttpRequest req = HttpRequest.newBuilder(uri).GET().build();
            final HttpResponse<byte[]> response = client.send(req, ofByteArray());
            log.info(String.valueOf(response.version()) + " GET request to {} returned {}", identifier,
                    String.valueOf(response.statusCode()));
            log.debug("Response Body: " + new String(response.body(), StandardCharsets.UTF_8));
            return response.body();
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Override
    public String getBinaryDigest(final IRI identifier, final String algorithm) throws LdpClientException {
        try {
            requireNonNull(identifier, NON_NULL_IDENTIFIER);
            final URI uri = new URI(identifier.getIRIString());
            final HttpRequest req = HttpRequest.newBuilder(uri).headers("Want-Digest", algorithm).method("HEAD",
                    noBody()).build();
            final HttpResponse<String> response = client.send(req, ofString());
            log.info(String.valueOf(response.version()) + " GET request to {} returned {}", identifier,
                    String.valueOf(response.statusCode()));
            final List<List<String>> res = response.headers().map().entrySet().stream().filter(
                    h -> h.getKey().equals("digest")).map(Map.Entry::getValue).collect(Collectors.toList());
            return res.stream().flatMap(List::stream).collect(Collectors.toList()).stream().findAny().orElse("");
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Override
    public Path getBinaryVersion(final IRI identifier, final Path file, final String timestamp) throws
            LdpClientException {
        try {
            requireNonNull(identifier, NON_NULL_IDENTIFIER);
            final URI uri = new URI(identifier.getIRIString() + "?version=" + timestamp);
            final HttpRequest req = HttpRequest.newBuilder(uri).GET().build();
            final HttpResponse<Path> response = client.send(req, ofFile(file));
            log.info(String.valueOf(response.version()) + " GET request to {} returned {}",
                    identifier + "?version=" + timestamp, String.valueOf(response.statusCode()));
            return response.body();
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Override
    public byte[] getBinaryVersion(final IRI identifier, final String timestamp) throws LdpClientException {
        try {
            requireNonNull(identifier, NON_NULL_IDENTIFIER);
            final URI uri = new URI(identifier.getIRIString() + "?version=" + timestamp);
            final HttpRequest req = HttpRequest.newBuilder(uri).GET().build();
            final HttpResponse<byte[]> response = client.send(req, ofByteArray());
            log.info(String.valueOf(response.version()) + " GET request to {} returned {}",
                    identifier.getIRIString() + "?version=" + timestamp, String.valueOf(response.statusCode()));
            log.debug("Response Body: " + new String(response.body(), StandardCharsets.UTF_8));
            return response.body();
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Override
    public byte[] getRange(final IRI identifier, final String byterange) throws LdpClientException {
        try {
            requireNonNull(identifier, NON_NULL_IDENTIFIER);
            final URI uri = new URI(identifier.getIRIString());
            final HttpRequest req = HttpRequest.newBuilder(uri).headers("Range", byterange).GET().build();
            final HttpResponse<byte[]> response = client.send(req, ofByteArray());
            log.info(String.valueOf(response.version()) + " GET request to {} returned {}", identifier.getIRIString(),
                    String.valueOf(response.statusCode()));
            return response.body();
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Override
    public String getPrefer(final IRI identifier, final String prefer) throws LdpClientException {
        try {
            requireNonNull(identifier, NON_NULL_IDENTIFIER);
            final URI uri = new URI(identifier.getIRIString());
            final HttpRequest req = HttpRequest.newBuilder(uri).headers("Prefer", prefer).GET().build();
            final HttpResponse<String> response = client.send(req, ofString());
            log.info(String.valueOf(response.version()) + " GET request to {} returned {}", identifier,
                    String.valueOf(response.statusCode()));
            log.debug("Response Body: " + response.body());
            return response.body();
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Override
    public String getPreferMinimal(final IRI identifier) throws LdpClientException {
        try {
            requireNonNull(identifier, NON_NULL_IDENTIFIER);
            final URI uri = new URI(identifier.getIRIString());
            final String[] headers = new String[]{"Prefer", "return=representation; include=\"" + LDP
                    .PreferMinimalContainer.getIRIString() + "\""};
            final HttpRequest req = HttpRequest.newBuilder(uri).headers(headers).GET().build();
            final HttpResponse<String> response = client.send(req, ofString());
            log.info(String.valueOf(response.version()) + " GET request to {} returned {}", identifier,
                    String.valueOf(response.statusCode()));
            log.debug("Response Body: " + response.body());
            return response.body();
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Override
    public String getJsonProfile(final IRI identifier, final String profile) throws LdpClientException {
        try {
            requireNonNull(identifier, NON_NULL_IDENTIFIER);
            final URI uri = new URI(identifier.getIRIString());
            final String[] headers = new String[]{ACCEPT, contentTypeJSONLD + "; " + "profile=\"" + profile + "\""};
            final HttpRequest req = HttpRequest.newBuilder(uri).headers(headers).GET().build();
            final HttpResponse<String> response = client.send(req, ofString());
            log.info(String.valueOf(response.version()) + " GET request to {} returned {}", identifier,
                    String.valueOf(response.statusCode()));
            log.debug("Response Body: " + response.body());
            return response.body();
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Override
    public String getJsonProfileLDF(final IRI identifier, final String profile, final String subject, final String
            predicate, final String object) throws LdpClientException {
        try {
            requireNonNull(identifier, NON_NULL_IDENTIFIER);
            final String q = buildLDFQuery(subject, predicate, object);
            final URI uri = new URI(identifier.getIRIString() + q);
            final String[] headers = new String[]{ACCEPT, contentTypeJSONLD + "; profile=\"" + profile + "\""};
            final HttpRequest req = HttpRequest.newBuilder(uri).headers(headers).GET().build();
            final HttpResponse<String> response = client.send(req, ofString());
            log.info(String.valueOf(response.version()) + " GET request to {} returned {}", identifier,
                    String.valueOf(response.statusCode()));
            log.debug("Response Body: " + response.body());
            return response.body();
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Override
    public String getJsonLDF(final IRI identifier, final String subject, final String predicate, final String object)
            throws LdpClientException {
        try {
            requireNonNull(identifier, NON_NULL_IDENTIFIER);
            final String q = buildLDFQuery(subject, predicate, object);
            final URI uri = new URI(identifier.getIRIString() + q);
            final HttpRequest req = HttpRequest.newBuilder(uri).headers(ACCEPT, contentTypeJSONLD).GET().build();
            final HttpResponse<String> response = client.send(req, ofString());
            log.info(String.valueOf(response.version()) + " GET request to {} returned {}", identifier + q,
                    String.valueOf(response.statusCode()));
            log.debug("Response Body: " + response.body());
            return response.body();
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }

    }

    @Override
    public String getAcl(final IRI identifier, final String contentType) throws LdpClientException {
        try {
            requireNonNull(identifier, NON_NULL_IDENTIFIER);
            final URI uri = new URI(identifier.getIRIString() + "?ext=acl");
            final HttpRequest req = HttpRequest.newBuilder(uri).headers(ACCEPT, contentType).GET().build();
            final HttpResponse<String> response = client.send(req, ofString());
            log.info(String.valueOf(response.version()) + " GET request to {} returned {}", identifier + "?ext=acl",
                    String.valueOf(response.statusCode()));
            log.debug("Response Body: " + response.body());
            return response.body();
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Override
    public Map<String, List<String>> getCORS(final IRI identifier, final IRI origin) throws LdpClientException {
        try {
            requireNonNull(identifier, NON_NULL_IDENTIFIER);
            final URI uri = new URI(identifier.getIRIString());
            final String[] headers = new String[]{"Origin", origin.getIRIString(), "Access-Control-Request-Method",
                    "PUT", "Access-Control-Request-Headers", "Content-Type, Link"};
            final HttpRequest req = HttpRequest.newBuilder(uri).headers(headers).GET().build();
            final HttpResponse<String> response = client.send(req, ofString());
            log.info(String.valueOf(response.version()) + " GET request to {} returned {}", identifier,
                    String.valueOf(response.statusCode()));
            return response.headers().map();
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Override
    public Map<String, List<String>> getCORSSimple(final IRI identifier, final IRI origin) throws LdpClientException {
        try {
            requireNonNull(identifier, NON_NULL_IDENTIFIER);
            final URI uri = new URI(identifier.getIRIString());
            final String[] headers = new String[]{"Origin", origin.getIRIString(), "Access-Control-Request-Method",
                    "POST", "Access-Control-Request-Headers", "Accept"};
            final HttpRequest req = HttpRequest.newBuilder(uri).headers(headers).GET().build();
            final HttpResponse<String> response = client.send(req, ofString());
            log.info(String.valueOf(response.version()) + " GET request to {} returned {}", identifier,
                    String.valueOf(response.statusCode()));
            return response.headers().map();
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Override
    public String getWithMetadata(final IRI identifier, final Map<String, String> metadata) throws LdpClientException {
        try {
            requireNonNull(identifier, NON_NULL_IDENTIFIER);
            final URI uri = new URI(identifier.getIRIString());
            final String[] entries = buildHeaderEntryList(metadata);
            final HttpRequest req = HttpRequest.newBuilder(uri).headers(entries).GET().build();
            final HttpResponse<String> response = client.send(req, ofString());
            log.info(String.valueOf(response.version()) + " GET request to {} returned {}", identifier,
                    String.valueOf(response.statusCode()));
            log.debug("Response Body: " + response.body());
            return response.body();
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Override
    public byte[] getBytesWithMetadata(final IRI identifier, final Map<String, String> metadata) throws
            LdpClientException {
        try {
            requireNonNull(identifier, NON_NULL_IDENTIFIER);
            final URI uri = new URI(identifier.getIRIString());
            final String[] entries = buildHeaderEntryList(metadata);
            final HttpRequest req = HttpRequest.newBuilder(uri).headers(entries).GET().build();
            final HttpResponse<byte[]> response = client.send(req, ofByteArray());
            log.info(String.valueOf(response.version()) + " GET request to {} returned {}", identifier,
                    String.valueOf(response.statusCode()));
            return response.body();
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Override
    public Map<String, Map<String, List<String>>> getResponseWithHeaders(final IRI identifier, final Map<String,
            String> metadata) throws LdpClientException {
        try {
            requireNonNull(identifier, NON_NULL_IDENTIFIER);
            final URI uri = new URI(identifier.getIRIString());
            final String[] entries = buildHeaderEntryList(metadata);
            final HttpRequest req = HttpRequest.newBuilder(uri).headers(entries).GET().build();
            final HttpResponse<String> response = client.send(req, ofString());
            log.info(String.valueOf(response.version()) + " GET request to {} returned {}", identifier,
                    String.valueOf(response.statusCode()));
            log.debug("Response Body: " + response.body());
            final Map<String, Map<String, List<String>>> res = new HashMap<>();
            res.put(response.body(), response.headers().map());
            return res;
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Override
    public Map<String, List<String>> options(final IRI identifier) throws LdpClientException {
        try {
            requireNonNull(identifier, NON_NULL_IDENTIFIER);
            final URI uri = new URI(identifier.getIRIString());
            final HttpRequest req = HttpRequest.newBuilder(uri).method("OPTIONS", noBody()).build();
            final HttpResponse<String> response = client.send(req, ofString());
            log.info(String.valueOf(response.version()) + " OPTIONS request to {} returned {}", identifier,
                    String.valueOf(response.statusCode()));
            return response.headers().map();
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    public void initUpgrade(final IRI identifier) throws LdpClientException {
        try {
        requireNonNull(identifier, NON_NULL_IDENTIFIER);
        final URI uri = new URI(identifier.getIRIString());
        final HttpRequest req = HttpRequest.newBuilder(uri).method("OPTIONS", noBody()).build();
        client.send(req, discarding());
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Override
    public void post(final IRI identifier, final InputStream stream, final String contentType) throws
            LdpClientException {
        try {
            requireNonNull(identifier, NON_NULL_IDENTIFIER);
            final URI uri = new URI(identifier.getIRIString());
            final HttpRequest req = HttpRequest.newBuilder(uri).headers(CONTENT_TYPE, contentType).POST(
                    ofInputStream(() -> stream)).build();
            final HttpResponse<String> response = client.send(req, ofString());
            log.info("New Resource Location {}", String.valueOf(response.headers().map().get("Location")));
            log.info(String.valueOf(response.version()) + " POST request to {} returned {}", identifier,
                    String.valueOf(response.statusCode()));
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Override
    public void postWithMetadata(final IRI identifier, final InputStream stream, final Map<String, String> metadata)
            throws LdpClientException {
        try {
            requireNonNull(identifier, NON_NULL_IDENTIFIER);
            final URI uri = new URI(identifier.getIRIString());
            final String[] entries = buildHeaderEntryList(metadata);
            final HttpRequest req = HttpRequest.newBuilder(uri).headers(entries).POST(
                    ofInputStream(() -> stream)).build();
            final HttpResponse<String> response = client.send(req, ofString());
            log.info("New Resource Location {}", String.valueOf(response.headers().map().get("Location")));
            log.info(String.valueOf(response.version()) + " POST request to {} returned {}", identifier,
                    String.valueOf(response.statusCode()));
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Override
    public void postWithAuth(final IRI identifier, final InputStream stream, final String contentType, final String
            authorization) throws LdpClientException {
        try {
            requireNonNull(identifier, NON_NULL_IDENTIFIER);
            final URI uri = new URI(identifier.getIRIString());
            final String[] headers = new String[]{CONTENT_TYPE, contentType, AUTHORIZATION, authorization};
            final HttpRequest req = HttpRequest.newBuilder(uri).headers(headers).POST(
                    ofInputStream(() -> stream)).build();
            final HttpResponse<String> response = client.send(req, ofString());
            log.info("New Resource Location {}", String.valueOf(response.headers().map().get("Location")));
            log.info(String.valueOf(response.version()) + " AUTHORIZED POST request to {} returned {}", identifier,
                    String.valueOf(response.statusCode()));
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Override
    public void postSlug(final IRI identifier, final String slug, final InputStream stream, final String contentType)
            throws LdpClientException {
        try {
            requireNonNull(identifier, NON_NULL_IDENTIFIER);
            final URI uri = new URI(identifier.getIRIString());
            final HttpRequest req = HttpRequest.newBuilder(uri).headers(CONTENT_TYPE, contentType, "Slug", slug).POST(
                    ofInputStream(() -> stream)).build();
            final HttpResponse<String> response = client.send(req, ofString());
            log.info("New Resource Location {}", String.valueOf(response.headers().map().get("Location")));
            log.info(String.valueOf(response.version()) + " POST request to {} returned {}", identifier,
                    String.valueOf(response.statusCode()));
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Override
    public void postBinaryWithDigest(final IRI identifier, final InputStream stream, final String contentType, final
    String digest) throws LdpClientException {
        try {
            requireNonNull(identifier, NON_NULL_IDENTIFIER);
            final URI uri = new URI(identifier.getIRIString());
            final HttpRequest req = HttpRequest.newBuilder(uri).headers(
                    CONTENT_TYPE, contentType, "Digest", digest).POST(ofInputStream(() -> stream)).build();
            final HttpResponse<String> response = client.send(req, ofString());
            log.info("New Resource Location {}", String.valueOf(response.headers().map().get("Location")));
            log.info(String.valueOf(response.version()) + " POST request to {} returned {}", identifier,
                    String.valueOf(response.statusCode()));
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Override
    public void createBasicContainer(final IRI identifier) throws LdpClientException {
        try {
            requireNonNull(identifier, NON_NULL_IDENTIFIER);
            final URI uri = new URI(identifier.getIRIString());
            final String[] headers = new String[]{LINK, LDP.BasicContainer + "; rel=\"type\""};
            final HttpRequest req = HttpRequest.newBuilder(uri).headers(headers).PUT(ofString("")).build();
            final HttpResponse<String> response = client.send(req, ofString());
            log.info(String.valueOf(response.version()) + " PUT create LDP-BC request to {} returned {}", uri,
                    String.valueOf(response.statusCode()));
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Override
    public void createDirectContainer(final IRI identifier, final String slug, final IRI membershipObj) throws
            LdpClientException {
        try {
            requireNonNull(identifier, NON_NULL_IDENTIFIER);
            final URI uri = new URI(identifier.getIRIString());
            final String[] headers = new String[]{CONTENT_TYPE, contentTypeTurtle, "Slug", slug, LINK, LDP
                    .DirectContainer + "; rel=\"type\""};
            final String entity = "<> " + LDP.hasMemberRelation + " " + DC.isPartOf + " ;\n" + LDP.membershipResource
                    + " " + membershipObj;
            final HttpRequest req = HttpRequest.newBuilder(uri).headers(headers).POST(ofString(entity)).build();
            final HttpResponse<String> response = client.send(req, ofString());
            log.info(String.valueOf(response.version()) + " POST create LDP-DC request to {} returned {}", uri,
                    String.valueOf(response.statusCode()));
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Override
    public void createDirectContainerWithAuth(final IRI identifier, final String slug, final IRI membershipObj, final
    String authorization) throws LdpClientException {
        try {
            requireNonNull(identifier, NON_NULL_IDENTIFIER);
            final URI uri = new URI(identifier.getIRIString());
            final String[] headers = new String[]{CONTENT_TYPE, contentTypeTurtle, "Slug", slug, LINK, LDP
                    .DirectContainer + "; rel=\"type\"", AUTHORIZATION, authorization};
            final String entity = "<> " + LDP.hasMemberRelation + " " + DC.isPartOf + " ;\n" + LDP.membershipResource
                    + " " + membershipObj;
            final HttpRequest req = HttpRequest.newBuilder(uri).headers(headers).POST(ofString(entity)).build();
            final HttpResponse<String> response = client.send(req, ofString());
            log.info(String.valueOf(response.version()) + " AUTHORIZED POST create LDP-DC request to {} returned {}",
                    uri, String.valueOf(response.statusCode()));
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Override
    public void put(final IRI identifier, final InputStream stream, final String contentType) throws
            LdpClientException {
        try {
            requireNonNull(identifier, NON_NULL_IDENTIFIER);
            final URI uri = new URI(identifier.getIRIString());
            final HttpRequest req = HttpRequest.newBuilder(uri).headers(CONTENT_TYPE, contentType).PUT(
                    ofInputStream(() -> stream)).build();
            final HttpResponse<String> response = client.send(req, ofString());
            log.info(String.valueOf(response.version()) + " PUT request to {} returned {}", identifier,
                    String.valueOf(response.statusCode()));
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Override
    public void putSupplier(final IRI identifier, Supplier<FileInputStream> fileInputStreamSupplier, final String
            contentType) throws LdpClientException {
        try {
            requireNonNull(identifier, NON_NULL_IDENTIFIER);
            final URI uri = new URI(identifier.getIRIString());
            final HttpRequest req = HttpRequest.newBuilder(uri).headers(CONTENT_TYPE, contentType).PUT(
                    ofInputStream(fileInputStreamSupplier)).build();
            final HttpResponse<String> response = client.send(req, ofString());
            log.info(String.valueOf(response.version()) + " PUT request to {} returned {}", identifier,
                    String.valueOf(response.statusCode()));
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Override
    public Boolean putWithResponse(final IRI identifier, final InputStream stream, final String contentType) throws
            LdpClientException {
        try {
            requireNonNull(identifier, NON_NULL_IDENTIFIER);
            final URI uri = new URI(identifier.getIRIString());
            final HttpRequest req = HttpRequest.newBuilder(uri).headers(CONTENT_TYPE, contentType).PUT(
                    ofInputStream(() -> stream)).build();
            final HttpResponse<String> response = client.send(req, ofString());
            log.info(String.valueOf(response.version()) + " PUT request to {} returned {}", identifier,
                    String.valueOf(response.statusCode()));
            return response.statusCode() == 204 || response.statusCode() == 201;
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Override
    public void putWithMetadata(final IRI identifier, final InputStream stream, final Map<String, String> metadata)
            throws LdpClientException {
        try {
            requireNonNull(identifier, NON_NULL_IDENTIFIER);
            final URI uri = new URI(identifier.getIRIString());
            final String[] entries = buildHeaderEntryList(metadata);
            final HttpRequest req = HttpRequest.newBuilder(uri).headers(entries).PUT(
                    ofInputStream(() -> stream)).build();
            final HttpResponse<String> response = client.send(req, ofString());
            log.info(String.valueOf(response.version()) + " PUT request to {} returned {}", identifier,
                    String.valueOf(response.statusCode()));
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Override
    public void putWithAuth(final IRI identifier, final InputStream stream, final String contentType, final String
            authorization) throws LdpClientException {
        try {
            requireNonNull(identifier, NON_NULL_IDENTIFIER);
            final URI uri = new URI(identifier.getIRIString());
            final HttpRequest req = HttpRequest.newBuilder(uri).headers(CONTENT_TYPE, contentType, AUTHORIZATION,
                    authorization).PUT(ofInputStream(() -> stream)).build();
            final HttpResponse<String> response = client.send(req, ofString());
            log.info(String.valueOf(response.version()) + " AUTHORIZED PUT request to {} returned {}", identifier,
                    String.valueOf(response.statusCode()));
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Override
    public void putIfMatch(final IRI identifier, final InputStream stream, final String contentType, final String
            etag) throws LdpClientException {
        try {
            requireNonNull(identifier, NON_NULL_IDENTIFIER);
            final URI uri = new URI(identifier.getIRIString());
            final HttpRequest req = HttpRequest.newBuilder(uri).headers(CONTENT_TYPE, contentType, ETAG, etag).PUT(
                    ofInputStream(() -> stream)).build();
            final HttpResponse<String> response = client.send(req, ofString());
            log.info(String.valueOf(response.version()) + " PUT request with matching Etag {} to {} returned {}", etag,
                    identifier, String.valueOf(response.statusCode()));
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Override
    public void putBinaryWithDigest(final IRI identifier, final InputStream stream, final String contentType, final
    String digest) throws LdpClientException {
        try {
            requireNonNull(identifier, NON_NULL_IDENTIFIER);
            final URI uri = new URI(identifier.getIRIString());
            final HttpRequest req = HttpRequest.newBuilder(uri).headers(
                    CONTENT_TYPE, contentType, "Digest", digest).PUT(ofInputStream(() -> stream)).build();
            final HttpResponse<String> response = client.send(req, ofString());
            log.info(String.valueOf(response.version()) + " PUT request to {} returned {}", identifier,
                    String.valueOf(response.statusCode()));
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Override
    public void putIfUnmodified(final IRI identifier, final InputStream stream, final String contentType, final
    String time) throws LdpClientException {
        try {
            requireNonNull(identifier, NON_NULL_IDENTIFIER);
            final URI uri = new URI(identifier.getIRIString());
            final HttpRequest req = HttpRequest.newBuilder(uri).headers(CONTENT_TYPE, contentType,
                    "If-Unmodified-Since", time).PUT(ofInputStream(() -> stream)).build();
            final HttpResponse<String> response = client.send(req, ofString());
            log.info(String.valueOf(response.version()) + " PUT request to {} returned {}", identifier,
                    String.valueOf(response.statusCode()));
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Override
    public void delete(final IRI identifier) throws LdpClientException {
        try {
            requireNonNull(identifier, NON_NULL_IDENTIFIER);
            final URI uri = new URI(identifier.getIRIString());
            final HttpRequest req = HttpRequest.newBuilder(uri).DELETE().build();
            final HttpResponse<String> response = client.send(req, ofString());
            log.info(String.valueOf(response.version()) + " DELETE request to {} returned {}", identifier,
                    String.valueOf(response.statusCode()));
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Override
    public void patch(final IRI identifier, final InputStream stream) throws LdpClientException {
        try {
            requireNonNull(identifier, NON_NULL_IDENTIFIER);
            final URI uri = new URI(identifier.getIRIString());
            final HttpRequest req = HttpRequest.newBuilder(uri).header(CONTENT_TYPE, contentTypeSPARQLUpdate).method(
                    "PATCH", ofInputStream(() -> stream)).build();
            final HttpResponse<String> response = client.send(req, ofString());
            log.info(String.valueOf(response.version()) + " PATCH request to {} returned {}", identifier,
                    String.valueOf(response.statusCode()));
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Override
    public Boolean asyncPut(final IRI identifier, final InputStream stream) throws LdpClientException {
        try {
            requireNonNull(identifier, NON_NULL_IDENTIFIER);
            final URI uri = new URI(identifier.getIRIString());
            final HttpRequest req = HttpRequest.newBuilder(uri).headers(CONTENT_TYPE, contentTypeNTriples).PUT(
                    ofInputStream(() -> stream)).build();
            final CompletableFuture<HttpResponse<String>> response = client.sendAsync(req, ofString());
            final Integer code = response.get().statusCode();
            return code == 204 || code == 201;

        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Override
    public void joiningCompletableFuturePut(final Map<URI, InputStream> bodies, final String contentType) {
        CompletableFuture.allOf(bodies.entrySet().stream().map(k -> client.sendAsync(
                HttpRequest.newBuilder(k.getKey()).headers(CONTENT_TYPE, contentType).PUT(
                        ofInputStream(k::getValue)).build(), ofString()).thenApply(HttpResponse::statusCode)).toArray(
                CompletableFuture<?>[]::new)).join();
    }
}
