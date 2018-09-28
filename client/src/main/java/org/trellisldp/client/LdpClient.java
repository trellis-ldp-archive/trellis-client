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

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.commons.rdf.api.IRI;

/**
 * LdpClient.
 *
 * @author christopher-johnson
 */
public interface LdpClient {

    /**
     * head.
     *
     * @param identifier a resource identifier
     * @return Map of headers
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    Map<String, List<String>> head(final IRI identifier) throws LdpClientException;

    /**
     * getJson.
     *
     * @param identifier a resource identifier
     * @return body as a {@link String}
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    String getJson(final IRI identifier) throws LdpClientException;

    /**
     * getDefaultType.
     *
     * @param identifier a resource identifier
     * @return body as a {@link String}
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    String getDefaultType(final IRI identifier) throws LdpClientException;

    /**
     * getWithContentType.
     *
     * @param identifier  a resource identifier
     * @param contentType a content type (text/turtle, application/n-triples or application/ld+json)
     * @return body as a {@link String}
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    String getWithContentType(final IRI identifier, final String contentType) throws LdpClientException;

    /**
     * getAcceptDatetime.
     *
     * @param identifier a resource identifier
     * @param timestamp  an epoch millisecond
     * @return headers as a {@link Map}
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    Map<String, List<String>> getAcceptDatetime(final IRI identifier, String timestamp) throws LdpClientException;

    /**
     * getTimeMapLinkDefaultFormat.
     *
     * @param identifier a resource identifier
     * @return body as a {@link String}
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    String getTimeMapLinkDefaultFormat(IRI identifier) throws LdpClientException;

    /**
     * getTimeMapJsonProfile.
     *
     * @param identifier a resource identifier
     * @param profile    a JSON-LD profile
     * @return body as a {@link String}
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     * @see <a href="https://www.w3.org/ns/json-ld">The JSON-LD Vocabulary</a>
     */
    String getTimeMapJsonProfile(IRI identifier, String profile) throws LdpClientException;

    /**
     * getVersionJson.
     *
     * @param identifier a resource identifier
     * @param profile    a JSON-LD profile
     * @param timestamp  an epoch millisecond
     * @return body as a {@link String}
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     * @see <a href="https://www.w3.org/ns/json-ld">The JSON-LD Vocabulary</a>
     */
    String getVersionJson(IRI identifier, String profile, String timestamp) throws LdpClientException;

    /**
     * getBinary.
     *
     * @param identifier a resource identifier
     * @param file       an output file as an {@link Path}
     * @return body as a {@link Path}
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    Path getBinary(IRI identifier, Path file) throws LdpClientException;

    /**
     * getBinary.
     *
     * @param identifier a resource identifier
     * @return body as a byte[]
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    byte[] getBinary(IRI identifier) throws LdpClientException;

    /**
     * getBinaryDigest.
     *
     * @param identifier a resource identifier
     * @param algorithm  a digest algorithm (md5, sha, sha-256 or sha-512)
     * @return digest as a {@link String}
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    String getBinaryDigest(IRI identifier, String algorithm) throws LdpClientException;

    /**
     * getBinaryVersion.
     *
     * @param identifier a resource identifier
     * @param file       an output file as an {@link Path}
     * @param timestamp  an epoch millisecond
     * @return body as a  {@link Path}
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    Path getBinaryVersion(IRI identifier, Path file, String timestamp) throws LdpClientException;

    /**
     * getBinaryVersion.
     *
     * @param identifier a resource identifier
     * @param timestamp  an epoch millisecond
     * @return body as a byte[]
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    byte[] getBinaryVersion(IRI identifier, String timestamp) throws LdpClientException;

    /**
     * getRange.
     *
     * @param identifier a resource identifier
     * @param byterange  a byterange
     * @return body as a byte[]
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    byte[] getRange(final IRI identifier, String byterange) throws LdpClientException;

    /**
     * getPrefer.
     *
     * @param identifier a resource identifier
     * @param prefer     an LDP preference
     * @return body as a {@link String}
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     * @see <a href="https://www.w3.org/TR/ldp/#prefer-parameters">7.2 Preferences on the Prefer
     * Request Header</a>
     */
    String getPrefer(final IRI identifier, String prefer) throws LdpClientException;

    /**
     * getPreferMinimal.
     *
     * @param identifier a resource identifier
     * @return body as a {@link String}
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    String getPreferMinimal(IRI identifier) throws LdpClientException;

    /**
     * getJsonProfile.
     *
     * @param identifier a resource identifier
     * @param profile    a JSON-LD profile
     * @return body as a {@link String}
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     * @see <a href="https://www.w3.org/ns/json-ld">The JSON-LD Vocabulary</a>
     */
    String getJsonProfile(IRI identifier, String profile) throws LdpClientException;

    /**
     * getJsonProfileLDF.
     *
     * @param identifier a resource identifier
     * @param profile    a JSON-LD profile
     * @param subject    RdfTerm as a {@link String}
     * @param predicate  RdfTerm as a {@link String}
     * @param object     RdfTerm as a {@link String}
     * @return body as a {@link String}
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     * @see <a href="https://www.w3.org/ns/json-ld">The JSON-LD Vocabulary</a>
     */
    String getJsonProfileLDF(IRI identifier, String profile, String subject, String predicate, String object) throws
            LdpClientException;

    /**
     * getJsonLDF.
     *
     * @param identifier a resource identifier
     * @param subject    RdfTerm as a {@link String}
     * @param predicate  RdfTerm as a {@link String}
     * @param object     RdfTerm as a {@link String}
     * @return body as a {@link String}
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    String getJsonLDF(IRI identifier, String subject, String predicate, String object) throws LdpClientException;

    /**
     * getAcl.
     *
     * @param identifier  a resource identifier
     * @param contentType a content type as {@link String}
     * @return body as a {@link String}
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    String getAcl(IRI identifier, String contentType) throws LdpClientException;


    /**
     * getCORS.
     *
     * @param identifier a resource identifier
     * @param origin     a root resource identifier
     * @return headers as a {@link Map}
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    Map<String, List<String>> getCORS(final IRI identifier, final IRI origin) throws LdpClientException;

    /**
     * getCORSSimple.
     *
     * @param identifier a resource identifier
     * @param origin     a root resource identifier
     * @return headers as a {@link Map}
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    Map<String, List<String>> getCORSSimple(final IRI identifier, final IRI origin) throws LdpClientException;

    /**
     * getWithMetadata.
     *
     * @param identifier a resource identifier
     * @param metadata   a {@link Map} of headers
     * @return body as a {@link String}
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    String getWithMetadata(IRI identifier, Map<String, String> metadata) throws LdpClientException;

    /**
     * getBytesWithMetadata.
     *
     * @param identifier a resource identifier
     * @param metadata   a {@link Map} of headers
     * @return body as a byte[]
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    byte[] getBytesWithMetadata(IRI identifier, Map<String, String> metadata) throws LdpClientException;

    /**
     * getResponse.
     *
     * @param identifier identifier
     * @return HttpResponse response
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    HttpResponse getResponse(final IRI identifier) throws LdpClientException;

    /**
     * getResponseWithHeaders.
     *
     * @param identifier a resource identifier
     * @param metadata   a {@link Map} of headers
     * @return body and headers as a {@link Map}
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    Map<String, Map<String, List<String>>> getResponseWithHeaders(final IRI identifier, final Map<String, String>
            metadata) throws LdpClientException;

    /**
     * options.
     *
     * @param identifier a resource identifier
     * @return headers as a {@link Map}
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    Map<String, List<String>> options(final IRI identifier) throws LdpClientException;

    /**
     * initUpgrade.
     * H2c writes require a "preflight" options to upgrade the 1.1 connection before streams can be read.
     *
     * @param identifier a resource identifier
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    void initUpgrade(final IRI identifier) throws LdpClientException;

    /**
     * post.
     *
     * @param identifier  a resource identifier
     * @param stream      an {@link InputStream}
     * @param contentType a content type
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    void post(final IRI identifier, final InputStream stream, final String contentType) throws LdpClientException;

    /**
     * postWithMetadata.
     *
     * @param identifier a resource identifier
     * @param stream     an {@link InputStream}
     * @param metadata   a {@link Map} of headers
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    void postWithMetadata(final IRI identifier, final InputStream stream, final Map<String, String> metadata) throws
            LdpClientException;

    /**
     * postWithAuth.
     *
     * @param identifier    a resource identifier
     * @param stream        an {@link InputStream}
     * @param contentType   a content type
     * @param authorization an authorization token
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    void postWithAuth(final IRI identifier, final InputStream stream, final String contentType, final String
            authorization) throws LdpClientException;

    /**
     * postSlug.
     *
     * @param identifier  a resource identifier
     * @param slug        a resource name as a {@link String}
     * @param stream      an {@link InputStream}
     * @param contentType a content type
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    void postSlug(final IRI identifier, final String slug, final InputStream stream, final String contentType) throws
            LdpClientException;

    /**
     * postBinaryWithDigest.
     *
     * @param identifier  a resource identifier
     * @param stream      an {@link InputStream}
     * @param contentType a content type
     * @param digest      a digest as a {@link String}
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     * @see <a href="https://tools.ietf.org/html/rfc3230#page-9">rfc3230 4.3.2 Digest</a>
     */
    void postBinaryWithDigest(final IRI identifier, final InputStream stream, final String contentType, String
            digest) throws LdpClientException;


    /**
     * createBasicContainer.
     *
     * @param identifier a resource identifier
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    void createBasicContainer(final IRI identifier) throws LdpClientException;

    /**
     * createDirectContainer.
     *
     * @param identifier    a resource identifier
     * @param slug          a resource name as a {@link String}
     * @param membershipObj a membership Object identifier
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    void createDirectContainer(final IRI identifier, final String slug, IRI membershipObj) throws LdpClientException;

    /**
     * createDirectContainerWithAuth.
     *
     * @param identifier    a resource identifier
     * @param slug          a resource name as a {@link String}
     * @param membershipObj a membership Object identifier
     * @param authorization an authorization token
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    void createDirectContainerWithAuth(final IRI identifier, final String slug, IRI membershipObj, String
            authorization) throws LdpClientException;

    /**
     * put.
     *
     * @param identifier  a resource identifier
     * @param stream      an {@link InputStream}
     * @param contentType a content type
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    void put(final IRI identifier, final InputStream stream, final String contentType) throws LdpClientException;

    /**
     * put.
     *
     * @param identifier              a resource identifier
     * @param fileInputStreamSupplier an {@link Supplier}
     * @param contentType             a content type
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    void putSupplier(final IRI identifier, Supplier<FileInputStream> fileInputStreamSupplier, final String
            contentType) throws LdpClientException;

    /**
     * putWithResponse.
     *
     * @param identifier  a resource identifier
     * @param stream      an {@link InputStream}
     * @param contentType a content type
     * @return status a {@link Boolean}
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    Boolean putWithResponse(final IRI identifier, final InputStream stream, final String contentType) throws
            LdpClientException;

    /**
     * putWithMetadata.
     *
     * @param identifier a resource identifier
     * @param stream     an {@link InputStream}
     * @param metadata   a {@link Map} of headers
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    void putWithMetadata(final IRI identifier, final InputStream stream, final Map<String, String> metadata) throws
            LdpClientException;

    /**
     * putWithAuth.
     *
     * @param identifier    a resource identifier
     * @param stream        an {@link InputStream}
     * @param contentType   a content Type as a {@link String}
     * @param authorization an authorization token
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    void putWithAuth(final IRI identifier, final InputStream stream, final String contentType, final String
            authorization) throws LdpClientException;

    /**
     * putIfMatch.
     *
     * @param identifier  a resource identifier
     * @param stream      an {@link InputStream}
     * @param contentType a content type
     * @param etag        a strong Etag as a {@link String}
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    void putIfMatch(final IRI identifier, final InputStream stream, final String contentType, String etag) throws
            LdpClientException;

    /**
     * putBinaryWithDigest.
     *
     * @param identifier  a resource identifier
     * @param stream      an {@link InputStream}
     * @param contentType a content type
     * @param digest      a digest as a {@link String}
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     * @see <a href="https://tools.ietf.org/html/rfc3230#page-9">rfc3230 4.3.2 Digest</a>
     */
    void putBinaryWithDigest(final IRI identifier, final InputStream stream, final String contentType, String digest)
            throws LdpClientException;

    /**
     * putIfUnmodified.
     *
     * @param identifier  a resource identifier
     * @param stream      an {@link InputStream}
     * @param contentType a content type
     * @param time        an RFC_1123_DATE_TIME as an {@link String}
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    void putIfUnmodified(final IRI identifier, final InputStream stream, final String contentType, final String time)
            throws LdpClientException;

    /**
     * delete.
     *
     * @param identifier a resource identifier
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    void delete(final IRI identifier) throws LdpClientException;

    /**
     * patch.
     *
     * @param identifier a resource identifier
     * @param stream     an {@link InputStream}
     * @throws LdpClientException an URISyntaxException, IOException or InterruptedException
     */
    void patch(final IRI identifier, final InputStream stream) throws LdpClientException;

    /**
     * asyncPut.
     *
     * @param identifier a resource identifier
     * @param stream     an {@link InputStream}
     * @return Boolean boolean
     * @throws LdpClientException an URISyntaxException
     */
    Boolean asyncPut(final IRI identifier, final InputStream stream) throws LdpClientException;

    /**
     * joiningCompleteableFuturePut.
     *
     * @param bodies      a Map of URI keys with InputStream values
     * @param contentType a content Type
     */
    void joiningCompletableFuturePut(Map<URI, InputStream> bodies, final String contentType);

}
