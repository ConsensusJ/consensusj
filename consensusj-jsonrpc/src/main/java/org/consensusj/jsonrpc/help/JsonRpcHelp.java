package org.consensusj.jsonrpc.help;

public class JsonRpcHelp {
    private final String summary;
    private final String detail;

    public JsonRpcHelp(String summary, String detail) {
        this.summary = summary;
        this.detail = detail;
    }

    public String summary() {
        return summary;
    }

    public String detail() {
        return detail;
    }
}
