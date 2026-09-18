package org.consensusj.jsonrpc.unix;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.consensusj.jsonrpc.DefaultRpcClient;
import org.consensusj.jsonrpc.JsonRpcMessage;
import org.consensusj.jsonrpc.JsonRpcRequest;
import org.consensusj.jsonrpc.JsonRpcResponse;
import org.consensusj.jsonrpc.JsonRpcStatusException;
import org.consensusj.jsonrpc.JsonRpcTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.net.URI;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

/**
 * Proof-of-concept UNIX domain socket JsonRpc Client (works with {@link UnixSocketEchoServer} and {@code lightningd}.)
 */
public class JsonRpcClientUnixSocket implements JsonRpcTransport<JavaType> {
    private static final Logger log = LoggerFactory.getLogger(JsonRpcClientUnixSocket.class);
    private final UnixDomainSocketAddress socketAddress;
    private final JsonRpcUnixSocketMapper socketMapper;

    public static void main(String[] args) throws IOException {
        boolean useEchoServer = true;
        Path socketPath = useEchoServer ? UnixSocketEchoServer.getTestPath() : getLightningRpcPath();
        UnixSocketTransportFactory factory  = new UnixSocketTransportFactory(socketPath);
        try (var client = new DefaultRpcClient(factory, JsonRpcMessage.Version.V2)) {
            JsonNode response = client.send("getinfo", JsonNode.class);
            System.out.println(response);
        }
    }

    public JsonRpcClientUnixSocket(Path socketPath, ObjectMapper mapper) {
        socketAddress = UnixDomainSocketAddress.of(socketPath);
        socketMapper = new JsonRpcUnixSocketMapper(mapper);

        // TODO: Delete on close:
        // Files.deleteIfExists(socketPath);
    }

    @Override
    public <R> JsonRpcResponse<R> sendRequestForResponse(JsonRpcRequest request, JavaType responseType) throws IOException, JsonRpcStatusException {
        SocketChannel channel = SocketChannel.open(StandardProtocolFamily.UNIX);
        // TODO: Use Selectable channel for async??
        channel.connect(socketAddress);
        ByteBuffer buffer = socketMapper.serializeRequest(request);
        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
        // TODO: Read response see https://www.baeldung.com/java-unix-domain-socket
        // See also: https://nipafx.dev/java-unix-domain-sockets/
        // And: https://www.linkedin.com/pulse/java-sockets-io-blocking-non-blocking-asynchronous-aliaksandr-liakh/
        JsonRpcResponse<R> responseJson = null;
        try {
            responseJson = socketMapper.readSocketResponse(request, responseType, channel);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        channel.close();
        return responseJson;
    }

    @Override
    public <R> CompletableFuture<JsonRpcResponse<R>> sendRequestForResponseAsync(JsonRpcRequest request, JavaType responseType) {
        return supplyAsync(() -> this.sendRequestForResponse(request, responseType));
    }

    @Override
    public URI getServerURI() {
        return socketAddress.getPath().toUri();
    }

    public static Path getLightningRpcPath() {
        return Path.of(System.getProperty("user.home")).resolve(".lightning/regtest/lightning-rpc");
    }
}
