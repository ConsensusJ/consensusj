package org.consensusj.jsonrpc.unix;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.consensusj.jsonrpc.DefaultRpcClient;
import org.consensusj.jsonrpc.JsonRpcTransport;

import java.nio.file.Path;

/**
 * Factory for creating {@link JsonRpcClientUnixSocket} from an {@link ObjectMapper}. The
 * factory instance is created with the {@link Path} to the desired socket.
 */
public class UnixSocketTransportFactory implements DefaultRpcClient.TransportFactory {
    private final Path socketPath;

    /**
     * Create a factory instance for a given Unix socket path.
     * @param socketPath Path to the desired socket.
     */
    public UnixSocketTransportFactory(Path socketPath) {
        this.socketPath = socketPath;
    }

    @Override
    public JsonRpcTransport<JavaType> create(ObjectMapper mapper) {
        return new JsonRpcClientUnixSocket(socketPath, mapper);
    }
}
