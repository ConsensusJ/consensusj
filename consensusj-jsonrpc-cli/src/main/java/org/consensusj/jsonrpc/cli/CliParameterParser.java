package org.consensusj.jsonrpc.cli;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.consensusj.jsonrpc.JsonRpcMessage;
import org.consensusj.jsonrpc.JsonRpcRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Parser for command-line JSON-RPC method and parameters
 */
public class CliParameterParser {
    private static final Logger log = LoggerFactory.getLogger(CliParameterParser.class);
    private final ObjectMapper mapper;
    private final JsonRpcMessage.Version jsonRpcVersion;

    public CliParameterParser(JsonRpcMessage.Version jsonRpcVersion, ObjectMapper mapper) {
        this.jsonRpcVersion = jsonRpcVersion;
        this.mapper = mapper;
    }

    public CliParameterParser(ObjectMapper mapper) {
        this(JsonRpcMessage.Version.V2, mapper);
    }

    /**
     * Parse command-line arguments (after options, etc. have been processed/removed)
     * @param args the JSON-RPC method name followed by JSON-RPC parameters as JSON strings
     * @return A request ready for serialization and sending
     */
    public JsonRpcRequest parse(List<String> args) {
        String method = args.get(0);
        List<String> unparsedParams = args.stream().skip(1).toList();
        List<CliParameter> convertedArgs = parseParamList(method, unparsedParams);
        if (convertedArgs.stream().anyMatch(CliParameter::invalid)) {
            throw new JsonRpcClientTool.ToolException(1, "One or more invalid JSON-RPC parameters");
        }
        List<Object> typedArgs = convertedArgs.stream()
                .flatMap(CliParameter::stream)
                .toList();
        return new JsonRpcRequest(jsonRpcVersion, method, typedArgs);
    }

    /**
     * Convert params from strings to Java types that will map to correct JSON types
     *
     * @param method the JSON-RPC method
     * @param params Params with String type
     * @return Params with correct Java types for JSON
     */
    private List<CliParameter> parseParamList(String method, List<String> params) {
        return params.stream()
                .map(this::parseParam)
                .peek(this::logInvalid)
                .toList();
    }

    /**
     * Convert a single param from a command-line option {@code String} to a type more appropriate
     * for serialization by Jackson/JSON-RPC.
     *
     * @param param A string parameter to convert
     * @return A wrapped Java Object that can serialize to JSON
     */
    private CliParameter parseParam(String param) {
        CliParameter mappedParam = mapParam(param);
        return mappedParam.valid()
                ? mappedParam                         // Param was valid JSON
                : CliParameter.valid(param, param);   // Handle string with no quotes as {@code String}
    }

    /**
     * Map a single param from a command-line {@code String} to a {@link JsonNode} wrapped
     * in a {@link CliParameter}.
     *
     * @param param A string parameter to convert
     * @return A wrapped Java Object that can serialize to JSON (or a wrapped exception)
     */
    private CliParameter mapParam(String param) {
        return CliParameter.parse(param, string -> mapper.readValue(param, JsonNode.class));
    }

    private void logInvalid(CliParameter param) {
        if (param instanceof CliParameter.Invalid i) {
            log.error("Invalid parameter", i.error());
        }
    }
}
