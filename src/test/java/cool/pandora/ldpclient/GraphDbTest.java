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

import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.ws.rs.core.HttpHeaders.ACCEPT;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static jdk.incubator.http.HttpRequest.BodyPublisher.fromByteArray;
import static jdk.incubator.http.HttpRequest.BodyPublisher.fromString;
import static jdk.incubator.http.HttpResponse.BodyHandler.asByteArray;
import static jdk.incubator.http.HttpResponse.BodyHandler.asString;
import static jdk.incubator.http.HttpResponse.BodyHandler.discard;
import static org.apache.jena.riot.WebContent.contentTypeSPARQLQuery;
import static org.apache.jena.riot.WebContent.contentTypeSPARQLUpdate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLContext;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.jena.JenaRDF;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import jdk.incubator.http.HttpClient;
import jdk.incubator.http.HttpRequest;
import jdk.incubator.http.HttpResponse;

public class GraphDbTest {
    private static HttpClient client = getClient();
    private static LdpClient h2client;
    private static final JenaRDF rdf = new JenaRDF();
    private static String baseUrl;

    @BeforeAll
    static void initAll() {
        baseUrl = "https://localhost:8443/fuseki/trellis";
        try {
            final SimpleSSLContextJetty sslct = new SimpleSSLContextJetty();
            final SSLContext sslContext = sslct.get();
            h2client = new LdpClientImpl(sslContext);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static HttpClient getClient() {
        final ExecutorService exec = Executors.newCachedThreadPool();
        return HttpClient.newBuilder().executor(exec).build();
    }

    private static InputStream getSparqlUpdate() {
        return LdpClientTest.class.getResourceAsStream("/sparqlUpdateFuseki.txt");
    }

    @RepeatedTest(100)
    void testFusekiSyncGet() throws Exception {
        String query = baseUrl + "?query=SELECT%20%3Fsubject%20%3Fpredicate%20%3Fobject%0AWHERE%20%7B%0A%20%20%3Fsubject%20%3Fpredicate%20%3Fobject%0A%7D";
        final String response = h2client.getQuery(query, "application/sparql-results+json");
        System.out.println(response);
    }

    @RepeatedTest(100)
    void testFusekiSyncUpdate() throws Exception {
        final IRI identifier = rdf.createIRI(baseUrl + "/update");
        final InputStream is = getSparqlUpdate();
        String update = readFile(is);
        h2client.syncUpdate(identifier, update);
    }

    private void joiningCompletableFuturePostUpdate(URI target, final List<String> updates) throws Exception {
        final int REQUESTS = 200;
        CompletableFuture[] results = new CompletableFuture<?>[REQUESTS];
        URI uri = new URI(baseUrl + "?query=SELECT%20%3Fsubject%20%3Fpredicate%20%3Fobject%0AWHERE%20%7B%0A%20%20%3Fsubject%20" +
                "%3Fpredicate%20%3Fobject%0A%7D");
        for (int i = 0; i < REQUESTS; i++) {
            results[i] = client.sendAsync(
                    HttpRequest.newBuilder(uri).headers(CONTENT_TYPE,
                            contentTypeSPARQLQuery, ACCEPT, "application/sparql-results+json").GET().build(), asString());
        }
        CompletableFuture<?>[] cf = updates.stream().map((u) ->
            client.sendAsync(HttpRequest.newBuilder(target).headers(CONTENT_TYPE, contentTypeSPARQLUpdate).POST(
                    fromString(u)).build(), discard(null)).thenApply(HttpResponse::statusCode)).toArray(
                    CompletableFuture<?>[]::new);
        CompletableFuture[] cf2 = ArrayUtils.addAll(results, cf);
        CompletableFuture.allOf(cf2).join();
    }

    @Test
    void testJoiningCompleteableFuturePostUpdate() throws Exception {
        try {
            URI uri = new URI(baseUrl + "/update");
            final List<String> list = new ArrayList<>();
            final int LOOPS = 400;
            for (int i = 0; i < LOOPS; i++) {
                String pid = "urn:" + UUID.randomUUID().toString();

                final String update = "INSERT DATA { <" + pid + "> <urn:x:p> <urn:x:o>}";
                list.add(update);
            }
            joiningCompletableFuturePostUpdate(uri, list);
        } catch (Exception ex) {
            throw new LdpClientException(ex.toString(), ex.getCause());
        }
    }

    @Test
    void testFusekiAsyncUpdate() throws Exception {
        final int REQUESTS = 200;
        RequestLimiter limiter = new RequestLimiter(400);
        CompletableFuture[] results = new CompletableFuture<?>[REQUESTS];
        HashMap<HttpRequest, byte[]> bodies = new HashMap<>();

        for (int i = 0; i < REQUESTS; i++) {
            byte[] buf = readFile(getSparqlUpdate()).getBytes();
            URI uri = new URI(baseUrl + "/update");
            HttpRequest r = HttpRequest.newBuilder(uri).header("XFixed", "true")
                    .headers(CONTENT_TYPE, contentTypeSPARQLUpdate).POST(fromByteArray(buf)).build();
            bodies.put(r, buf);
            results[i] = limiter.whenOkToSend().thenCompose((v) -> {
                System.out.println("Client: sendAsync: " + r.uri());
                return client.sendAsync(r, asByteArray());
            }).thenCompose((resp) -> {
                System.out.printf("Status Code: %d \n", resp.statusCode());
                limiter.requestComplete();
                return CompletableFuture.completedStage(resp.body()).thenApply((b) -> new Pair<>(resp, b));
            }).thenAccept((pair) -> {
                HttpRequest request = pair.t.request();
                byte[] requestBody = bodies.get(request);
            });
        }
        CompletableFuture.allOf(results).join();
    }

    private static String readFile(InputStream in) throws IOException {
        StringBuilder inobj = new StringBuilder();
        try (BufferedReader buf = new BufferedReader(new InputStreamReader(in, UTF_8))) {
            String line;
            while ((line = buf.readLine()) != null) {
                inobj.append(line).append("\n");
            }
        }
        return inobj.toString();
    }

    static final class Pair<T, U> {
        Pair(T t, U u) {
            this.t = t;
            this.u = u;
        }

        T t;
        U u;
    }

    static class RequestLimiter {

        static final CompletableFuture<Void> COMPLETED_FUTURE = CompletableFuture.completedFuture(null);

        final int maxnumber;
        final LinkedList<CompletableFuture<Void>> waiters;
        int number;
        boolean blocked;

        RequestLimiter(int maximum) {
            waiters = new LinkedList<>();
            maxnumber = maximum;
        }

        synchronized void requestComplete() {
            number--;
            // don't unblock until number of requests has halved.
            if ((blocked && number <= maxnumber / 2) || (!blocked && waiters.size() > 0)) {
                int toRelease = Math.min(maxnumber - number, waiters.size());
                for (int i = 0; i < toRelease; i++) {
                    CompletableFuture<Void> f = waiters.remove();
                    number++;
                    f.complete(null);
                }
                blocked = number >= maxnumber;
            }
        }

        synchronized CompletableFuture<Void> whenOkToSend() {
            if (blocked || number + 1 >= maxnumber) {
                blocked = true;
                CompletableFuture<Void> r = new CompletableFuture<>();
                waiters.add(r);
                return r;
            } else {
                number++;
                return COMPLETED_FUTURE;
            }
        }
    }

    static <T> CompletableFuture<T> completedWithIOException(String message) {
        return CompletableFuture.failedFuture(new IOException(message));
    }

    static void check(boolean cond, Object... msg) {
        if (cond) return;
        StringBuilder sb = new StringBuilder();
        for (Object o : msg)
            sb.append(o);
        throw new RuntimeException(sb.toString());
    }

    static String bytesToHexString(byte[] bytes) {
        if (bytes == null) return "null";

        StringBuilder sb = new StringBuilder(bytes.length * 2);

        Formatter formatter = new Formatter(sb);
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }

        return sb.toString();
    }

}
