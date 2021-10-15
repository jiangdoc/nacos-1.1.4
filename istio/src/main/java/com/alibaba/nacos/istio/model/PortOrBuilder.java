// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: gateway.proto

package com.alibaba.nacos.istio.model;

public interface PortOrBuilder extends
    // @@protoc_insertion_point(interface_extends:istio.networking.v1alpha3.Port)
    com.google.protobuf.MessageOrBuilder {

    /**
     * <pre>
     * REQUIRED: A valid non-negative integer port number.
     * </pre>
     *
     * <code>uint32 number = 1;</code>
     *
     * @return The number.
     */
    int getNumber();

    /**
     * <pre>
     * REQUIRED: The protocol exposed on the port.
     * MUST BE one of HTTP|HTTPS|GRPC|HTTP2|MONGO|TCP|TLS.
     * TLS implies the connection will be routed based on the SNI header to
     * the destination without terminating the TLS connection.
     * </pre>
     *
     * <code>string protocol = 2;</code>
     *
     * @return The protocol.
     */
    java.lang.String getProtocol();

    /**
     * <pre>
     * REQUIRED: The protocol exposed on the port.
     * MUST BE one of HTTP|HTTPS|GRPC|HTTP2|MONGO|TCP|TLS.
     * TLS implies the connection will be routed based on the SNI header to
     * the destination without terminating the TLS connection.
     * </pre>
     *
     * <code>string protocol = 2;</code>
     *
     * @return The bytes for protocol.
     */
    com.google.protobuf.ByteString
    getProtocolBytes();

    /**
     * <pre>
     * Label assigned to the port.
     * </pre>
     *
     * <code>string name = 3;</code>
     *
     * @return The name.
     */
    java.lang.String getName();

    /**
     * <pre>
     * Label assigned to the port.
     * </pre>
     *
     * <code>string name = 3;</code>
     *
     * @return The bytes for name.
     */
    com.google.protobuf.ByteString
    getNameBytes();
}
