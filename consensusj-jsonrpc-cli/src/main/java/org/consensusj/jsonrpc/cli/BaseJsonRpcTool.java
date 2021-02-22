package org.consensusj.jsonrpc.cli;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.consensusj.jsonrpc.JsonRpcException;
import org.consensusj.jsonrpc.RpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * An abstract base class for JsonRpcClientTool that uses Apache Commons CLI
 */
public abstract class BaseJsonRpcTool implements JsonRpcClientTool {
    private static final Logger log = LoggerFactory.getLogger(BaseJsonRpcTool.class);
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

    public String usage() {
        return usage;
    }

    abstract public Options options();

    @Override
    public CommonsCLICall createCall(PrintWriter out, PrintWriter err, String... args) {
        return new CommonsCLICall(this, out, err, args);
    }

    @Override
    public void run(Call call) {
        run((CommonsCLICall) call);
    }
    
    public void run(CommonsCLICall call) {
        List<String> args = call.line.getArgList();
        if (args.size() == 0) {
            printError(call,"jsonrpc method required");
            printHelp(call, usage);
            throw new ToolException(1, "jsonrpc method required");
        }
        String method = args.get(0);
        args.remove(0); // remove method from list
        List<Object> typedArgs = convertParameters(method, args);
        Object result;
        try {
            result = call.rpcClient().send(method, typedArgs);
        } catch (JsonRpcException e) {
            e.printStackTrace();
            throw new ToolException(1, e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            throw new ToolException(1, e.getMessage());
        }
        String resultForPrinting = formatResult(result);
        call.out.println(resultForPrinting);
    }

    private String formatResult(Object result) {
        String string;
        if (result == null) {
            log.info("result is null");
            string = "null";
        } else if (result instanceof JsonNode) {
            log.info("result instanceof JsonNode");
            string = ((JsonNode) result).toPrettyString();
        } else {
            log.info("result class is: {}", result.getClass());
            string = result.toString();
        }
        return string;
    }

    /**
     * Convert params from strings to Java types that will map to correct JSON types
     *
     * TODO: Make this better and complete
     *
     * @param method the JSON-RPC method
     * @param params Params with String type
     * @return Params with correct Java types for JSON
     */
    protected List<Object> convertParameters(String method, List<String> params) {
        List<Object> converted = new ArrayList<>();
        for (String param : params) {
            converted.add(convertParam(param));
        }
        return converted;
    }

    /**
     * Convert a single param from a command-line option {@code String} to a type more appropriate
     * for Jackson/JSON-RPC.
     *
     * @param param A string parameter to convert
     * @return The input parameter, possibly converted to a different type
     */
    protected Object convertParam(String param) {
        Object result;
        Optional<Long> l = toLong(param);
        if (l.isPresent()) {
            // If the param was a valid Long, return a Long
            result = l.get();
        } else {
            // Else, return a Boolean or String
            switch (param) {
                case "false":
                    result = Boolean.FALSE;
                    break;
                case "true":
                    result = Boolean.TRUE;
                    break;
                default:
                    result = param;
            }
        }
        return result;
    }

    // Convert to Long (if possible)
    protected static Optional<Long> toLong(String strNum) {
        try {
            return Optional.of(Long.parseLong(strNum));
        } catch (NumberFormatException nfe) {
            return Optional.empty();
        }
    }

    public void printHelp(Call call, String usage) {
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


    public static class CommonsCLICall extends JsonRpcClientTool.Call {
        protected final BaseJsonRpcTool rpcTool;
        public final CommandLine line;
        public final boolean verbose;
        private RpcClient client;

        public CommonsCLICall(BaseJsonRpcTool parentTool, PrintWriter out, PrintWriter err, String[] args) {
            super(out, err, args);
            this.rpcTool = parentTool;
            CommandLineParser parser = new DefaultParser();
            try {
                this.line = parser.parse(rpcTool.options(), args);
            } catch (ParseException e) {
                throw new JsonRpcClientTool.ToolException(1, "Parser error");
            }
            if (line.hasOption("?")) {
                rpcTool.printHelp(this, rpcTool.usage());
                throw new JsonRpcClientTool.ToolException(0, "Help Option was chosen");
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
