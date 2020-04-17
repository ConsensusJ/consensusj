package org.consensusj.jsonrpc.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.consensusj.jsonrpc.JsonRpcException;
import org.consensusj.jsonrpc.RpcClient;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * An abstract base class for JsonRpcClientTool that uses Apache Commons CLI
 */
public abstract class BaseJsonRpcTool implements JsonRpcClientTool {
    private final static String name = "jsonrpc";
    protected final String usage ="usage string";
    protected final HelpFormatter formatter = new HelpFormatter();


    public BaseJsonRpcTool() {
        formatter.setLongOptPrefix("-");
    }

    @Override
    public String name() {
        return name;
    }

    abstract public Options options();

    @Override
    public CommonsCLICall createCall(PrintWriter out, PrintWriter err, String... args) {
        return new CommonsCLICall(out, err, args);
    }

    @Override
    public void run(Call call) {
        run((CommonsCLICall) call);
    }
    
    public void run(CommonsCLICall call) {
        List<String> args = call.line.getArgList();
        if (args.size() == 0) {
            printError(call,"jsonrpc method required");
            printHelp(call);
            throw new ToolException(1, "jsonrpc method required");
        }
        String method = args.get(0);
        args.remove(0); // remove method from list
        List<Object> typedArgs = convertParameters(method, args);
        Object result = null;
        try {
            result = call.rpcClient().send(method, typedArgs);
        } catch (JsonRpcException e) {
            e.printStackTrace();
            throw new ToolException(1, e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            throw new ToolException(1, e.getMessage());
        }
        if (result != null) {
            call.out.println(result.toString());
        }
    }

    protected abstract List<Object> convertParameters(String method, List<String> params);

    public void printHelp(Call call) {
        int leftPad = 4;
        int descPad = 2;
        int helpWidth = 120;
        String header = "";
        String footer = "";
        formatter.printHelp(call.err, helpWidth, usage, header, options(), leftPad, descPad, footer, false);
    }

    public void printError(Call call, String str) {
        call.err.println(str);
    }


    public class CommonsCLICall extends JsonRpcClientTool.Call {
        public final CommandLine line;
        public final boolean verbose;
        private RpcClient client;

        public CommonsCLICall(PrintWriter out, PrintWriter err, String[] args) {
            super(out, err, args);
            CommandLineParser parser = new DefaultParser();
            try {
                this.line = parser.parse(options(), args);
            } catch (ParseException e) {
                throw new JsonRpcClientTool.ToolException(1, "Parser error");
            }
            if (line.hasOption("?")) {
                printHelp(this);
                throw new JsonRpcClientTool.ToolException(1, "Help Option was chosen");
            }
            verbose = line.hasOption("v");
            if (verbose) {
                JavaLoggingSupport.setVerbose();
            }
            // TODO: Add rpcwait option for non-Bitcoin JsonRPC???
        }

        @Override
        public RpcClient rpcClient() {
            if (client == null) {
                URI uri;
                String urlString;
                if ((urlString = line.getOptionValue("url")) != null ) {
                    try {
                        uri = new URI(urlString);
                    } catch (URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    uri = URI.create("http://localhost:8080/");  // Hardcoded for now
                }
                String rpcUser = null;
                String rpcPassword = null;
                String rawUserInfo = uri.getRawUserInfo();
                if (rawUserInfo != null) {
                    String[] split = rawUserInfo.split(":");
                    rpcUser = split[0];
                    rpcPassword = split[1];
                }
                client = new RpcClient(uri, rpcUser, rpcPassword);
            }
            return client;
        }
    }
}
