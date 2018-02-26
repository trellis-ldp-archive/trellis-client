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

/**
 * LdpClientException.
 *
 * @author christopher-johnson
 */
public class LdpClientException extends Exception {
    private static final long serialVersionUID = -2402999298460159803L;

    /**
     * Create a new LdpClientException.
     */
    public LdpClientException() {
        super();
    }

    /**
     * Create a new LdpClientException with a custom message.
     *
     * @param message the message
     */
    public LdpClientException(final String message) {
        super(message);
    }

    /**
     * Create a new LdpClientException with a custom message and known cause.
     *
     * @param message the message
     * @param cause the cause
     */
    public LdpClientException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Create a new LdpClientException with a known cause.
     *
     * @param cause the cause
     */
    public LdpClientException(final Throwable cause) {
        super(cause);
    }
}
