package org.consensusj.jsonrpc.unix;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.consensusj.jsonrpc.JsonRpcRequest;
import org.consensusj.jsonrpc.JsonRpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Optional;

/**
 * Common code for JsonRpc UNIX Sockets clients and servers
 */
public class JsonRpcUnixSocketMapper {
    private static final Logger log = LoggerFactory.getLogger(JsonRpcUnixSocketMapper.class);

    private final ObjectMapper mapper;

    /**
     * @param mapper Jackson mapper
     */
    public JsonRpcUnixSocketMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public <R> JsonRpcResponse<R> readSocketResponse(JsonRpcRequest request, JavaType responseType, SocketChannel channel) throws IOException, InterruptedException {
        Optional<String> resp = Optional.empty();
        while (resp.isEmpty()) {
            Thread.sleep(100);
            resp = readSocketMessage(channel);
            resp.ifPresent(System.out::println);
        }
        JsonRpcResponse<R> responseJson = deserializeResponse(responseType, resp.orElseThrow());
        return responseJson;
    }

    public Optional<String> readSocketMessage(SocketChannel channel) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(10240);
        int bytesRead = channel.read(buffer);
        if (bytesRead < 0)
            return Optional.empty();

        byte[] bytes = new byte[bytesRead];
        buffer.flip();
        buffer.get(bytes);
        String message = new String(bytes);
        return Optional.of(message);
    }

    public <R> JsonRpcResponse<R> deserializeResponse(JavaType responseType, String s) throws JsonProcessingException {
        JsonRpcResponse<R> responseJson;
        log.debug("Response String: {}", s);
        try {
            responseJson = mapper.readValue(s, responseType);
        } catch (JsonProcessingException e) {
            log.error("JsonProcessingException: ", e);
            // TODO: Map to some kind of JsonRPC exception similar to JsonRPCStatusException
            throw e;
        }
        return responseJson;
    }

    public JsonRpcRequest deserializeRequest(String s) throws JsonProcessingException {
        JsonRpcRequest requestJson;
        log.debug("Request String: {}", s);
        try {
            requestJson = mapper.readValue(s, JsonRpcRequest.class);
        } catch (JsonProcessingException e) {
            log.error("JsonProcessingException: ", e);
            // TODO: Map to some kind of JsonRPC exception similar to JsonRPCStatusException
            throw e;
        }
        return requestJson;
    }

    public ByteBuffer serializeRequest(JsonRpcRequest request) throws JsonProcessingException {
        String message = mapper.writeValueAsString(request);
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.clear();
        buffer.put(message.getBytes());
        buffer.flip();
        return buffer;
    }

    public <R> ByteBuffer serializeResponse(JsonRpcResponse<R> response) throws JsonProcessingException {
        String message = mapper.writeValueAsString(response);
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.clear();
        buffer.put(message.getBytes());
        buffer.flip();
        return buffer;
    }
}
