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

import static org.apache.jena.riot.RDFFormat.NTRIPLES;
import static org.apache.jena.riot.system.StreamRDFWriter.getWriterStream;

import java.io.ByteArrayOutputStream;
import java.util.Set;
import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.jena.JenaRDF;
import org.apache.jena.riot.system.StreamRDF;
import org.trellisldp.vocabulary.ACL;
import org.trellisldp.vocabulary.RDF;

/**
 * ACLStatement.
 *
 * @author christopher-johnson
 */
public class ACLStatement {
    private static final JenaRDF rdf = new JenaRDF();
    private final Set<IRI> modes;
    private final IRI agent;
    private final IRI accessTo;
    private Graph graph = rdf.createGraph();

    /**
     * ACLStatement.
     *
     * @param modes a {@link Set} of ACL modes
     * @param agent a user agent
     * @param accessTo the resource this ACL grants access to
     */
    public ACLStatement(final Set<IRI> modes, final IRI agent, final IRI accessTo) {
        this.modes = modes;
        this.agent = agent;
        this.accessTo = accessTo;
    }

    /**
     * ByteArrayOutputStream.
     *
     * @return ntriples as a {@link ByteArrayOutputStream}
     */
    public ByteArrayOutputStream getACL() {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final StreamRDF stream = getWriterStream(out, NTRIPLES);
        final IRI auth = rdf.createIRI(accessTo.getIRIString() + "?ext=acl#auth");
        graph.add(auth, RDF.type, ACL.Authorization);
        graph.add(auth, ACL.agent, agent);
        graph.add(auth, ACL.accessTo, accessTo);
        modes.forEach(m -> {
            graph.add(auth, ACL.mode, m);
        });
        stream.start();
        graph.stream().map(rdf::asJenaTriple).forEachOrdered(stream::triple);
        stream.finish();
        return out;
    }
}
