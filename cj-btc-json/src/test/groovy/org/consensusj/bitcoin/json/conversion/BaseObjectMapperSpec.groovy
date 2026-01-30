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
package org.consensusj.bitcoin.json.conversion

import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import spock.lang.Shared
import spock.lang.Specification

/**
 * Base class for testing serializers, deserializers in a mapper module
 */
abstract class BaseObjectMapperSpec extends Specification {
    @Shared
    ObjectMapper mapper

    void setup() {
        mapper = new ObjectMapper()
        def testModule = new SimpleModule("BitcoinJMappingClient", new Version(1, 0, 0, null, null, null))
        configureModule(testModule)
        mapper.registerModule(testModule)
    }

    /**
     * Override this class to configure your module
     * @param testModule
     */
    abstract void configureModule(testModule);
}