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

import static javax.ws.rs.core.HttpHeaders.ACCEPT;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static jdk.incubator.http.HttpRequest.BodyPublisher.fromString;
import static jdk.incubator.http.HttpResponse.BodyHandler.asString;
import static jdk.incubator.http.HttpResponse.BodyHandler.discard;
import static org.apache.jena.riot.WebContent.contentTypeSPARQLQuery;
import static org.apache.jena.riot.WebContent.contentTypeSPARQLUpdate;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jdk.incubator.http.HttpClient;
import jdk.incubator.http.HttpRequest;
import jdk.incubator.http.HttpResponse;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Test;

/**
 * FusekiConcurrencyTest.
 *
 * @author christopher-johnson
 */
public class FusekiConcurrencyTest {
    private static HttpClient client = getClient();
    private static String baseUrl = "https://localhost:8443/fuseki/trellis";

    private static HttpClient getClient() {
        final ExecutorService exec = Executors.newCachedThreadPool();
        return HttpClient.newBuilder().executor(exec).build();
    }

    @Test
    void testJoiningCompletableFuturePostGetUpdate() throws Exception {
        URI uri = new URI(baseUrl + "/update");
        final List<String> list = new ArrayList<>();
        final int LOOPS = 400;
        for (int i = 0; i < LOOPS; i++) {
            final String pid = "urn:" + UUID.randomUUID().toString();
            final String update = "INSERT DATA { <" + pid + "> <urn:x:p> <urn:x:o>}";
            list.add(update);
        }
        joiningCompletableFuturePostGetUpdate(uri, list);
    }

    private void joiningCompletableFuturePostGetUpdate(URI target, final List<String> updates) throws Exception {
        final CompletableFuture<?>[] posts = updates.stream().map((u) -> client.sendAsync(
                HttpRequest.newBuilder(target).headers(CONTENT_TYPE, contentTypeSPARQLUpdate).POST(
                        fromString(u)).build(), discard(null)).thenApply(HttpResponse::statusCode)).toArray(
                CompletableFuture<?>[]::new);

        final int REQUESTS = 400;
        final CompletableFuture[] gets = new CompletableFuture<?>[REQUESTS];
        final String selectQuery = "?query=SELECT%20*%20WHERE%20%7B%3Fs%20%3Fp%20%3Fo%7D";
        final URI uri = new URI(baseUrl + selectQuery);

        for (int i = 0; i < REQUESTS; i++) {
            gets[i] = client.sendAsync(
                    HttpRequest.newBuilder(uri).headers(CONTENT_TYPE, contentTypeSPARQLQuery, ACCEPT,
                            "application/sparql-results+json").GET().build(), asString());
        }
        final CompletableFuture[] everything = ArrayUtils.addAll(gets, posts);
        CompletableFuture.allOf(everything).join();
    }
}
