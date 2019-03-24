package org.consensusj.jsonrpc.cli;

import org.apache.commons.cli.Options;
import org.consensusj.jsonrpc.JsonRpcException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import org.consensusj.jsonrpc.RpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for CLI commands that use Bitcoin RPC
 */
public abstract class CliCommand {
    private static Logger log = LoggerFactory.getLogger(CliCommand.class);
    static final String defaultproto = "http";
    static final String defaulthost = "127.0.0.1";
    static final int defaultport = 8332;
    static final String defaultfile = "/";

    protected CommandLine line;
    protected CommandLineParser parser;
    protected Options options;
    protected String name;
    protected String usage;
    protected HelpFormatter formatter = null;

    protected InputStream in;
    protected PrintWriter pwout;
    protected PrintWriter pwerr;

    protected boolean verbose = false;

    protected RpcClient client = null;

    protected CliCommand(String name, Options options, String[] args) {
        this(name, name, options, args);
    }

    protected CliCommand(String name, String usage, Options options, String[] args) {
        this.name = name;
        this.usage = usage;
        this.options = options;
        parser = new DefaultParser();
        try {
            this.line = this.parser.parse(options, args);
        } catch (ParseException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Can be used by subclasses to set a narrower type
     * @param client An instance of BitcoinClient or subclass
     */
    protected void setClient(RpcClient client) {
        this.client = client;
    }

    public void printHelp() {
        if (formatter == null) {
            formatter = new HelpFormatter();
            formatter.setLongOptPrefix("-");
        }
        int leftPad = 4;
        int descPad = 2;
        int helpWidth = 120;
        String header = "";
        String footer = "";
        formatter.printHelp(pwout, helpWidth, usage, header, options, leftPad, descPad, footer, false);
    }

    public void printError(String str) {
        pwout.println(str);
    }

    /**
     * Check Options and Arguments
     *
     * Override to customize behavior.
     *
     * @return status code
     */
    public int checkArgs() {
        if (line.hasOption("?")) {
            printHelp();
            // Return 1 so tool can exit
            return 1;
        }
        verbose = line.hasOption("v");
        if (verbose) {
            JavaLoggingSupport.setVerbose();
        }
        return 0;
    }

    public RpcClient getClient() {
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
            client = new RpcClient(uri, null, null);
        }
        return client;
    }

    /**
     * Initial a client
     * TODO: Add rpcwait option for non-Bitcoin JsonRPC???
     * @return status code
     */
    public int preflight() {
        getClient();
        return 0;
    }

    public int run() {
        return run(System.in, System.out, System.err);
    }

    public Integer run(InputStream in, PrintStream out, PrintStream err) {
        this.in = in;
        this.pwout = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), true);
        this.pwerr = new PrintWriter(new OutputStreamWriter(err, StandardCharsets.UTF_8), true);

        Integer status = checkArgs();
        if (status != 0) {
            return status;
        }

        status = preflight();
        if (status != 0) {
            return status;
        }

        try {
            status = runImpl();
        } catch (JsonRpcException e) {
            e.printStackTrace();
            status = 1;
        } catch (IOException e) {
            e.printStackTrace();
            status = 1;
        }
        return status;
    }
    
    /**
     * Implement in subclasses
     * @return status code
     */
    abstract protected Integer runImpl() throws IOException, JsonRpcException;
}
