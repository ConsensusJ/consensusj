package org.consensusj.jsonrpc.cli;

import org.consensusj.jsonrpc.RpcClient;

import javax.net.ssl.SSLSocketFactory;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.spi.ToolProvider;

/**
 * An implementation of ToolProvider that uses an RpcClient and runs in two steps
 * 1. Parse arguments and initialize RPC Client
 * 2. Run the command
 */
interface JsonRpcClientTool extends ToolProvider {
    @Override
    default int run(PrintWriter out, PrintWriter err, String... args) {
        try {
            Call call = createCall(out, err, args);
            run(call);
        } catch (ToolException e) {
            return e.resultCode;
        } catch (Exception e) {
            throw new RuntimeException((e));
        }
        return 0;
    }

    Call createCall(PrintWriter out, PrintWriter err, String... args);
    default Call createCall(PrintStream out, PrintStream err, String... args) {
        return createCall(writerFromStream(out), writerFromStream(err), args);
    }

    default PrintWriter writerFromStream(PrintStream stream) {
        return new PrintWriter(new OutputStreamWriter(stream, StandardCharsets.UTF_8), true);

    }

    void run(Call call);

    abstract class Call {
        public final PrintWriter out;
        public final PrintWriter err;
        public final String[] args;

        public Call(PrintWriter out, PrintWriter err, String[] args) {
            this.out = out;
            this.err = err;
            this.args = args;
        }
        
        abstract public RpcClient rpcClient();
        abstract public RpcClient rpcClient(SSLSocketFactory sslSocketFactory);
    }
    
    class ToolException extends RuntimeException {
        public final int resultCode;

        public ToolException(int resultCode, String resultMessage) {
            super(resultMessage);
            this.resultCode = resultCode;
        }
    }

    enum OutputObject {
        RESPONSE,
        RESULT
    }

    enum OutputFormat {
        JSON,
        JAVA
    }

    enum OutputStyle {
        DEFAULT,
        PRETTY
    }
}
