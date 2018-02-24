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

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.apache.commons.rdf.api.IRI;

/**
 * LdpClient.
 *
 * @author christopher-johnson
 */
public interface LdpClient {

    /**
     * @param identifier a resource identifier
     * @return Map of headers
     */
    Map<String, List<String>> head(final IRI identifier) throws URISyntaxException, IOException,
            InterruptedException;

    /**
     * @param identifier a resource identifier
     * @return body as a {@link String}
     */
    String getJson(final IRI identifier) throws URISyntaxException, IOException,
            InterruptedException;

    /**
     * @param identifier a resource identifier
     * @return body as a {@link String}
     */
    String getDefaultType(final IRI identifier) throws URISyntaxException, IOException,
            InterruptedException;

    /**
     * @param identifier a resource identifier
     * @param contentType a content type (text/turtle, application/n-triples or application/ld+json)
     * @return body as a {@link String}
     */
    String getWithContentType(final IRI identifier, String contentType) throws
            URISyntaxException, IOException, InterruptedException;

    /**
     * @param identifier a resource identifier
     * @param timestamp an epoch millisecond
     * @return headers as a {@link Map}
     */
    Map<String, List<String>> getAcceptDatetime(final IRI identifier, String timestamp) throws
            URISyntaxException, IOException, InterruptedException;

    /**
     * @param identifier a resource identifier
     * @return body as a {@link String}
     */
    String getTimeMapLinkDefaultFormat(IRI identifier) throws URISyntaxException,
            IOException, InterruptedException;

    /**
     * @param identifier a resource identifier
     * @param profile a JSON-LD profile
     * @return body as a {@link String}
     * @see <a href="https://www.w3.org/ns/json-ld">The JSON-LD Vocabulary</a>
     */
    String getTimeMapJsonProfile(IRI identifier, String profile) throws
            URISyntaxException, IOException, InterruptedException;

    /**
     * @param identifier a resource identifier
     * @param profile a JSON-LD profile
     * @param timestamp an epoch millisecond
     * @return body as a {@link String}
     * @see <a href="https://www.w3.org/ns/json-ld">The JSON-LD Vocabulary</a>
     */
    String getVersionJson(IRI identifier, String profile, String timestamp) throws
            URISyntaxException, IOException, InterruptedException;

    /**
     * @param identifier a resource identifier
     * @param file an output file as an {@link Path}
     * @return body as a {@link Path}
     */
    Path getBinary(IRI identifier, Path file) throws URISyntaxException,
            IOException, InterruptedException;

    /**
     * @param identifier a resource identifier
     * @return body as a byte[]
     */
    byte[] getBinary(IRI identifier) throws URISyntaxException,
            IOException, InterruptedException;

    /**
     * @param identifier a resource identifier
     * @param algorithm a digest algorithm (md5, sha, sha-256 or sha-512)
     * @return digest as a {@link String}
     */
    String getBinaryDigest(IRI identifier, String algorithm) throws
            URISyntaxException, IOException, InterruptedException;

    /**
     * @param identifier a resource identifier
     * @param file an output file as an {@link Path}
     * @param timestamp an epoch millisecond
     * @return body as a  {@link Path}
     */
    Path getBinaryVersion(IRI identifier, Path file, String timestamp) throws
            URISyntaxException, IOException, InterruptedException;

    /**
     * @param identifier a resource identifier
     * @param timestamp an epoch millisecond
     * @return body as a byte[]
     */
    byte[] getBinaryVersion(IRI identifier, String timestamp) throws
            URISyntaxException, IOException, InterruptedException;

    /**
     * @param identifier a resource identifier
     * @param file an output file as an {@link Path}
     * @param byterange
     * @return body as a byte[]
     */
    byte[] getRange(final IRI identifier, Path file, String byterange) throws
            URISyntaxException, IOException, InterruptedException;

    /**
     * @param identifier a resource identifier
     * @param prefer an LDP preference
     * @return body as a {@link String}
     * @see <a href="https://www.w3.org/TR/ldp/#prefer-parameters">7.2 Preferences on the Prefer
     * Request Header</a>
     */
    String getPrefer(final IRI identifier, String prefer) throws URISyntaxException, IOException,
            InterruptedException;

    /**
     * @param identifier a resource identifier
     * @return body as a {@link String}
     */
    String getPreferServerManaged(IRI identifier) throws URISyntaxException,
            IOException, InterruptedException;

    /**
     * @param identifier a resource identifier
     * @return body as a {@link String}
     */
    String getPreferMinimal(IRI identifier) throws URISyntaxException, IOException,
            InterruptedException;

    /**
     * @param identifier a resource identifier
     * @param profile a JSON-LD profile
     * @return body as a {@link String}
     * @see <a href="https://www.w3.org/ns/json-ld">The JSON-LD Vocabulary</a>
     */
    String getJsonProfile(IRI identifier, String profile) throws
            URISyntaxException, IOException, InterruptedException;

    /**
     * @param identifier a resource identifier
     * @param profile a JSON-LD profile
     * @param subject RdfTerm as a {@link String}
     * @param predicate RdfTerm as a {@link String}
     * @param object RdfTerm as a {@link String}
     * @return body as a {@link String}
     * @see <a href="https://www.w3.org/ns/json-ld">The JSON-LD Vocabulary</a>
     */
    String getJsonProfileLDF(IRI identifier, String profile, String subject, String
            predicate, String object) throws URISyntaxException, IOException, InterruptedException;

    /**
     * @param identifier a resource identifier
     * @param subject RdfTerm as a {@link String}
     * @param predicate RdfTerm as a {@link String}
     * @param object RdfTerm as a {@link String}
     * @return body as a {@link String}
     */
    String getJsonLDF(IRI identifier, String subject, String predicate, String object) throws
            URISyntaxException, IOException, InterruptedException;

    /**
     * @param identifier a resource identifier
     * @return body as a {@link String}
     */
    String getAcl(IRI identifier, Map<String, String> metadata) throws URISyntaxException,
            IOException,
            InterruptedException;


    /**
     * @param identifier a resource identifier
     */
    Map<String, List<String>> getCORS(final IRI identifier, final IRI origin) throws
            URISyntaxException, IOException,
            InterruptedException;

    /**
     * @param identifier a resource identifier
     */
    Map<String, List<String>> getCORSSimple(final IRI identifier, final IRI origin) throws
            URISyntaxException, IOException,
            InterruptedException;

    /**
     *
     * @param identifier a resource identifier
     * @param metadata a {@link Map} of headers
     * @return body as a {@link String}
     */
    String getWithMetadata(IRI identifier, Map<String, String> metadata) throws URISyntaxException,
            IOException,
            InterruptedException;

    /**
     *
     * @param identifier a resource identifier
     * @param metadata a {@link Map} of headers
     * @return body as a byte[]
     */
    byte[] getBytesWithMetadata(IRI identifier, Map<String, String> metadata) throws URISyntaxException,
            IOException,
            InterruptedException;

    /**
     * @param identifier a resource identifier
     */
    Map<String, List<String>> options(final IRI identifier) throws URISyntaxException,
            IOException, InterruptedException;

    /**
     * @param identifier a resource identifier
     * @param stream an {@link InputStream}
     * @param metadata a {@link Map} of headers
     */
    void post(final IRI identifier, final InputStream stream, final Map<String, String> metadata)
            throws URISyntaxException, IOException, InterruptedException;

    /**
     * @param identifier a resource identifier
     * @param stream an {@link InputStream}
     * @param metadata a {@link Map} of headers
     */
    void postWithMetadata(final IRI identifier, final InputStream stream, final Map<String, String> metadata)
            throws URISyntaxException, IOException, InterruptedException;

    /**
     * @param identifier a resource identifier
     * @param stream an {@link InputStream}
     * @param metadata a {@link Map} of headers
     */
    void postWithAuth(final IRI identifier, final InputStream stream, final Map<String, String>
            metadata) throws URISyntaxException, IOException, InterruptedException;

    /**
     * @param identifier a resource identifier
     * @param stream an {@link InputStream}
     * @param metadata a {@link Map} of headers
     */
    void postSlug(final IRI identifier, final String slug, final InputStream stream, final
    Map<String, String> metadata) throws URISyntaxException, IOException, InterruptedException;

    /**
     * @param identifier a resource identifier
     * @param stream an {@link InputStream}
     * @param digest a digest as a {@link String}
     * @see <a href="https://tools.ietf.org/html/rfc3230#page-9">rfc3230 4.3.2 Digest</a>
     */
    void postBinaryWithDigest(final IRI identifier, final InputStream stream, final Map<String,
            String> metadata, String digest) throws URISyntaxException, IOException,
            InterruptedException;

    /**
     * @param identifier a resource identifier
     * @param stream an {@link InputStream}
     * @param metadata a {@link Map} of headers
     */
    void put(final IRI identifier, final InputStream stream,
             final Map<String, String> metadata) throws URISyntaxException, IOException,
            InterruptedException;

    /**
     * @param identifier a resource identifier
     * @param stream an {@link InputStream}
     * @param metadata a {@link Map} of headers
     */
    void putWithMetadata(final IRI identifier, final InputStream stream,
             final Map<String, String> metadata) throws URISyntaxException, IOException,
            InterruptedException;

    /**
     * @param identifier a resource identifier
     * @param stream an {@link InputStream}
     * @param metadata a {@link Map} of headers
     */
    void putWithAuth(final IRI identifier, final InputStream stream,
                     final Map<String, String> metadata) throws URISyntaxException, IOException,
            InterruptedException;

    /**
     * @param identifier a resource identifier
     * @param stream an {@link InputStream}
     * @param etag a strong Etag as a {@link String}
     */
    void putIfMatch(final IRI identifier, final InputStream stream, final Map<String, String>
            metadata, String etag) throws URISyntaxException, IOException,
            InterruptedException;

    /**
     * @param identifier a resource identifier
     * @param stream an {@link InputStream}
     * @param digest a digest as a {@link String}
     * @see <a href="https://tools.ietf.org/html/rfc3230#page-9">rfc3230 4.3.2 Digest</a>
     */
    void putBinaryWithDigest(final IRI identifier, final InputStream stream, final Map<String,
            String>
            metadata, String digest) throws URISyntaxException, IOException,
            InterruptedException;

    /**
     * @param identifier a resource identifier
     * @param stream an {@link InputStream}
     * @param time an RFC_1123_DATE_TIME as an {@link String}
     */
    void putIfUnmodified(final IRI identifier, final InputStream stream, final
    Map<String, String> metadata, final String time) throws URISyntaxException, IOException,
            InterruptedException;

    /**
     * @param identifier a resource identifier
     */
    void delete(final IRI identifier) throws URISyntaxException, IOException, InterruptedException;

    /**
     * @param identifier a resource identifier
     * @param stream an {@link InputStream}
     */
    void patch(final IRI identifier, final InputStream stream) throws URISyntaxException,
            IOException,
            InterruptedException;

    /**
     * @param identifier a resource identifier
     */
    void multipartOptions(final IRI identifier);

    /**
     * @param identifier a resource identifier
     * @param stream an {@link InputStream}
     */
    void multipartStart(final IRI identifier, final InputStream stream);

    /**
     * @param identifier a resource identifier
     * @param sessionId
     */
    void multipartGet(final IRI identifier, String sessionId);

    /**
     * @param identifier a resource identifier
     * @param stream an {@link InputStream}
     * @param sessionId
     */
    void multipartPut(final IRI identifier, final InputStream stream, String sessionId);

    /**
     * @param identifier a resource identifier
     * @param stream an {@link InputStream}
     * @param sessionId
     */
    void multipartPost(final IRI identifier, final InputStream stream, String sessionId);

    /**
     * @param identifier a resource identifier
     * @param sessionId
     */
    void multipartDelete(final IRI identifier, String sessionId);

    /**
     * @param identifier a resource identifier
     * @param slug
     * @param membershipObj
     */
    void newLdpDc(final IRI identifier, final String slug, IRI membershipObj) throws IOException,
            InterruptedException, URISyntaxException;

}
