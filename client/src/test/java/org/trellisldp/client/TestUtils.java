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

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyMap;
import static org.trellisldp.api.RDFUtils.getInstance;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.ws.rs.core.Link;

import org.apache.commons.io.IOUtils;
import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDFSyntax;
import org.apache.commons.rdf.api.Triple;
import org.trellisldp.api.IOService;
import org.trellisldp.api.NoopNamespaceService;
import org.trellisldp.io.JenaIOService;

/**
 * Common utility functions.
 */
public final class TestUtils {

    private static final IOService ioService = new JenaIOService(new NoopNamespaceService(), null, emptyMap());
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Build a JWT Token.
     *
     * @param webid the web ID
     * @param secret the JWT secret
     * @return the JWT token
     */
    public static String buildJwt(final String webid, final String secret) {
        return "Bearer " + Jwts.builder().claim("webid", webid).signWith(
                SignatureAlgorithm.HS512, secret.getBytes(UTF_8)).compact();
    }

    /**
     * Get the IO service.
     *
     * @return the I/O service
     */
    public static IOService getIOService() {
        return ioService;
    }


    /**
     * Read an entity as an RDF Graph.
     *
     * @param entity the HTTP entity
     * @param baseURL the base URL
     * @param syntax the RDF syntax
     * @return the graph
     */
    public static Graph readEntityAsGraph(final InputStream entity, final String baseURL, final RDFSyntax syntax) {
        final Graph g = getInstance().createGraph();
        getIOService().read(entity, baseURL, syntax).forEach(g::add);
        return g;
    }

    /**
     * Read an http entity as a string.
     *
     * @param entity the entity
     * @return the entity as a string
     */
    public static String readEntityAsString(final Object entity) {
        try {
            return IOUtils.toString((InputStream) entity, UTF_8);
        } catch (final IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    /**
     * Parse a JSON entity into the desired type.
     *
     * @param entity the entity
     * @param valueType the type reference
     * @param <T> the intended return type
     * @return the entity as the desired type
     */
    public static <T> T readEntityAsJson(final Object entity, final TypeReference<T> valueType) {
        try {
            return MAPPER.readValue(readEntityAsString(entity), valueType);
        } catch (final IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    /**
     * Test if the given type link is present.
     *
     * @param iri the IRI
     * @return true if present; false otherwise
     */
    public static Predicate<Link> hasType(final IRI iri) {
        return link -> "type".equals(link.getRel()) && iri.getIRIString().equals(link.getUri().toString());
    }

    private TestUtils() {
        // prevent instantiation
    }

    static Optional<? extends Triple> closeableFindAny(Stream<? extends Triple> stream) {
        try (Stream<? extends Triple> s = stream) {
            return s.findAny();
        }
    }
}

