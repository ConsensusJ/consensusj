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
/**
 * Java Module Declaration
 */
module org.consensusj.jsonrpc {
    requires java.net.http;

    requires org.slf4j;

    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires org.jspecify;

    exports org.consensusj.jsonrpc;
    exports org.consensusj.jsonrpc.introspection;
    exports org.consensusj.jsonrpc.internal to com.fasterxml.jackson.databind;
    opens org.consensusj.jsonrpc to com.fasterxml.jackson.databind;
}
