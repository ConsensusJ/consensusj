package org.consensusj.jsonrpc.unix;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import org.consensusj.jsonrpc.AbstractRpcClient;
import org.consensusj.jsonrpc.JsonRpcMessage;
import org.consensusj.jsonrpc.JsonRpcRequest;
import org.consensusj.jsonrpc.JsonRpcResponse;
import org.consensusj.jsonrpc.JsonRpcStatusException;

import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.net.URI;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 *
 */
public class JsonRpcClientUnixSocket extends AbstractRpcClient  {
    private final UnixDomainSocketAddress socketAddress;

    public static void main(String[] args) throws IOException {
        Path socketPath = UnixSocketEchoServer.getTestPath();
        JsonRpcClientUnixSocket socket  = new JsonRpcClientUnixSocket(socketPath);
        JsonRpcRequest request = new JsonRpcRequest("echo", List.of("hello"));
        String response = socket.send("echo", String.class, "hello" );
    }

    public JsonRpcClientUnixSocket(Path socketPath) {
        super(JsonRpcMessage.Version.V2);
        socketAddress = UnixDomainSocketAddress.of(socketPath);
        // TODO: Delete on close:
        // Files.deleteIfExists(socketPath);
    }
    
    @Override
    public <R> JsonRpcResponse<R> sendRequestForResponse(JsonRpcRequest request, JavaType responseType) throws IOException, JsonRpcStatusException {
        SocketChannel channel = SocketChannel.open(StandardProtocolFamily.UNIX);
        // TODO: Use Selectable channel for async??
        channel.connect(socketAddress);
        ByteBuffer buffer = serialize(request);
        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
        // TODO: Read response see https://www.baeldung.com/java-unix-domain-socket
        // See also: https://nipafx.dev/java-unix-domain-sockets/
        // And: https://www.linkedin.com/pulse/java-sockets-io-blocking-non-blocking-asynchronous-aliaksandr-liakh/
        JsonRpcResponse<R> responseJson = readSocketResponse(request, responseType, channel);
        channel.close();
        return responseJson;
    }

    private <R> JsonRpcResponse<R> readSocketResponse(JsonRpcRequest request, JavaType responseType, SocketChannel channel) {
        JsonRpcResponse<R> responseJson = new JsonRpcResponse<R>(request, (R) "String result");
        return responseJson;
    }

    private Optional<String> readSocketMessage(SocketChannel channel) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int bytesRead = channel.read(buffer);
        if (bytesRead < 0)
            return Optional.empty();

        byte[] bytes = new byte[bytesRead];
        buffer.flip();
        buffer.get(bytes);
        String message = new String(bytes);
        return Optional.of(message);
    }

    private ByteBuffer serialize(JsonRpcRequest request) throws JsonProcessingException {
        String message = mapper.writeValueAsString(request);
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.clear();
        buffer.put(message.getBytes());
        buffer.flip();
        return buffer;
    }

    @Override
    public URI getServerURI() {
        return null;
    }
}
