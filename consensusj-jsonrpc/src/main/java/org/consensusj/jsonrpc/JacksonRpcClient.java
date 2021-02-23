package org.consensusj.jsonrpc;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Methods for creating Jackson's {@link JavaType} for a Jackson-based implementation
 * of {link @JsonRpcClient}.
 */
public interface JacksonRpcClient {

    ObjectMapper getMapper();

    default JavaType responseTypeFor(JavaType resultType) {
        return getMapper().getTypeFactory().
                constructParametricType(JsonRpcResponse.class, resultType);
    }

    default JavaType responseTypeFor(Class<?> resultType) {
        return getMapper().getTypeFactory().
                constructParametricType(JsonRpcResponse.class, resultType);
    }
}
