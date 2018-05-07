/**
 * Basic JSON-RPC Java client and supporting classes.
 *
 * [plantuml, diagram-classes, png]
 * ....
 * skinparam packageStyle Rect
 * skinparam shadowing false
 * hide empty members
 *
 * namespace org.consensusj.jsonrpc {
 * interface UntypedRPCClient
 * abstract class AbstractRPCClient
 *
 * class RPCClient
 *
 * interface DynamicRPCFallback << Groovy, trait >>
 * class DynamicRPCClient << Groovy >>
 *
 * UntypedRPCClient <|.. AbstractRPCClient
 * AbstractRPCClient <|-- RPCClient
 *
 * UntypedRPCClient <|.. DynamicRPCFallback
 * RPCClient <|-- DynamicRPCClient
 * DynamicRPCFallback <|.. DynamicRPCClient
 * }
 * ....
 *
 */
package org.consensusj.jsonrpc;