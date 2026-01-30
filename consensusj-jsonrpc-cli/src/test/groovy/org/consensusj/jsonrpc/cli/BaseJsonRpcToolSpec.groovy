/*
 * Copyright 2014-2026 ConsensusJ Developers.
 *
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
package org.consensusj.jsonrpc.cli

import org.apache.commons.cli.Options
import spock.lang.Specification

class BaseJsonRpcToolSpec extends Specification {
    static final expectedURI = URI.create('http://localhost:8080/freebird')
    static final String[] dummyArgs = ['-url', expectedURI].toArray()

    def "Can create a Call object properly"() {
        given:
        BaseJsonRpcTool tool = new BaseJsonRpcTool() {
            public BaseJsonRpcTool.CommonsCLICall call;

            @Override
            Options options() {
                return new JsonRpcToolOptions()
            }

            @Override
            void run(BaseJsonRpcTool.CommonsCLICall call) {
                this.call = call;
            }
        }

        when:
        def call = tool.createCall(System.out, System.err, dummyArgs)
        def client = call.rpcClient();

        then:
        call.out instanceof PrintWriter
        call.err instanceof PrintWriter
        call.args == dummyArgs
        client.getServerURI() == expectedURI
        call instanceof BaseJsonRpcTool.CommonsCLICall
    }
}
