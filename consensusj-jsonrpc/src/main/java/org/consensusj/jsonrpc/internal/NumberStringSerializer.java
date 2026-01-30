/*
 * Copyright 2014-2026 ConsensusJ Developers.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
    private static final Pattern numberRegEx = Pattern.compile("^-?\\d+$");

    @Override
    public void serialize(String numberOrString, JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider)
            throws IOException, JsonProcessingException {

        if (numberRegEx.matcher(numberOrString).matches()) {
            try {
                jsonGenerator.writeNumber(Long.parseLong(numberOrString));
                return;
            } catch (NumberFormatException ignored) {}
        }
        jsonGenerator.writeString(numberOrString);
    }
}
