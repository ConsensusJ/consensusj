package org.consensusj.jsonrpc.unix;

import org.consensusj.jsonrpc.JsonRpcMessage;

import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 *
 */
public class UnixSocketEchoServer {
    private final UnixDomainSocketAddress socketAddress;

    public static void main(String[] args) throws IOException, InterruptedException {
        Path socketPath = getTestPath();
        Files.deleteIfExists(socketPath);
        UnixSocketEchoServer server = new UnixSocketEchoServer(socketPath);
        server.run();
    }

    public UnixSocketEchoServer(Path socketPath) {
        socketAddress = UnixDomainSocketAddress.of(socketPath);
        // TODO: Delete on close:
        // Files.deleteIfExists(socketPath);
    }

    public void run() throws IOException, InterruptedException {
        ServerSocketChannel serverChannel = ServerSocketChannel.open(StandardProtocolFamily.UNIX);
        serverChannel.bind(socketAddress);
        SocketChannel channel = serverChannel.accept();
        while (true) {
            readSocketMessage(channel)
                    .ifPresent(message -> System.out.printf("[Client message] %s\n", message));
            channel.close();
            channel = serverChannel.accept();
            Thread.sleep(100);
        }
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

    static Path getTestPath() {
        return Path.of(System.getProperty("user.home")).resolve("consensusj.socket");
    }

}
