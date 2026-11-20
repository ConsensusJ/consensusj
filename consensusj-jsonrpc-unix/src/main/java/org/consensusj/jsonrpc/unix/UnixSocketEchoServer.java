package org.consensusj.jsonrpc.unix;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.consensusj.jsonrpc.JsonRpcError;
import org.consensusj.jsonrpc.JsonRpcRequest;
import org.consensusj.jsonrpc.JsonRpcResponse;

import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 *  For possible implementation with CompletableFuture,
 *  See <a href="https://github.com/IBM/java-async-util/blob/master/asyncutil/src/test/java/com/ibm/asyncutil/examples/nio/nio.md#nio-bridge">...</a>
 *
 */
public class UnixSocketEchoServer {
    private final UnixDomainSocketAddress socketAddress;
    private final JsonRpcUnixSocketMapper socketMapper;

    public static void main(String[] args) throws IOException, InterruptedException {
        Path socketPath = getTestPath();
        Files.deleteIfExists(socketPath);
        UnixSocketEchoServer server = new UnixSocketEchoServer(socketPath);
        server.run();
    }

    public UnixSocketEchoServer(Path socketPath) {
        socketAddress = UnixDomainSocketAddress.of(socketPath);
        socketMapper = new JsonRpcUnixSocketMapper(getMapper());

        // TODO: Delete on close:
        // Files.deleteIfExists(socketPath);
    }

    public void run() throws IOException, InterruptedException {
        ServerSocketChannel serverChannel = ServerSocketChannel.open(StandardProtocolFamily.UNIX);
        serverChannel.bind(socketAddress);
        SocketChannel channel = serverChannel.accept();
        while (true) {
            var optMessage = socketMapper.readSocketMessage(channel);
            if (optMessage.isPresent()) {
                processMessage(channel, optMessage.get());
            }
            channel.close();
            channel = serverChannel.accept();
            Thread.sleep(100);
        }
    }

    private void processMessage(SocketChannel channel, String message) throws IOException {
        System.out.printf("[Client message] %s\n", message);
        JsonRpcRequest request;
        try {
            request = socketMapper.deserializeRequest(message);
            System.out.println("Got " + request.getMethod() + " request");
            JsonRpcResponse<?> response = switch (request.getMethod()) {
                case "getinfo" -> new JsonRpcResponse<>(request, "Echo GETINFO Response");
                case "help" -> new JsonRpcResponse<>(request, "echo message (TBD)\ngetinfo\nhelp\nstop (TBD)\n");
                default -> new JsonRpcResponse<>(request, JsonRpcError.of(JsonRpcError.Error.METHOD_NOT_FOUND));
            };
            ByteBuffer buffer = socketMapper.serializeResponse(response);
            while (buffer.hasRemaining()) {
                channel.write(buffer);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    static ObjectMapper getMapper() {
        var mapper = new ObjectMapper();
        // TODO: Provide external API to configure FAIL_ON_UNKNOWN_PROPERTIES
        // TODO: Remove "ignore unknown" annotations on various POJOs that we've defined.
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    static Path getTestPath() {
        return Path.of(System.getProperty("user.home")).resolve("consensusj.socket");
    }
}
