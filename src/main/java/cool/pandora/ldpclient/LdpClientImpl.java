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

import static java.time.Instant.ofEpochMilli;
import static java.time.ZoneOffset.UTC;
import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;
import static java.util.Collections.synchronizedList;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static jdk.incubator.http.HttpClient.Version.HTTP_2;
import static jdk.incubator.http.HttpResponse.BodyHandler.asByteArray;
import static jdk.incubator.http.HttpResponse.BodyHandler.asFile;
import static jdk.incubator.http.HttpResponse.BodyHandler.asString;
import static org.apache.jena.riot.WebContent.contentTypeTurtle;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.net.ssl.SSLContext;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import jdk.incubator.http.HttpClient;
import jdk.incubator.http.HttpRequest;
import jdk.incubator.http.HttpResponse;
import org.apache.commons.rdf.api.IRI;
import org.apache.jena.riot.WebContent;
import org.slf4j.Logger;
import org.trellisldp.vocabulary.DC;
import org.trellisldp.vocabulary.LDP;
import org.trellisldp.vocabulary.Trellis;

/**
 * LdpClientImpl.
 *
 * @author christopher-johnson
 */
public class LdpClientImpl implements LdpClient {
    private static final Logger log = getLogger(LdpClientImpl.class);
    private static final String NON_NULL_IDENTIFIER = "Identifier may not be null!";
    private static SSLContext sslContext;
    private static HttpClient client = null;

    private LdpClientImpl(final HttpClient client) {
        requireNonNull(client, "HTTP client may not be null!");
        LdpClientImpl.client = client;
    }

    /**
     *
     */
    public LdpClientImpl() {
        this(getClient());
    }

    private static HttpClient getClient() {
        final ExecutorService exec = Executors.newCachedThreadPool();
        return HttpClient.newBuilder().executor(exec)
                // .sslContext(sslContext)
                .version(HTTP_2).build();
    }

    private static String buildLDFQuery(final String subject, final String predicate,
                                        final String object) {
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
    public Map<String, List<String>> head(final IRI identifier) throws URISyntaxException,
            IOException,
            InterruptedException {
        requireNonNull(identifier, NON_NULL_IDENTIFIER);
        final HttpRequest req = HttpRequest.newBuilder(new URI(identifier.getIRIString())).method(
                "HEAD", HttpRequest.noBody()).build();
        final HttpResponse<String> response = client.send(req, asString());
        log.info(
                String.valueOf(response.version()) + " HEAD request to {} returned {}", identifier,
                String.valueOf(response.statusCode()));
        return response.headers().map();
    }

    @Override
    public String getJson(final IRI identifier) throws URISyntaxException, IOException,
            InterruptedException {
        requireNonNull(identifier, NON_NULL_IDENTIFIER);
        final HttpRequest req = HttpRequest.newBuilder(new URI(identifier.getIRIString()))
                .headers(HttpHeaders.ACCEPT, WebContent.contentTypeJSONLD).GET().build();
        final HttpResponse<String> response = client.send(req, asString());
        log.info(
                String.valueOf(response.version()) + " GET request to {} returned {}", identifier,
                String.valueOf(response.statusCode()));
        return response.body();
    }

    @Override
    public String getDefaultType(final IRI identifier) throws URISyntaxException, IOException,
            InterruptedException {
        requireNonNull(identifier, NON_NULL_IDENTIFIER);
        final HttpRequest req = HttpRequest.newBuilder(
                new URI(identifier.getIRIString())).GET().build();
        final HttpResponse<String> response = client.send(req, asString());
        log.info(
                String.valueOf(response.version()) + " GET request to {} returned {}", identifier,
                String.valueOf(response.statusCode()));
        return response.body();
    }

    @Override
    public String getWithContentType(final IRI identifier, final String contentType) throws
            URISyntaxException, IOException, InterruptedException {
        requireNonNull(identifier, NON_NULL_IDENTIFIER);
        final HttpRequest req = HttpRequest.newBuilder(new URI(identifier.getIRIString()))
                .headers(HttpHeaders.ACCEPT, contentType).GET().build();
        final HttpResponse<String> response = client.send(req, asString());
        log.info(
                String.valueOf(response.version()) + " GET request to {} returned {}", identifier,
                String.valueOf(response.statusCode()));
        return response.body();
    }

    @Override
    public Map<String, List<String>> getAcceptDatetime(final IRI identifier, final String timestamp)
            throws
            URISyntaxException, IOException, InterruptedException {
        requireNonNull(identifier, NON_NULL_IDENTIFIER);
        final String datetime = RFC_1123_DATE_TIME.withZone(UTC).format(ofEpochMilli(
                Long.parseLong(timestamp)));
        final HttpRequest req = HttpRequest.newBuilder(new URI(identifier.getIRIString()))
                .headers("Accept-Datetime", datetime).GET().build();
        final HttpResponse<String> response = client.send(req, asString());
        log.info(
                String.valueOf(response.version()) + " GET request to {} returned {}", identifier,
                String.valueOf(response.statusCode()));
        return response.headers().map();
    }

    @Override
    public String getTimeMapLinkDefaultFormat(final IRI identifier) throws URISyntaxException,
            IOException, InterruptedException {
        requireNonNull(identifier, NON_NULL_IDENTIFIER);
        final HttpRequest req = HttpRequest.newBuilder(
                new URI(identifier.getIRIString() + "?ext=timemap")).GET().build();
        final HttpResponse<String> response = client.send(req, asString());
        log.info(
                String.valueOf(response.version()) + " GET request to {} returned {}", identifier,
                String.valueOf(response.statusCode()));
        return response.body();
    }

    @Override
    public String getTimeMapJsonProfile(final IRI identifier, final String profile) throws
            URISyntaxException, IOException, InterruptedException {
        requireNonNull(identifier, NON_NULL_IDENTIFIER);
        final HttpRequest req = HttpRequest.newBuilder(
                new URI(identifier.getIRIString() + "?ext=timemap"))
                .headers(
                        HttpHeaders.ACCEPT, WebContent.contentTypeJSONLD + "; profile=\"" + profile
                                + "\"").GET().build();
        final HttpResponse<String> response = client.send(req, asString());
        log.info(
                String.valueOf(response.version()) + " GET request to {} returned {}",
                identifier + "?ext=timemap",
                String.valueOf(response.statusCode()));
        return response.body();
    }

    @Override
    public String getVersionJson(final IRI identifier, final String profile, final String
            timestamp) throws
            URISyntaxException, IOException, InterruptedException {
        requireNonNull(identifier, NON_NULL_IDENTIFIER);
        final HttpRequest req = HttpRequest.newBuilder(
                new URI(identifier.getIRIString() + "?version=" + timestamp))
                .headers(
                        HttpHeaders.ACCEPT, WebContent.contentTypeJSONLD + "; profile=\"" + profile
                                + "\"").GET().build();
        final HttpResponse<String> response = client.send(req, asString());
        log.info(
                String.valueOf(response.version()) + " GET request to {} returned {}",
                identifier + "?version=" + timestamp,
                String.valueOf(response.statusCode()));
        return response.body();
    }

    @Override
    public Path getBinary(final IRI identifier, final Path file) throws URISyntaxException,
            IOException,
            InterruptedException {
        requireNonNull(identifier, NON_NULL_IDENTIFIER);
        final HttpRequest req = HttpRequest.newBuilder(
                new URI(identifier.getIRIString())).GET().build();
        final HttpResponse<Path> response = client.send(req, asFile(file));
        log.info(
                String.valueOf(response.version()) + " GET request to {} returned {}", identifier,
                String.valueOf(response.statusCode()));
        return response.body();
    }

    @Override
    public byte[] getBinary(final IRI identifier) throws URISyntaxException, IOException,
            InterruptedException {
        requireNonNull(identifier, NON_NULL_IDENTIFIER);
        final HttpRequest req = HttpRequest.newBuilder(
                new URI(identifier.getIRIString())).GET().build();
        final HttpResponse<byte[]> response = client.send(req, asByteArray());
        log.info(
                String.valueOf(response.version()) + " GET request to {} returned {}", identifier,
                String.valueOf(response.statusCode()));
        return response.body();
    }

    @Override
    public String getBinaryDigest(final IRI identifier, final String algorithm)
            throws
            URISyntaxException, IOException, InterruptedException {
        requireNonNull(identifier, NON_NULL_IDENTIFIER);
        final HttpRequest req = HttpRequest.newBuilder(new URI(identifier.getIRIString()))
                .headers("Want-Digest", algorithm).method(
                        "HEAD", HttpRequest.noBody()).build();
        final HttpResponse<String> response = client.send(req, asString());
        log.info(
                String.valueOf(response.version()) + " GET request to {} returned {}", identifier,
                String.valueOf(response.statusCode()));
        return String.valueOf(response.headers().map().get("Digest"));
    }

    @Override
    public Path getBinaryVersion(final IRI identifier, final Path file, final String timestamp)
            throws
            URISyntaxException, IOException, InterruptedException {
        requireNonNull(identifier, NON_NULL_IDENTIFIER);
        final HttpRequest req = HttpRequest.newBuilder(
                new URI(identifier.getIRIString() + "?version=" + timestamp)).GET().build();
        final HttpResponse<Path> response = client.send(req, asFile(file));
        log.info(
                String.valueOf(response.version()) + " GET request to {} returned {}",
                identifier + "?version=" + timestamp,
                String.valueOf(response.statusCode()));
        return response.body();
    }

    @Override
    public byte[] getBinaryVersion(final IRI identifier, final String timestamp)
            throws
            URISyntaxException, IOException, InterruptedException {
        requireNonNull(identifier, NON_NULL_IDENTIFIER);
        final HttpRequest req = HttpRequest.newBuilder(
                new URI(identifier.getIRIString() + "?version=" + timestamp)).GET().build();
        final HttpResponse<byte[]> response = client.send(req, asByteArray());
        log.info(
                String.valueOf(response.version()) + " GET request to {} returned {}",
                identifier + "?version=" + timestamp,
                String.valueOf(response.statusCode()));
        return response.body();
    }

    @Override
    public byte[] getRange(final IRI identifier, final Path file, final String byterange) throws
            URISyntaxException, IOException, InterruptedException {
        requireNonNull(identifier, NON_NULL_IDENTIFIER);
        final HttpRequest req = HttpRequest.newBuilder(new URI(identifier.getIRIString()))
                .headers("Range", byterange).GET().build();
        final HttpResponse<byte[]> response = client.send(req, asByteArray());
        log.info(String.valueOf(response.version()));
        log.info(String.valueOf(response.statusCode()));
        return response.body();
    }

    @Override
    public String getPrefer(final IRI identifier, final String prefer) throws URISyntaxException,
            IOException, InterruptedException {
        requireNonNull(identifier, NON_NULL_IDENTIFIER);
        final HttpRequest req = HttpRequest.newBuilder(new URI(identifier.getIRIString()))
                .headers("Prefer", prefer).GET().build();
        final HttpResponse<String> response = client.send(req, asString());
        log.info(
                String.valueOf(response.version()) + " GET request to {} returned {}", identifier,
                String.valueOf(response.statusCode()));
        return response.body();
    }

    @Override
    public String getPreferServerManaged(final IRI identifier) throws URISyntaxException,
            IOException,
            InterruptedException {
        requireNonNull(identifier, NON_NULL_IDENTIFIER);
        final HttpRequest req = HttpRequest.newBuilder(new URI(identifier.getIRIString()))
                .headers(
                        "Prefer", "return=representation; include=\""
                                + Trellis.PreferServerManaged.getIRIString() + "\"").GET().build();
        final HttpResponse<String> response = client.send(req, asString());
        log.info(
                String.valueOf(response.version()) + " GET request to {} returned {}", identifier,
                String.valueOf(response.statusCode()));
        return response.body();
    }

    @Override
    public String getPreferMinimal(final IRI identifier) throws URISyntaxException, IOException,
            InterruptedException {
        requireNonNull(identifier, NON_NULL_IDENTIFIER);
        final HttpRequest req = HttpRequest.newBuilder(new URI(identifier.getIRIString()))
                .headers(
                        "Prefer", "return=representation; include=\""
                                + LDP.PreferMinimalContainer.getIRIString() + "\"").GET().build();
        final HttpResponse<String> response = client.send(req, asString());
        log.info(
                String.valueOf(response.version()) + " GET request to {} returned {}", identifier,
                String.valueOf(response.statusCode()));
        return response.body();
    }

    @Override
    public String getJsonProfile(final IRI identifier, final String profile) throws
            URISyntaxException,
            IOException, InterruptedException {
        requireNonNull(identifier, NON_NULL_IDENTIFIER);
        final HttpRequest req = HttpRequest.newBuilder(new URI(identifier.getIRIString()))
                .headers(
                        HttpHeaders.ACCEPT, WebContent.contentTypeJSONLD + "; profile=\"" + profile
                                + "\"").GET().build();
        final HttpResponse<String> response = client.send(req, asString());
        log.info(
                String.valueOf(response.version()) + " GET request to {} returned {}", identifier,
                String.valueOf(response.statusCode()));
        return response.body();
    }

    @Override
    public String getJsonProfileLDF(final IRI identifier, final String profile, final String
            subject, final String predicate, final String object) throws URISyntaxException,
            IOException, InterruptedException {
        requireNonNull(identifier, NON_NULL_IDENTIFIER);
        final String q = buildLDFQuery(subject, predicate, object);
        final URI uri = new URI(identifier.getIRIString() + q);
        final HttpRequest req = HttpRequest.newBuilder(uri).headers(
                HttpHeaders.ACCEPT,
                WebContent.contentTypeJSONLD + "; profile=\"" + profile + "\"").GET().build();
        final HttpResponse<String> response = client.send(req, asString());
        log.info(
                String.valueOf(response.version()) + " GET request to {} returned {}", identifier,
                String.valueOf(response.statusCode()));
        return response.body();
    }

    @Override
    public String getJsonLDF(final IRI identifier, final String subject, final String predicate,
                             final String object) throws URISyntaxException, IOException,
            InterruptedException {
        requireNonNull(identifier, NON_NULL_IDENTIFIER);
        final String q = buildLDFQuery(subject, predicate, object);
        final URI uri = new URI(identifier.getIRIString() + q);
        final HttpRequest req = HttpRequest.newBuilder(uri)
                .headers(HttpHeaders.ACCEPT, WebContent.contentTypeJSONLD).GET().build();
        final HttpResponse<String> response = client.send(req, asString());
        log.info(
                String.valueOf(response.version()) + " GET request to {} returned {}",
                identifier + q,
                String.valueOf(response.statusCode()));
        return response.body();
    }

    @Override
    public String getAcl(final IRI identifier, final Map<String, String> metadata) throws
            URISyntaxException, IOException,
            InterruptedException {
        requireNonNull(identifier, NON_NULL_IDENTIFIER);
        final String authorization = ofNullable(metadata.get(HttpHeaders.AUTHORIZATION)).orElse("");
        final HttpRequest req = HttpRequest.newBuilder(
                new URI(identifier.getIRIString() + "?ext=acl")).header(
                HttpHeaders.AUTHORIZATION, authorization).GET().build();
        final HttpResponse<String> response = client.send(req, asString());
        log.info(
                String.valueOf(response.version()) + " GET request to {} returned {}",
                identifier + "?ext=acl",
                String.valueOf(response.statusCode()));
        return response.body();
    }

    @Override
    public Map<String, List<String>> getCORS(final IRI identifier, final IRI origin) throws
            URISyntaxException, IOException,
            InterruptedException {
        requireNonNull(identifier, NON_NULL_IDENTIFIER);
        final HttpRequest req = HttpRequest.newBuilder(new URI(identifier.getIRIString()))
                .headers("Origin", origin.getIRIString(),
                        "Access-Control-Request-Method", "PUT",
                        "Access-Control-Request-Headers", "Content-Type, Link").GET().build();
        final HttpResponse<String> response = client.send(req, asString());
        log.info(
                String.valueOf(response.version()) + " GET request to {} returned {}", identifier,
                String.valueOf(response.statusCode()));
        return response.headers().map();
    }

    @Override
    public Map<String, List<String>> getCORSSimple(final IRI identifier, final IRI origin) throws
            URISyntaxException, IOException,
            InterruptedException {
        requireNonNull(identifier, NON_NULL_IDENTIFIER);
        final HttpRequest req = HttpRequest.newBuilder(new URI(identifier.getIRIString()))
                .headers("Origin", origin.getIRIString(),
                        "Access-Control-Request-Method", "POST",
                        "Access-Control-Request-Headers", "Accept").GET().build();
        final HttpResponse<String> response = client.send(req, asString());
        log.info(
                String.valueOf(response.version()) + " GET request to {} returned {}", identifier,
                String.valueOf(response.statusCode()));
        return response.headers().map();
    }

    @Override
    public String getWithMetadata(final IRI identifier, final Map<String, String> metadata) throws
            URISyntaxException, IOException, InterruptedException {
        requireNonNull(identifier, NON_NULL_IDENTIFIER);
        final String[] entries = buildHeaderEntryList(metadata);
        final HttpRequest req = HttpRequest.newBuilder(new URI(identifier.getIRIString()))
                .headers(entries).GET().build();
        final HttpResponse<String> response = client.send(req, asString());
        log.info(
                String.valueOf(response.version()) + " GET request to {} returned {}",
                identifier,
                String.valueOf(response.statusCode()));
        return response.body();
    }

    @Override
    public byte[] getBytesWithMetadata(final IRI identifier, final Map<String, String> metadata) throws
            URISyntaxException, IOException, InterruptedException {
        requireNonNull(identifier, NON_NULL_IDENTIFIER);
        final String[] entries = buildHeaderEntryList(metadata);
        final HttpRequest req = HttpRequest.newBuilder(new URI(identifier.getIRIString()))
                .headers(entries).GET().build();
        final HttpResponse<byte[]> response = client.send(req, asByteArray());
        log.info(
                String.valueOf(response.version()) + " GET request to {} returned {}",
                identifier,
                String.valueOf(response.statusCode()));
        return response.body();
    }

    @Override
    public Map<String, List<String>> options(final IRI identifier) throws URISyntaxException,
            IOException, InterruptedException {
        requireNonNull(identifier, NON_NULL_IDENTIFIER);
        final HttpRequest req = HttpRequest.newBuilder(new URI(identifier.getIRIString())).method(
                "OPTIONS", HttpRequest.noBody()).build();
        final HttpResponse<String> response = client.send(req, asString());
        log.info(
                String.valueOf(response.version()) + " OPTIONS request to {} returned {}",
                identifier,
                String.valueOf(response.statusCode()));
        return response.headers().map();
    }

    @Override
    public void post(final IRI identifier, final InputStream stream, final Map<String, String>
            metadata) throws URISyntaxException, IOException, InterruptedException {
        requireNonNull(identifier, NON_NULL_IDENTIFIER);
        final MediaType contentType = ofNullable(metadata.get(HttpHeaders.CONTENT_TYPE)).map(
                MediaType::valueOf)
                .orElse(MediaType.APPLICATION_OCTET_STREAM_TYPE);
        final HttpRequest req = HttpRequest.newBuilder(new URI(identifier.getIRIString()))
                .headers(HttpHeaders.CONTENT_TYPE, contentType.toString())
                .POST(HttpRequest.BodyProcessor.fromInputStream(() -> stream)).build();
        final HttpResponse<String> response = client.send(req, asString());
        log.info(
                "New Resource Location {}",
                String.valueOf(response.headers().map().get("Location")));
        log.info(
                String.valueOf(response.version()) + " POST request to {} returned {}", identifier,
                String.valueOf(response.statusCode()));
    }

    @Override
    public void postWithMetadata(final IRI identifier, final InputStream stream, final Map<String, String>
            metadata) throws URISyntaxException, IOException, InterruptedException {
        requireNonNull(identifier, NON_NULL_IDENTIFIER);
        final String[] entries = buildHeaderEntryList(metadata);
        final HttpRequest req = HttpRequest.newBuilder(new URI(identifier.getIRIString()))
                .headers(entries)
                .POST(HttpRequest.BodyProcessor.fromInputStream(() -> stream)).build();
        final HttpResponse<String> response = client.send(req, asString());
        log.info(
                "New Resource Location {}",
                String.valueOf(response.headers().map().get("Location")));
        log.info(
                String.valueOf(response.version()) + " POST request to {} returned {}", identifier,
                String.valueOf(response.statusCode()));
    }

    @Override
    public void postWithAuth(final IRI identifier, final InputStream stream, final Map<String,
            String>
            metadata) throws URISyntaxException, IOException, InterruptedException {
        requireNonNull(identifier, NON_NULL_IDENTIFIER);
        final MediaType contentType = ofNullable(metadata.get(HttpHeaders.CONTENT_TYPE)).map(
                MediaType::valueOf)
                .orElse(MediaType.APPLICATION_OCTET_STREAM_TYPE);
        final String authorization = ofNullable(metadata.get(HttpHeaders.AUTHORIZATION)).orElse(
                "");
        final HttpRequest req = HttpRequest.newBuilder(new URI(identifier.getIRIString()))
                .headers(HttpHeaders.CONTENT_TYPE, contentType.toString(),
                        HttpHeaders.AUTHORIZATION,
                        authorization)
                .POST(HttpRequest.BodyProcessor.fromInputStream(() -> stream)).build();
        final HttpResponse<String> response = client.send(req, asString());
        log.info(
                "New Resource Location {}",
                String.valueOf(response.headers().map().get("Location")));
        log.info(
                String.valueOf(response.version()) + " AUTHORIZED POST request to {} returned {}", identifier,
                String.valueOf(response.statusCode()));
    }

    @Override
    public void postSlug(final IRI identifier, final String slug, final InputStream stream, final
    Map<String, String> metadata) throws URISyntaxException, IOException, InterruptedException {
        requireNonNull(identifier, NON_NULL_IDENTIFIER);
        final MediaType contentType = ofNullable(metadata.get(HttpHeaders.CONTENT_TYPE)).map(
                MediaType::valueOf)
                .orElse(MediaType.APPLICATION_OCTET_STREAM_TYPE);
        final HttpRequest req = HttpRequest.newBuilder(new URI(identifier.getIRIString()))
                .headers(HttpHeaders.CONTENT_TYPE, contentType.toString(), "Slug", slug)
                .POST(HttpRequest.BodyProcessor.fromInputStream(() -> stream)).build();
        final HttpResponse<String> response = client.send(req, asString());
        log.info(
                "New Resource Location {}",
                String.valueOf(response.headers().map().get("Location")));
        log.info(
                String.valueOf(response.version()) + " POST request to {} returned {}", identifier,
                String.valueOf(response.statusCode()));
    }

    @Override
    public void postBinaryWithDigest(final IRI identifier, final InputStream stream, final
    Map<String, String> metadata, final
                                     String digest) throws URISyntaxException, IOException,
            InterruptedException {
        requireNonNull(identifier, NON_NULL_IDENTIFIER);
        final MediaType contentType = ofNullable(metadata.get(HttpHeaders.CONTENT_TYPE)).map(
                MediaType::valueOf)
                .orElse(MediaType.APPLICATION_OCTET_STREAM_TYPE);
        final HttpRequest req = HttpRequest.newBuilder(new URI(identifier.getIRIString()))
                .headers(HttpHeaders.CONTENT_TYPE, contentType.toString(), "Digest", digest)
                .POST(HttpRequest.BodyProcessor.fromInputStream(() -> stream)).build();
        final HttpResponse<String> response = client.send(req, asString());
        log.info(
                "New Resource Location {}",
                String.valueOf(response.headers().map().get("Location")));
        log.info(
                String.valueOf(response.version()) + " POST request to {} returned {}", identifier,
                String.valueOf(response.statusCode()));
    }

    @Override
    public void newLdpDc(final IRI identifier, final String slug, final IRI membershipObj) throws
            IOException,
            InterruptedException,
            URISyntaxException {
        requireNonNull(identifier, NON_NULL_IDENTIFIER);
        final URI parentURI = new URI(identifier.getIRIString());
        final String entity = "<> " + LDP.hasMemberRelation + " " + DC.isPartOf + " ;\n"
                + LDP.membershipResource + " " + membershipObj;
        final HttpRequest req = HttpRequest.newBuilder(parentURI)
                .headers(HttpHeaders.CONTENT_TYPE, contentTypeTurtle, "Slug", slug,
                        HttpHeaders.LINK, LDP.DirectContainer + "; rel=\"type\"")
                .POST(HttpRequest.BodyProcessor.fromString(entity)).build();
        final HttpResponse<String> response = client.send(req, asString());
        log.info(
                String.valueOf(response.version())
                        + " POST create LDP-DC request to {} returned {}", parentURI,
                String.valueOf(response.statusCode()));
    }

    @Override
    public void put(final IRI identifier, final InputStream stream,
                    final Map<String, String> metadata) throws URISyntaxException, IOException,
            InterruptedException {
        requireNonNull(identifier, NON_NULL_IDENTIFIER);
        final MediaType contentType = ofNullable(metadata.get(HttpHeaders.CONTENT_TYPE)).map(
                MediaType::valueOf)
                .orElse(MediaType.APPLICATION_OCTET_STREAM_TYPE);
        final HttpRequest req = HttpRequest.newBuilder(new URI(identifier.getIRIString()))
                .headers(
                        HttpHeaders.CONTENT_TYPE, contentType.toString())
                .PUT(HttpRequest.BodyProcessor.fromInputStream(() -> stream)).build();
        final HttpResponse<String> response = client.send(req, asString());
        log.info(
                String.valueOf(response.version()) + " PUT request to {} returned {}", identifier,
                String.valueOf(response.statusCode()));
    }

    @Override
    public void putWithMetadata(final IRI identifier, final InputStream stream,
                    final Map<String, String> metadata) throws URISyntaxException, IOException,
            InterruptedException {
        requireNonNull(identifier, NON_NULL_IDENTIFIER);
        final String[] entries = buildHeaderEntryList(metadata);
        final HttpRequest req = HttpRequest.newBuilder(new URI(identifier.getIRIString()))
                .headers(entries)
                .PUT(HttpRequest.BodyProcessor.fromInputStream(() -> stream)).build();
        final HttpResponse<String> response = client.send(req, asString());
        log.info(
                String.valueOf(response.version()) + " PUT request to {} returned {}", identifier,
                String.valueOf(response.statusCode()));
    }

    @Override
    public void putWithAuth(final IRI identifier, final InputStream stream,
                            final Map<String, String> metadata) throws URISyntaxException,
            IOException,
            InterruptedException {
        requireNonNull(identifier, NON_NULL_IDENTIFIER);
        final MediaType contentType = ofNullable(metadata.get(HttpHeaders.CONTENT_TYPE)).map(
                MediaType::valueOf)
                .orElse(MediaType.APPLICATION_OCTET_STREAM_TYPE);
        final String authorization = ofNullable(metadata.get(HttpHeaders.AUTHORIZATION)).orElse(
                "");
        final HttpRequest req = HttpRequest.newBuilder(new URI(identifier.getIRIString()))
                .headers(
                        HttpHeaders.CONTENT_TYPE, contentType.toString(), HttpHeaders.AUTHORIZATION,
                        authorization)
                .PUT(HttpRequest.BodyProcessor.fromInputStream(() -> stream)).build();
        final HttpResponse<String> response = client.send(req, asString());
        log.info(
                String.valueOf(response.version()) + " AUTHORIZED PUT request to {} returned {}",
                identifier,
                String.valueOf(response.statusCode()));
    }

    @Override
    public void putIfMatch(final IRI identifier, final InputStream stream, final Map<String,
            String> metadata, final String etag) throws URISyntaxException,
            IOException,
            InterruptedException {
        requireNonNull(identifier, NON_NULL_IDENTIFIER);
        final MediaType contentType = ofNullable(metadata.get(HttpHeaders.CONTENT_TYPE)).map(
                MediaType::valueOf)
                .orElse(MediaType.APPLICATION_OCTET_STREAM_TYPE);
        final HttpRequest req = HttpRequest.newBuilder(new URI(identifier.getIRIString()))
                .headers(
                        HttpHeaders.CONTENT_TYPE, contentType.toString(), HttpHeaders.ETAG,
                        etag)
                .PUT(HttpRequest.BodyProcessor.fromInputStream(() -> stream)).build();
        final HttpResponse<String> response = client.send(req, asString());
        log.info(
                String.valueOf(response.version())
                        + " PUT request with matching Etag {} to {} returned {}", etag, identifier,
                String.valueOf(response.statusCode()));
    }

    @Override
    public void putBinaryWithDigest(final IRI identifier, final InputStream stream, final
    Map<String, String> metadata, final String digest) throws URISyntaxException, IOException,
            InterruptedException {
        requireNonNull(identifier, NON_NULL_IDENTIFIER);
        final MediaType contentType = ofNullable(metadata.get(HttpHeaders.CONTENT_TYPE)).map(
                MediaType::valueOf)
                .orElse(MediaType.APPLICATION_OCTET_STREAM_TYPE);
        final HttpRequest req = HttpRequest.newBuilder(new URI(identifier.getIRIString()))
                .headers(HttpHeaders.CONTENT_TYPE, contentType.toString(), "Digest", digest)
                .PUT(HttpRequest.BodyProcessor.fromInputStream(() -> stream)).build();
        final HttpResponse<String> response = client.send(req, asString());
        log.info(
                String.valueOf(response.version()) + " PUT request to {} returned {}", identifier,
                String.valueOf(response.statusCode()));
    }

    @Override
    public void putIfUnmodified(final IRI identifier, final InputStream stream, final
    Map<String, String> metadata, final String time) throws URISyntaxException, IOException,
            InterruptedException {
        requireNonNull(identifier, NON_NULL_IDENTIFIER);
        final MediaType contentType = ofNullable(metadata.get(HttpHeaders.CONTENT_TYPE)).map(
                MediaType::valueOf)
                .orElse(MediaType.APPLICATION_OCTET_STREAM_TYPE);
        final HttpRequest req = HttpRequest.newBuilder(new URI(identifier.getIRIString()))
                .headers(
                        HttpHeaders.CONTENT_TYPE, contentType.toString(), "If-Unmodified-Since",
                        time)
                .PUT(HttpRequest.BodyProcessor.fromInputStream(() -> stream)).build();
        final HttpResponse<String> response = client.send(req, asString());
        log.info(
                String.valueOf(response.version()) + " PUT request to {} returned {}", identifier,
                String.valueOf(response.statusCode()));
    }

    @Override
    public void delete(final IRI identifier) throws URISyntaxException, IOException,
            InterruptedException {
        requireNonNull(identifier, NON_NULL_IDENTIFIER);
        final HttpRequest req = HttpRequest.newBuilder(new URI(identifier.getIRIString())).DELETE(
                HttpRequest.noBody()).build();
        final HttpResponse<String> response = client.send(req, asString());
        log.info(
                String.valueOf(response.version()) + " DELETE request to {} returned {}",
                identifier,
                String.valueOf(response.statusCode()));
    }

    @Override
    public void patch(final IRI identifier, final InputStream stream) throws URISyntaxException,
            IOException,
            InterruptedException {
        final HttpRequest req = HttpRequest.newBuilder(new URI(identifier.getIRIString())).method(
                "PATCH", HttpRequest.BodyProcessor.fromInputStream(() -> stream)).build();
        final HttpResponse<String> response = client.send(req, asString());
        log.info(
                String.valueOf(response.version()) + " PATCH request to {} returned {}", identifier,
                String.valueOf(response.statusCode()));
    }

    @Override
    public void multipartOptions(final IRI identifier) {
        requireNonNull(identifier, NON_NULL_IDENTIFIER);
    }

    @Override
    public void multipartStart(final IRI identifier, final InputStream stream) {
        requireNonNull(identifier, NON_NULL_IDENTIFIER);
    }

    @Override
    public void multipartGet(final IRI identifier, final String sessionId) {
        requireNonNull(identifier, NON_NULL_IDENTIFIER);
    }

    @Override
    public void multipartPut(final IRI identifier, final InputStream stream, final String
            sessionId) {
        requireNonNull(identifier, NON_NULL_IDENTIFIER);
    }

    @Override
    public void multipartPost(final IRI identifier, final InputStream stream, final String
            sessionId) {
        requireNonNull(identifier, NON_NULL_IDENTIFIER);
    }

    @Override
    public void multipartDelete(final IRI identifier, final String sessionId) {
        requireNonNull(identifier, NON_NULL_IDENTIFIER);
    }

    /**
     * @param is
     * @param toURI
     */
    public void asyncPut(final String is, final String toURI)
            throws ExecutionException, InterruptedException, URISyntaxException, IOException {
        final HttpRequest req =
                HttpRequest.newBuilder(new URI(toURI)).headers(
                        "Content-Type", WebContent.contentTypeNTriples)
                        .PUT(HttpRequest.BodyProcessor.fromString(is)).build();
        final CompletableFuture<HttpResponse<String>> response = client.sendAsync(req, asString());
    }

    /**
     * @param query
     * @param accept
     * @param optimized
     * @return
     */
    public byte[] syncGetQuery(final String query, final String accept, final boolean optimized)
            throws InterruptedException, URISyntaxException, IOException {
        final HttpRequest req = HttpRequest.newBuilder(new URI(query))
                .headers(
                        "Content-Type", WebContent.contentTypeSPARQLQuery, "Accept",
                        accept).GET().build();
        final HttpResponse<byte[]> response = client.send(req, asByteArray());

        log.info(String.valueOf(response.version()));
        log.info(String.valueOf(response.statusCode()));
        return response.body();
    }

    /**
     * @param query
     * @param accept
     * @return
     */
    public String syncGetQuery(final String query, final String accept)
            throws ExecutionException, InterruptedException, URISyntaxException, IOException {
        final HttpRequest req = HttpRequest.newBuilder(new URI(query))
                .headers(
                        "Content-Type", WebContent.contentTypeSPARQLQuery, "Accept",
                        accept).GET().build();
        final HttpResponse<String> response = client.send(req, asString());

        log.info(String.valueOf(response.version()));
        log.info(String.valueOf(response.statusCode()));
        return response.body();
    }

    /**
     * @param query
     * @param accept
     * @param optimized
     * @return
     */
    public byte[] asyncGetQuery(final String query, final String accept, final boolean optimized)
            throws ExecutionException, InterruptedException, URISyntaxException, IOException {
        final HttpRequest req = HttpRequest.newBuilder().uri(new URI(query))
                .headers(
                        "Content-Type", WebContent.contentTypeSPARQLQuery, "Accept",
                        accept).GET().build();
        final CompletableFuture<HttpResponse<byte[]>> response = client.sendAsync(
                req, asByteArray());
        log.info(String.valueOf(response.get().version()));
        log.info(String.valueOf(response.get().statusCode()));
        return response.get().body();
    }

    /**
     * @param query
     * @param accept
     * @return
     */
    public String asyncGetQuery(final String query, final String accept)
            throws ExecutionException, InterruptedException, URISyntaxException, IOException {
        final HttpRequest req = HttpRequest.newBuilder().uri(new URI(query))
                .headers(
                        "Content-Type", WebContent.contentTypeSPARQLQuery, "Accept",
                        accept).GET().build();
        final CompletableFuture<HttpResponse<String>> response = client.sendAsync(req, asString());
        log.info(String.valueOf(response.get().version()));
        log.info(String.valueOf(response.get().statusCode()));
        return response.get().body();
    }

    /**
     * @param query
     * @return
     */
    public String syncUpdate(final String query)
            throws ExecutionException, InterruptedException, URISyntaxException, IOException {
        final String formdata = "update=" + query;
        final HttpRequest req =
                HttpRequest.newBuilder(new URI("http://localhost:3030/fuseki/annotations"))
                        .headers("Content-Type", WebContent.contentTypeSPARQLQuery)
                        .POST(HttpRequest.BodyProcessor.fromString(formdata)).build();
        final HttpResponse<String> response = client.send(req, asString());
        log.info(String.valueOf(response.version()));
        log.info(String.valueOf(response.statusCode()));
        return response.body();
    }
}
