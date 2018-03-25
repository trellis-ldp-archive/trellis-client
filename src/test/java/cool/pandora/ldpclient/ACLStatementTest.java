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

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.jena.JenaRDF;
import org.junit.jupiter.api.Test;
import org.trellisldp.vocabulary.ACL;

/**
 * ACLStatementTest.
 *
 * @author christopher-johnson
 */
class ACLStatementTest {
    private static final Set<IRI> allModes = new HashSet<>();
    private static final JenaRDF rdf = new JenaRDF();

    static {
        allModes.add(ACL.Append);
        allModes.add(ACL.Read);
        allModes.add(ACL.Write);
        allModes.add(ACL.Control);
    }

    private final String baseURL = "http://localhost/";
    private final String pid = "ldp-test-" + UUID.randomUUID().toString();

    @Test
    void getACL() {
        final IRI agent = rdf.createIRI("http://localhost/test-user");
        final IRI accessTo = rdf.createIRI(baseURL + pid);
        final ACLStatement acl = new ACLStatement(allModes, agent, accessTo);
        final String text = new String(acl.getACL().toByteArray(), StandardCharsets.UTF_8);

    }
}
