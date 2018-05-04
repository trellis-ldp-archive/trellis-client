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

import static org.apache.jena.arq.riot.WebContent.contentTypeJSONLD;

import java.io.InputStream;

import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.jena.JenaRDF;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;

public class RepeatedTest extends CommonTrellisTest {

    private static final JenaRDF rdf = new JenaRDF();
    private final LdpClient client = new LdpClientImpl();
    private static String baseUrl;
    private static String pid;

    @BeforeAll
    static void initAll() {
        APP.before();
        baseUrl = "http://localhost:" + APP.getLocalPort() + "/";
    }

    @AfterAll
    static void tearDownAll() {
        APP.after();

    }

    private static InputStream getTestJsonResource() {
        return LdpClientTest.class.getResourceAsStream("/webanno.complete-embedded.json");
    }

    @DisplayName("RepeatedH1Put")
    @org.junit.jupiter.api.RepeatedTest(200)
    void testRepeatedH1Put() throws LdpClientException {
        try {
            final IRI identifier = rdf.createIRI(baseUrl + pid);
            client.put(identifier, getTestJsonResource(), contentTypeJSONLD);
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }
}
