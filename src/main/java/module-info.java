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
module cool.pandora.ldpclient {
    exports cool.pandora.ldpclient;
    requires org.apache.commons.rdf.api;
    requires org.apache.commons.rdf.jena;
    requires jena.osgi;
    requires slf4j.api;
    requires java.ws.rs;
    requires org.trellisldp.vocabulary;
    requires jdk.incubator.httpclient;
}