package org.consensusj.jsonrpc.cli

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.commons.cli.Options
import spock.lang.Specification

/**
 *
 */
class BaseJsonRpcToolSpec extends Specification {
    static final expectedURI = URI.create('http://localhost:8080/freebird')
    static final String[] dummyArgs = ['-url', expectedURI].toArray()

    def "Can create a Call object properly"() {
        given:
        BaseJsonRpcTool tool = new BaseJsonRpcTool() {
            public JsonRpcClientTool.Call call;

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
