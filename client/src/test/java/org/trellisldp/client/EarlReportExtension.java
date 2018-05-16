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

import static java.time.LocalDateTime.now;
import static org.apache.jena.arq.riot.RDFFormat.NQUADS;

import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.utils.JsonUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Objects;

import org.apache.commons.rdf.api.BlankNode;
import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.jena.JenaRDF;
import org.apache.jena.arq.riot.JsonLDWriteContext;
import org.apache.jena.arq.riot.RDFDataMgr;
import org.apache.jena.arq.riot.WriterDatasetRIOT;
import org.apache.jena.arq.riot.system.PrefixMap;
import org.apache.jena.arq.riot.system.RiotLib;
import org.apache.jena.arq.sparql.core.DatasetGraph;
import org.apache.jena.arq.sparql.core.DatasetGraphFactory;
import org.apache.jena.core.vocabulary.RDFTest;
import org.apache.jena.core.vocabulary.TestManifest;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.trellisldp.vocabulary.DC;
import org.trellisldp.vocabulary.RDF;
import org.trellisldp.vocabulary.RDFS;

public class EarlReportExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback {

    private static final JenaRDF rdf = new JenaRDF();
    private static Graph graph = rdf.createGraph();
    private static String testSuite = "https://github.com/trellis-ldp/trellis-client/testsuite";
    private static String project = "https://github.com/trellis-ldp/trellis-client";
    private static String testNamespace = "https://github.com/trellis-ldp/trellis-client/testsuite#";
    private static String earlNamespace = "http://www.w3.org/ns/earl#";

    public static Graph buildTestSuite() {
        graph.add(rdf.createIRI(testSuite), RDF.type, rdf.createIRI(TestManifest.Manifest.getURI()));
        graph.add(rdf.createIRI(testSuite), RDFS.comment, rdf.createLiteral("LDP Client tests"));
        graph.add(rdf.createIRI(testSuite), rdf.createIRI(TestManifest.getURI() + "entries"), rdf.createBlankNode());
        return graph;
    }

    public static Graph buildTestCase(String testName, String testDescription) {
        graph.add(rdf.createIRI(testNamespace + testName), RDF.type, rdf.createIRI(earlNamespace + "TestCase"));
        graph.add(rdf.createIRI(testNamespace + testName), DC.date, rdf.createLiteral(now().toString()));
        graph.add(rdf.createIRI(testNamespace + testName), DC.description, rdf.createLiteral(testDescription));
        graph.add(rdf.createIRI(testNamespace + testName), rdf.createIRI(TestManifest.name.getURI()),
                rdf.createLiteral(testName));
        graph.add(rdf.createIRI(testNamespace + testName), rdf.createIRI(TestManifest.action.getURI()),
                rdf.createIRI(testNamespace + testName));
        graph.add(rdf.createIRI(testNamespace + testName), rdf.createIRI(TestManifest.result.getURI()),
                rdf.createIRI(testNamespace + testName + "-result"));
        graph.add(rdf.createIRI(testNamespace + testName), (IRI) rdf.asRDFTerm(RDFTest.approval.asNode()),
                rdf.createIRI(RDFTest.getURI() + "Approved"));
        return graph;
    }

    public static Graph buildTestAssertion(String testName, String testDescription, String testOutcome) {
        final BlankNode bnode = rdf.createBlankNode();
        graph.add(rdf.createIRI(testNamespace + testName + "-result"), RDF.type,
                rdf.createIRI(earlNamespace + "Assertion"));
        graph.add(rdf.createIRI(testNamespace + testName + "-result"), rdf.createIRI(earlNamespace + "assertedBy"),
                rdf.createIRI(project));
        graph.add(rdf.createIRI(testNamespace + testName + "-result"), rdf.createIRI(earlNamespace + "mode"),
                rdf.createIRI(earlNamespace + "automatic"));
        graph.add(rdf.createIRI(testNamespace + testName + "-result"), rdf.createIRI(earlNamespace + "result"), bnode);
        graph.add(bnode, RDF.type, rdf.createIRI(earlNamespace + "TestResult"));
        graph.add(bnode, DC.description, rdf.createLiteral(testDescription));
        graph.add(bnode, DC.title, rdf.createLiteral(testOutcome));
        final String earlTestOutcome;
        if (testOutcome.equals("TEST PASSED")) {
            earlTestOutcome = "pass";
            graph.add(bnode, rdf.createIRI(earlNamespace + "outcome"), rdf.createIRI(earlNamespace + earlTestOutcome));
        } else {
            earlTestOutcome = "fail";
            graph.add(bnode, rdf.createIRI(earlNamespace + "outcome"), rdf.createIRI(earlNamespace + earlTestOutcome));
        }
        graph.add(bnode, DC.date, rdf.createLiteral(now().toString()));
        graph.add(bnode, rdf.createIRI(earlNamespace + "subject"), rdf.createIRI(project));
        graph.add(bnode, rdf.createIRI(earlNamespace + "test"), rdf.createIRI(testNamespace + testName));
        return graph;
    }

    private static String writeGraphToN3(final DatasetGraph graph) {
        final WriterDatasetRIOT writer = RDFDataMgr.createDatasetWriter(NQUADS);
        Writer swriter = new StringWriter();
        final PrefixMap pm = RiotLib.prefixMap(graph);
        final String base = null;
        final JsonLDWriteContext ctx = new JsonLDWriteContext();
        writer.write(swriter, graph, pm, base, ctx);
        return swriter.toString();
    }

    private static void writeFramedJsonLd(final OutputStream output, final DatasetGraph graph) {
        String n3 = writeGraphToN3(graph);
        final JsonLdOptions opts = new JsonLdOptions();
        try {
        final Object outobj = com.github.jsonldjava.core.JsonLdProcessor.fromRDF(n3, opts);
        final InputStream fs = EarlReportExtension.class.getResourceAsStream("/earlreport/earlreport-frame.json");

            final Object frame = JsonUtils.fromInputStream(fs);
            final Object frameobj = com.github.jsonldjava.core.JsonLdProcessor.frame(outobj, frame, opts);
            final String json = JsonUtils.toPrettyString(frameobj);
            byte[] strToBytes = json.getBytes();
            output.write(strToBytes);
            output.close();
        } catch (IOException e) {
            e.getMessage();
        } catch (JsonLdError jsonLdError) {
            jsonLdError.printStackTrace();
        }
    }

    @Override
    public void beforeTestExecution(ExtensionContext context) {

    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        Boolean testResult = context.getExecutionException().isPresent();
        String testOutcome;
        if (testResult) {
            testOutcome = "TEST FAILED";
        } else {
            testOutcome = "TEST PASSED";
        }
        Graph g = buildTestAssertion(
                context.getDisplayName(), Objects.requireNonNull(context.getTestClass().orElse(null)).toString(),
                testOutcome);
        OutputStream out = null;
        try {
            final String dir = new File(
                    getClass().getResource("/earlreport/earlreport.json").getFile()).getParentFile().getPath();
            out = new FileOutputStream(dir + "/earlreport.json");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        writeFramedJsonLd(Objects.requireNonNull(out), DatasetGraphFactory.create(rdf.asJenaGraph(g)));
    }
}
