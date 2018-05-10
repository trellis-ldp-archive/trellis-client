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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.jena.JenaRDF;
import org.junit.jupiter.api.Test;

/**
 * LdpClientExceptionTest.
 *
 * @author christopher-johnson
 */
class LdpClientExceptionTest {

    private static final JenaRDF rdf = new JenaRDF();
    private final LdpClientImpl client = new LdpClientImpl();
    private final IRI identifier = rdf.createIRI("httq://some.fictitious.org");

    @Test
    void testException1() {
        final LdpClientException ex = new LdpClientException();
        assertNull(ex.getMessage());
    }

    @Test
    void testException2() {
        final String msg = "the cause";
        final LdpClientException ex = new LdpClientException(msg);
        assertEquals(msg, ex.getMessage());
    }

    @Test
    void testException3() {
        final Throwable cause = new Throwable("an error");
        final LdpClientException ex = new LdpClientException(cause);
        assertEquals(cause, ex.getCause());
    }

    @Test
    void testException4() {
        final Throwable cause = new Throwable("an error");
        final String msg = "The message";
        final LdpClientException ex = new LdpClientException(msg, cause);
        assertEquals(cause, ex.getCause());
        assertEquals(msg, ex.getMessage());
    }

    //Note: for exception test coverage
    @Test
    void testURISyntaxException() {
        assertThrows(LdpClientException.class, () -> client.head(identifier));
        assertThrows(LdpClientException.class, () -> client.getJson(identifier));
        assertThrows(LdpClientException.class, () -> client.getDefaultType(identifier));
        assertThrows(LdpClientException.class, () -> client.getWithContentType(identifier, null));
        assertThrows(LdpClientException.class, () -> client.getAcceptDatetime(identifier, null));
        assertThrows(LdpClientException.class, () -> client.getTimeMapLinkDefaultFormat(identifier));
        assertThrows(LdpClientException.class, () -> client.getTimeMapJsonProfile(identifier, null));
        assertThrows(LdpClientException.class, () -> client.getVersionJson(identifier, null, null));
        assertThrows(LdpClientException.class, () -> client.getBinary(identifier));
        assertThrows(LdpClientException.class, () -> client.getBinary(identifier, null));
        assertThrows(LdpClientException.class, () -> client.getBinary(identifier));
        assertThrows(LdpClientException.class, () -> client.getBinaryDigest(identifier, "sha256"));
        assertThrows(LdpClientException.class, () -> client.getBinaryVersion(identifier, null));
        assertThrows(LdpClientException.class, () -> client.getBinaryVersion(identifier, null, null));
        assertThrows(LdpClientException.class, () -> client.getRange(identifier, null));
        assertThrows(LdpClientException.class, () -> client.getPrefer(identifier, null));
        assertThrows(LdpClientException.class, () -> client.getPreferMinimal(identifier));
        assertThrows(LdpClientException.class, () -> client.getJsonProfile(identifier, null));
        assertThrows(LdpClientException.class, () -> client.getJsonProfileLDF(identifier, null, null, null, null));
        assertThrows(LdpClientException.class, () -> client.getJsonLDF(identifier, null, null, null));
        assertThrows(LdpClientException.class, () -> client.getAcl(identifier, null));
        assertThrows(LdpClientException.class, () -> client.getCORS(identifier, null));
        assertThrows(LdpClientException.class, () -> client.getCORSSimple(identifier, null));
        assertThrows(LdpClientException.class, () -> client.getWithMetadata(identifier, null));
        assertThrows(LdpClientException.class, () -> client.getBytesWithMetadata(identifier, null));
        assertThrows(LdpClientException.class, () -> client.getResponseWithHeaders(identifier, null));
        assertThrows(LdpClientException.class, () -> client.getResponse(identifier));
        assertThrows(LdpClientException.class, () -> client.options(identifier));
        assertThrows(LdpClientException.class, () -> client.post(identifier, null, null));
        assertThrows(LdpClientException.class, () -> client.postWithMetadata(identifier, null, null));
        assertThrows(LdpClientException.class, () -> client.postWithAuth(identifier, null, null, null));
        assertThrows(LdpClientException.class, () -> client.postSlug(identifier, null, null, null));
        assertThrows(LdpClientException.class, () -> client.postBinaryWithDigest(identifier, null, null, null));
        assertThrows(LdpClientException.class, () -> client.createDirectContainer(identifier, null, null));
        assertThrows(
                LdpClientException.class, () -> client.createDirectContainerWithAuth(identifier, null, null, null));
        assertThrows(LdpClientException.class, () -> client.put(identifier, null, null));
        assertThrows(LdpClientException.class, () -> client.putWithResponse(identifier, null, null));
        assertThrows(LdpClientException.class, () -> client.putWithMetadata(identifier, null, null));
        assertThrows(LdpClientException.class, () -> client.putWithAuth(identifier, null, null, null));
        assertThrows(LdpClientException.class, () -> client.putIfMatch(identifier, null, null, null));
        assertThrows(LdpClientException.class, () -> client.putBinaryWithDigest(identifier, null, null, null));
        assertThrows(LdpClientException.class, () -> client.putIfUnmodified(identifier, null, null, null));
        assertThrows(LdpClientException.class, () -> client.delete(identifier));
        assertThrows(LdpClientException.class, () -> client.patch(identifier, null));
    }
}
