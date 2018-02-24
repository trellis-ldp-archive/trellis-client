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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.jena.JenaRDF;
import org.junit.jupiter.api.Test;

/**
 * @author christopher-johnson
 */
class LdpClientExceptionTest {
    private static final JenaRDF rdf = new JenaRDF();
    private final LdpClientImpl client = new LdpClientImpl();

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

    @Test
    void testURISyntaxException() {
        assertThrows(LdpClientException.class, () -> {
            final IRI identifier = rdf.createIRI("httq://some.fictitious.org");
            client.head(identifier);
        });
        assertThrows(LdpClientException.class, () -> {
            final IRI identifier = rdf.createIRI("httq://some.fictitious.org");
            client.getJson(identifier);
        });
        assertThrows(LdpClientException.class, () -> {
            final IRI identifier = rdf.createIRI("httq://some.fictitious.org");
            client.getDefaultType(identifier);
        });
        assertThrows(LdpClientException.class, () -> {
            final IRI identifier = rdf.createIRI("httq://some.fictitious.org");
            client.getWithContentType(identifier, null);
        });
        assertThrows(LdpClientException.class, () -> {
            final IRI identifier = rdf.createIRI("httq://some.fictitious.org");
            client.getAcceptDatetime(identifier, null);
        });
        assertThrows(LdpClientException.class, () -> {
            final IRI identifier = rdf.createIRI("httq://some.fictitious.org");
            client.getTimeMapLinkDefaultFormat(identifier);
        });
        assertThrows(LdpClientException.class, () -> {
            final IRI identifier = rdf.createIRI("httq://some.fictitious.org");
            client.getTimeMapJsonProfile(identifier, null);
        });
        assertThrows(LdpClientException.class, () -> {
            final IRI identifier = rdf.createIRI("httq://some.fictitious.org");
            client.getVersionJson(identifier, null, null);
        });
        assertThrows(LdpClientException.class, () -> {
            final IRI identifier = rdf.createIRI("httq://some.fictitious.org");
            client.getBinary(identifier);
        });
        assertThrows(LdpClientException.class, () -> {
            final IRI identifier = rdf.createIRI("httq://some.fictitious.org");
            client.getBinary(identifier, null);
        });
        assertThrows(LdpClientException.class, () -> {
            final IRI identifier = rdf.createIRI("httq://some.fictitious.org");
            client.getBinary(identifier);
        });
        assertThrows(LdpClientException.class, () -> {
            final IRI identifier = rdf.createIRI("httq://some.fictitious.org");
            client.getBinaryDigest(identifier, "sha256");
        });
        assertThrows(LdpClientException.class, () -> {
            final IRI identifier = rdf.createIRI("httq://some.fictitious.org");
            client.getBinaryVersion(identifier, null);
        });
        assertThrows(LdpClientException.class, () -> {
            final IRI identifier = rdf.createIRI("httq://some.fictitious.org");
            client.getBinaryVersion(identifier, null, null);
        });
    }
}
