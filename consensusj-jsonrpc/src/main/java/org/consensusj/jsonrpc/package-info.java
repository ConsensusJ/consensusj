/**
 * Basic JSON-RPC Java client and supporting classes.
 *
 * Note the Groovy classes are now in a separate module, but they're still in the diagram
 * to illustrate the purpose of `UntypedRPCClient`.
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
 * UntypedRPCClient <|.. AbstractRPCClient
 * AbstractRPCClient <|-- RPCClient
 *
 * }
 *
 * namespace org.consensusj.jsonrpc.groovy {
 *
 * interface DynamicRPCFallback << Groovy, trait >>
 * class DynamicRPCClient << Groovy >>
 *
 * org.consensusj.jsonrpc.UntypedRPCClient <|.. DynamicRPCFallback
 * org.consensusj.jsonrpc.RPCClient <|-- DynamicRPCClient
 * DynamicRPCFallback <|.. DynamicRPCClient
 * }
 * ....
 *
 */
package org.consensusj.jsonrpc;