package org.consensusj.jsonrpc.internal;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * If string is an integer (valid long) serialize as Number, else serialize as String
 */
public class NumberStringSerializer extends JsonSerializer<String> {
    private static Pattern numberRegEx = Pattern.compile("^-?\\d+$");

    @Override
    public void serialize(String numberOrString, JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider)
            throws IOException, JsonProcessingException {

        if (numberRegEx.matcher(numberOrString).matches()) {
            try {
                jsonGenerator.writeNumber(Long.valueOf(numberOrString));
                return;
            } catch (NumberFormatException ignored) {}
        }
        jsonGenerator.writeString(numberOrString);
    }
}
