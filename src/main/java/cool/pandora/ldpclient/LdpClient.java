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

import java.io.InputStream;
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
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    Map<String, List<String>> head(final IRI identifier) throws LdpClientException;

    /**
     * @param identifier a resource identifier
     * @return body as a {@link String}
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    String getJson(final IRI identifier) throws LdpClientException;

    /**
     * @param identifier a resource identifier
     * @return body as a {@link String}
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    String getDefaultType(final IRI identifier) throws LdpClientException;

    /**
     * @param identifier a resource identifier
     * @param contentType a content type (text/turtle, application/n-triples or application/ld+json)
     * @return body as a {@link String}
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    String getWithContentType(final IRI identifier, final String contentType) throws LdpClientException;

    /**
     * @param identifier a resource identifier
     * @param timestamp an epoch millisecond
     * @return headers as a {@link Map}
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    Map<String, List<String>> getAcceptDatetime(final IRI identifier, String timestamp) throws LdpClientException;

    /**
     * @param identifier a resource identifier
     * @return body as a {@link String}
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    String getTimeMapLinkDefaultFormat(IRI identifier) throws LdpClientException;

    /**
     * @param identifier a resource identifier
     * @param profile a JSON-LD profile
     * @return body as a {@link String}
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     * @see <a href="https://www.w3.org/ns/json-ld">The JSON-LD Vocabulary</a>
     */
    String getTimeMapJsonProfile(IRI identifier, String profile) throws LdpClientException;

    /**
     * @param identifier a resource identifier
     * @param profile a JSON-LD profile
     * @param timestamp an epoch millisecond
     * @return body as a {@link String}
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     * @see <a href="https://www.w3.org/ns/json-ld">The JSON-LD Vocabulary</a>
     */
    String getVersionJson(IRI identifier, String profile, String timestamp) throws LdpClientException;

    /**
     * @param identifier a resource identifier
     * @param file an output file as an {@link Path}
     * @return body as a {@link Path}
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    Path getBinary(IRI identifier, Path file) throws LdpClientException;

    /**
     * @param identifier a resource identifier
     * @return body as a byte[]
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    byte[] getBinary(IRI identifier) throws LdpClientException;

    /**
     * @param identifier a resource identifier
     * @param algorithm a digest algorithm (md5, sha, sha-256 or sha-512)
     * @return digest as a {@link String}
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    String getBinaryDigest(IRI identifier, String algorithm) throws LdpClientException;

    /**
     * @param identifier a resource identifier
     * @param file an output file as an {@link Path}
     * @param timestamp an epoch millisecond
     * @return body as a  {@link Path}
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    Path getBinaryVersion(IRI identifier, Path file, String timestamp) throws LdpClientException;

    /**
     * @param identifier a resource identifier
     * @param timestamp an epoch millisecond
     * @return body as a byte[]
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    byte[] getBinaryVersion(IRI identifier, String timestamp) throws LdpClientException;

    /**
     * @param identifier a resource identifier
     * @param byterange a byterange
     * @return body as a byte[]
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    byte[] getRange(final IRI identifier, String byterange) throws LdpClientException;

    /**
     * @param identifier a resource identifier
     * @param prefer an LDP preference
     * @return body as a {@link String}
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     * @see <a href="https://www.w3.org/TR/ldp/#prefer-parameters">7.2 Preferences on the Prefer
     * Request Header</a>
     */
    String getPrefer(final IRI identifier, String prefer) throws LdpClientException;

    /**
     * @param identifier a resource identifier
     * @return body as a {@link String}
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    String getPreferServerManaged(IRI identifier) throws LdpClientException;

    /**
     * @param identifier a resource identifier
     * @return body as a {@link String}
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    String getPreferMinimal(IRI identifier) throws LdpClientException;

    /**
     * @param identifier a resource identifier
     * @param profile a JSON-LD profile
     * @return body as a {@link String}
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     * @see <a href="https://www.w3.org/ns/json-ld">The JSON-LD Vocabulary</a>
     */
    String getJsonProfile(IRI identifier, String profile) throws LdpClientException;

    /**
     * @param identifier a resource identifier
     * @param profile a JSON-LD profile
     * @param subject RdfTerm as a {@link String}
     * @param predicate RdfTerm as a {@link String}
     * @param object RdfTerm as a {@link String}
     * @return body as a {@link String}
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     * @see <a href="https://www.w3.org/ns/json-ld">The JSON-LD Vocabulary</a>
     */
    String getJsonProfileLDF(IRI identifier, String profile, String subject, String predicate, String object) throws
            LdpClientException;

    /**
     * @param identifier a resource identifier
     * @param subject RdfTerm as a {@link String}
     * @param predicate RdfTerm as a {@link String}
     * @param object RdfTerm as a {@link String}
     * @return body as a {@link String}
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    String getJsonLDF(IRI identifier, String subject, String predicate, String object) throws LdpClientException;

    /**
     * @param identifier a resource identifier
     * @param contentType a content type as {@link String}
     * @return body as a {@link String}
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    String getAcl(IRI identifier, String contentType) throws LdpClientException;


    /**
     * @param identifier a resource identifier
     * @param origin a root resource identifier
     * @return headers as a {@link Map}
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    Map<String, List<String>> getCORS(final IRI identifier, final IRI origin) throws LdpClientException;

    /**
     * @param identifier a resource identifier
     * @param origin a root resource identifier
     * @return headers as a {@link Map}
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    Map<String, List<String>> getCORSSimple(final IRI identifier, final IRI origin) throws LdpClientException;

    /**
     * @param identifier a resource identifier
     * @param metadata a {@link Map} of headers
     * @return body as a {@link String}
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    String getWithMetadata(IRI identifier, Map<String, String> metadata) throws LdpClientException;

    /**
     * @param identifier a resource identifier
     * @param metadata a {@link Map} of headers
     * @return body as a byte[]
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    byte[] getBytesWithMetadata(IRI identifier, Map<String, String> metadata) throws LdpClientException;

    /**
     * @param identifier a resource identifier
     * @param metadata a {@link Map} of headers
     * @return body and headers as a {@link Map}
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    Map<String, Map<String, List<String>>> getResponse(final IRI identifier, final Map<String, String> metadata)
            throws LdpClientException;

    /**
     * @param identifier a resource identifier
     * @return headers as a {@link Map}
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    Map<String, List<String>> options(final IRI identifier) throws LdpClientException;

    /**
     * @param identifier a resource identifier
     * @param stream an {@link InputStream}
     * @param contentType a content type
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    void post(final IRI identifier, final InputStream stream, final String contentType) throws LdpClientException;

    /**
     * @param identifier a resource identifier
     * @param stream an {@link InputStream}
     * @param metadata a {@link Map} of headers
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    void postWithMetadata(final IRI identifier, final InputStream stream, final Map<String, String> metadata) throws
            LdpClientException;

    /**
     * @param identifier a resource identifier
     * @param stream an {@link InputStream}
     * @param contentType a content type
     * @param authorization an authorization token
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    void postWithAuth(final IRI identifier, final InputStream stream, final String contentType, final String
            authorization) throws LdpClientException;

    /**
     * @param identifier a resource identifier
     * @param slug a resource name as a {@link String}
     * @param stream an {@link InputStream}
     * @param contentType a content type
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    void postSlug(final IRI identifier, final String slug, final InputStream stream, final String contentType) throws
            LdpClientException;

    /**
     * @param identifier a resource identifier
     * @param stream an {@link InputStream}
     * @param contentType a content type
     * @param digest a digest as a {@link String}
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     * @see <a href="https://tools.ietf.org/html/rfc3230#page-9">rfc3230 4.3.2 Digest</a>
     */
    void postBinaryWithDigest(final IRI identifier, final InputStream stream, final String contentType, String
            digest) throws LdpClientException;

    /**
     * @param identifier a resource identifier
     * @param slug a resource name as a {@link String}
     * @param membershipObj a membership Object identifier
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    void newLdpDc(final IRI identifier, final String slug, IRI membershipObj) throws LdpClientException;

    /**
     * @param identifier a resource identifier
     * @param slug a resource name as a {@link String}
     * @param membershipObj a membership Object identifier
     * @param authorization an authorization token
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    void newLdpDcWithAuth(final IRI identifier, final String slug, IRI membershipObj, String authorization) throws
            LdpClientException;

    /**
     * @param identifier a resource identifier
     * @param stream an {@link InputStream}
     * @param contentType a content type
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    void put(final IRI identifier, final InputStream stream, final String contentType) throws LdpClientException;

    /**
     * @param identifier a resource identifier
     * @param stream an {@link InputStream}
     * @param metadata a {@link Map} of headers
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    void putWithMetadata(final IRI identifier, final InputStream stream, final Map<String, String> metadata) throws
            LdpClientException;

    /**
     * @param identifier a resource identifier
     * @param stream an {@link InputStream}
     * @param contentType a content Type as a {@link String}
     * @param authorization an authorization token
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    void putWithAuth(final IRI identifier, final InputStream stream, final String contentType, final String
            authorization) throws LdpClientException;

    /**
     * @param identifier a resource identifier
     * @param stream an {@link InputStream}
     * @param contentType a content type
     * @param etag a strong Etag as a {@link String}
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    void putIfMatch(final IRI identifier, final InputStream stream, final String contentType, String etag) throws
            LdpClientException;

    /**
     * @param identifier a resource identifier
     * @param stream an {@link InputStream}
     * @param contentType a content type
     * @param digest a digest as a {@link String}
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     * @see <a href="https://tools.ietf.org/html/rfc3230#page-9">rfc3230 4.3.2 Digest</a>
     */
    void putBinaryWithDigest(final IRI identifier, final InputStream stream, final String contentType, String digest)
            throws LdpClientException;

    /**
     * @param identifier a resource identifier
     * @param stream an {@link InputStream}
     * @param contentType a content type
     * @param time an RFC_1123_DATE_TIME as an {@link String}
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    void putIfUnmodified(final IRI identifier, final InputStream stream, final String contentType, final String time)
            throws LdpClientException;

    /**
     * @param identifier a resource identifier
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    void delete(final IRI identifier) throws LdpClientException;

    /**
     * @param identifier a resource identifier
     * @param stream an {@link InputStream}
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    void patch(final IRI identifier, final InputStream stream) throws LdpClientException;

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
     * @param sessionId a session identifier
     */
    void multipartGet(final IRI identifier, String sessionId);

    /**
     * @param identifier a resource identifier
     * @param stream an {@link InputStream}
     * @param sessionId a session identifier
     */
    void multipartPut(final IRI identifier, final InputStream stream, String sessionId);

    /**
     * @param identifier a resource identifier
     * @param stream an {@link InputStream}
     * @param sessionId a session identifier
     */
    void multipartPost(final IRI identifier, final InputStream stream, String sessionId);

    /**
     * @param identifier a resource identifier
     * @param sessionId a session identifier
     */
    void multipartDelete(final IRI identifier, String sessionId);

    /**
     * @param identifier a resource identifier
     * @param stream an {@link InputStream}
     * @throws LdpClientException an URISyntaxException
     */
    void asyncPut(final IRI identifier, final InputStream stream) throws LdpClientException;

}
