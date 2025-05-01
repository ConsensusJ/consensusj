package org.consensusj.jsonrpc.cli;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.consensusj.jsonrpc.DefaultRpcClient;
import org.consensusj.jsonrpc.CompositeTrustManager;
import org.consensusj.jsonrpc.JsonRpcMessage;
import org.consensusj.jsonrpc.JsonRpcRequest;
import org.consensusj.jsonrpc.JsonRpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.spi.ToolProvider;

/**
 * An abstract base class for JsonRpcClientTool that implements ToolProvider and uses Apache Commons CLI
 */
public abstract class BaseJsonRpcTool implements JsonRpcClientTool, ToolProvider {
    private static final Logger log = LoggerFactory.getLogger(BaseJsonRpcTool.class);
    private static final String name = "jsonrpc";
    protected static final URI defaultUri = URI.create("http://localhost:8080/");
    protected final String usage ="usage string";
    protected final HelpFormatter formatter = new HelpFormatter();
    protected JsonRpcMessage.Version jsonRpcVersion = JsonRpcMessage.Version.V2;
    protected JsonRpcClientTool.OutputObject outputObject = OutputObject.RESULT;
    //protected JsonRpcClientTool.OutputFormat outputFormat = OutputFormat.JSON;
    protected final JsonRpcClientTool.OutputStyle outputStyle = OutputStyle.PRETTY;

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

    public CommonsCLICall createCall(PrintWriter out, PrintWriter err, String... args) {
        return new CommonsCLICall(this, out, err, args);
    }

    public CommonsCLICall createCall(PrintStream out, PrintStream err, String... args) {
        return createCall(writerFromStream(out), writerFromStream(err), args);
    }

    private PrintWriter writerFromStream(PrintStream stream) {
        return new PrintWriter(new OutputStreamWriter(stream, StandardCharsets.UTF_8), true);

    }

    @Override
    public int run(PrintWriter out, PrintWriter err, String... args) {
        try {
            CommonsCLICall call = createCall(out, err, args);
            run(call);
        } catch (ToolException e) {
            return e.resultCode;
        } catch (Exception e) {
            throw new RuntimeException((e));
        }
        return 0;
    }

    public void run(CommonsCLICall call) {
        log.info("argv[0]: {}", executableName().orElse("unknown"));
        List<String> args = call.line.getArgList();
        if (args.isEmpty()) {
            printError(call, "jsonrpc method required");
            printHelp(call.err, usage);
            throw new ToolException(1, "jsonrpc method required");
        }
        if (call.line.hasOption("response")) {
            // Print full JsonRpcResponse as output
            outputObject = OutputObject.RESPONSE;
        }
        if (call.line.hasOption("V1")) {
            jsonRpcVersion = JsonRpcMessage.Version.V1;
        }
        SSLContext sslContext = sslContext(call.line);
        String resultForPrinting;
        try (DefaultRpcClient client = call.rpcClient(sslContext)) {
            CliParameterParser parser = new CliParameterParser(jsonRpcVersion, client.getMapper());
            JsonRpcRequest request = parser.parse(args);
            JsonRpcResponse<JsonNode> response;
            try {
                response = client.sendRequestForResponseAsync(request).get();
            } catch (ExecutionException ee) {
                log.error("send execution exception: ", ee);
                Throwable t = ee.getCause() != null ? ee.getCause() : ee;
                throw new ToolException(1, t.getMessage());
            } catch (InterruptedException e) {
                log.error("send interrupted exception: ", e);
                throw new ToolException(1, e.getMessage());
            }
            resultForPrinting = formatResponse(response, client.getMapper());
        }
        call.out.println(resultForPrinting);
    }

    SSLContext sslContext(CommandLine line) {
        SSLContext sslContext;
        if (line.hasOption("add-truststore")) {
            // Create SSL sockets using additional truststore and CompositeTrustManager
            String trustStorePathString = line.getOptionValue("add-truststore");
            Path trustStorePath = Path.of(trustStorePathString);
            try {
                sslContext = CompositeTrustManager.getCompositeSSLContext(trustStorePath);
            } catch (NoSuchAlgorithmException | KeyManagementException | FileNotFoundException e) {
                throw new ToolException(1, e.getMessage());
            }
        } else if (line.hasOption("alt-truststore")) {
            // Create SSL sockets using alternate truststore
            String trustStorePathString = line.getOptionValue("alt-truststore");
            Path trustStorePath = Path.of(trustStorePathString);
            try {
                sslContext = CompositeTrustManager.getAlternateSSLContext(trustStorePath);
            } catch (NoSuchAlgorithmException | KeyManagementException | CertificateException | KeyStoreException | IOException e) {
                throw new ToolException(1, e.getMessage());
            }
        } else {
            // Otherwise, use the default SSLContext
            try {
                sslContext = SSLContext.getDefault();
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }
        return sslContext;
    }

    private String formatResponse(JsonRpcResponse<?> response, ObjectMapper mapper) {
        return (response.getResult() == null || response.getError() != null || outputObject == OutputObject.RESPONSE)
            ? mapper.valueToTree(response).toPrettyString() // Pretty print the entire response as JSON
            : switch (response.getResult()) {
                case TextNode tn    -> tn.asText();         // Remove the surrounding quotes and don't print `\n` for newlines
                case JsonNode jn    -> outputStyle == OutputStyle.PRETTY ? jn.toPrettyString() : jn.toString();
                case Object obj     -> obj.toString();
            };
    }

    public void printHelp(PrintWriter pw, String usage) {
        int leftPad = 4;
        int descPad = 2;
        int helpWidth = 120;
        String header = "";
        String footer = "";
        formatter.printHelp(pw, helpWidth, usage, header, options(), leftPad, descPad, footer, false);
    }

    public void printError(CommonsCLICall call, String str) {
        call.err.println(str);
    }

    public static class CommonsCLICall  {
        public final PrintWriter out;
        public final PrintWriter err;
        public final String[] args;
        protected final BaseJsonRpcTool rpcTool;
        public final CommandLine line;
        public final boolean verbose;
        private DefaultRpcClient client;

        public CommonsCLICall(BaseJsonRpcTool parentTool, PrintWriter out, PrintWriter err, String[] args) {
            this.out = out;
            this.err = err;
            this.args = args;
            this.rpcTool = parentTool;
            CommandLineParser parser = new DefaultParser();
            try {
                this.line = parser.parse(rpcTool.options(), args);
            } catch (ParseException e) {
                rpcTool.printError(this, e.getMessage());
                rpcTool.printHelp(this.err, rpcTool.usage());   // print help to stderr
                throw new JsonRpcClientTool.ToolException(1, "Parser error");
            }
            // Logging should be (one of?) the first thing(s) configured from command-line options
            Level level = parseLogLevel(line);
            JavaLoggingSupport.setLogLevel(level);
            verbose = line.hasOption("v");

            boolean help = line.hasOption("?");
            if (help) {
                rpcTool.printHelp(this.out, rpcTool.usage());  // Print help to stdout
                throw new JsonRpcClientTool.ToolException(0, "Help Option was chosen");
            }
            // TODO: Add rpcwait option for non-Bitcoin JsonRPC???
        }

        Level parseLogLevel(CommandLine line) {
            Level level = Level.WARNING;    // Default level
            boolean verbose = line.hasOption("v");
            if (verbose) {
                level = Level.INFO;          // Verbose enables "INFO" (for now)
            }
            // "-log" overrides "-v"
            boolean hasLogLevel = line.hasOption("log");
            if (hasLogLevel) {
                String intLevel = line.getOptionValue("log");
                level = switch (intLevel) {
                    case "0" -> Level.OFF;
                    case "1" -> Level.SEVERE;
                    case "2" -> Level.WARNING;
                    case "3" -> Level.INFO;
                    case "4" -> Level.FINE;
                    case "5" -> Level.ALL;
                    default -> throw new IllegalStateException("Unexpected value: " + intLevel);
                };
            }
            return level;
        }

        public DefaultRpcClient rpcClient(SSLContext sslContext) {
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
                    uri = defaultUri;
                }
                String rpcUser = null;
                String rpcPassword = null;
                String rawUserInfo = uri.getRawUserInfo();
                if (rawUserInfo != null) {
                    String[] split = rawUserInfo.split(":");
                    rpcUser = split[0];
                    rpcPassword = split[1];
                }
                client = new DefaultRpcClient(sslContext, rpcTool.jsonRpcVersion, uri, rpcUser, rpcPassword);
            }
            return client;
        }

        public DefaultRpcClient rpcClient() {
            SSLContext sslContext;
            try {
                sslContext = SSLContext.getDefault();
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
            return rpcClient(sslContext);
        }
    }

    static Optional<String> executableName () {
        return ProcessHandle.current().info()
                .command()
                .map(c -> c.substring(c.lastIndexOf('/') + 1));
    }
}
