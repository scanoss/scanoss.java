// SPDX-License-Identifier: MIT
/*
 * Copyright (c) 2023, SCANOSS
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.scanoss.rest;

import lombok.Getter;

/**
 * Enum list of standard HTTP Status Codes
 */
public enum HttpStatusCode {

    //1xx: Informational
    /**
     * Continue
     */
    CONTINUE(100, "Continue"),
    /**
     * Switching Protocols
     */
    SWITCHING_PROTOCOLS(101, "Switching Protocols"),
    /**
     * Processing
     */
    PROCESSING(102, "Processing"),
    /**
     * Early Hints
     */
    EARLY_HINTS(103, "Early Hints"),

    //2xx: Success
    /**
     * OK
     */
    OK(200, "OK"),
    /**
     * Created
     */
    CREATED(201, "Created"),
    /**
     * Accepted
     */
    ACCEPTED(202, "Accepted"),
    /**
     * Non-Authoritative Information
     */
    NON_AUTHORITATIVE_INFORMATION(203, "Non-Authoritative Information"),
    /**
     * No Content
     */
    NO_CONTENT(204, "No Content"),
    /**
     * Reset Content
     */
    RESET_CONTENT(205, "Reset Content"),
    /**
     * Partial Content
     */
    PARTIAL_CONTENT(206, "Partial Content"),
    /**
     * Multi Status
     */
    MULTI_STATUS(207, "Multi-Status"),
    /**
     * Already Reported
     */
    ALREADY_REPORTED(208, "Already Reported"),
    /**
     * IM Used
     */
    IM_USED(226, "IM Used"),

    //3xx: Redirection
    /**
     * Multiple Choices
     */
    MULTIPLE_CHOICES(300, "Multiple Choice"),
    /**
     * Moved Permanently
     */
    MOVED_PERMANENTLY(301, "Moved Permanently"),
    /**
     * Found
     */
    FOUND(302, "Found"),
    /**
     * See Other
     */
    SEE_OTHER(303, "See Other"),
    /**
     * Not Modified
     */
    NOT_MODIFIED(304, "Not Modified"),
    /**
     * Use Proxy
     */
    USE_PROXY(305, "Use Proxy"),
    /**
     * Temporary Redirect
     */
    TEMPORARY_REDIRECT(307, "Temporary Redirect"),
    /**
     * Permanent Redirect
     */
    PERMANENT_REDIRECT(308, "Permanent Redirect"),

    //4xx: Client Error
    /**
     * Bad Request
     */
    BAD_REQUEST(400, "Bad Request"),
    /**
     * Unauthorised
     */
    UNAUTHORIZED(401, "Unauthorized"),
    /**
     * Payment Continue
     */
    PAYMENT_REQUIRED(402, "Payment Required"),
    /**
     * Forbidden
     */
    FORBIDDEN(403, "Forbidden"),
    /**
     * Not Found
     */
    NOT_FOUND(404, "Not Found"),
    /**
     * Method Not Allowed
     */
    METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
    /**
     * Not Acceptable
     */
    NOT_ACCEPTABLE(406, "Not Acceptable"),
    /**
     * Proxy Authentication Required
     */
    PROXY_AUTHENTICATION_REQUIRED(407, "Proxy Authentication Required"),
    /**
     * Request Timeout
     */
    REQUEST_TIMEOUT(408, "Request Timeout"),
    /**
     * Conflict
     */
    CONFLICT(409, "Conflict"),
    /**
     * Gone
     */
    GONE(410, "Gone"),
    /**
     * Length Required
     */
    LENGTH_REQUIRED(411, "Length Required"),
    /**
     * Precondition Failed
     */
    PRECONDITION_FAILED(412, "Precondition Failed"),
    /**
     * Request Too Long
     */
    REQUEST_TOO_LONG(413, "Payload Too Large"),
    /**
     * Request URI Too Long
     */
    REQUEST_URI_TOO_LONG(414, "URI Too Long"),
    /**
     * Unsupported Media Type
     */
    UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type"),
    /**
     * Requested Range Not Satisfiable
     */
    REQUESTED_RANGE_NOT_SATISFIABLE(416, "Range Not Satisfiable"),
    /**
     * Expectation Failed
     */
    EXPECTATION_FAILED(417, "Expectation Failed"),
    /**
     * Misdirected Request
     */
    MISDIRECTED_REQUEST(421, "Misdirected Request"),
    /**
     * Unprocessable Entity
     */
    UNPROCESSABLE_ENTITY(422, "Unprocessable Entity"),
    /**
     * Locked
     */
    LOCKED(423, "Locked"),
    /**
     * Failed Dependency
     */
    FAILED_DEPENDENCY(424, "Failed Dependency"),
    /**
     * Too Early
     */
    TOO_EARLY(425, "Too Early"),
    /**
     * Upgrade Required
     */
    UPGRADE_REQUIRED(426, "Upgrade Required"),
    /**
     * Precondition Required
     */
    PRECONDITION_REQUIRED(428, "Precondition Required"),
    /**
     * Too Many Requests
     */
    TOO_MANY_REQUESTS(429, "Too Many Requests"),
    /**
     * Request Header Fields Too Large
     */
    REQUEST_HEADER_FIELDS_TOO_LARGE(431, "Request Header Fields Too Large"),
    /**
     * Unavailable For Legal Reasons
     */
    UNAVAILABLE_FOR_LEGAL_REASONS(451, "Unavailable For Legal Reasons"),

    //5xx: Server Error
    /**
     * Internal Server Error
     */
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    /**
     * Not Implemented
     */
    NOT_IMPLEMENTED(501, "Not Implemented"),
    /**
     * Bad Gateway
     */
    BAD_GATEWAY(502, "Bad Gateway"),
    /**
     * Service Unavailable
     */
    SERVICE_UNAVAILABLE(503, "Service Unavailable"),
    /**
     * Gateway Timeout
     */
    GATEWAY_TIMEOUT(504, "Gateway Timeout"),
    /**
     * HTTP Version Not Supported
     */
    HTTP_VERSION_NOT_SUPPORTED(505, "HTTP Version Not Supported"),
    /**
     * Variant Also Negotiates
     */
    VARIANT_ALSO_NEGOTIATES(506, "Variant Also Negotiates"),
    /**
     * Insufficient Storage
     */
    INSUFFICIENT_STORAGE(507, "Insufficient Storage"),
    /**
     * Loop Detected
     */
    LOOP_DETECTED(508, "Loop Detected"),
    /**
     * Not Extended
     */
    NOT_EXTENDED(510, "Not Extended"),
    /**
     * Network Authentication Required
     */
    NETWORK_AUTHENTICATION_REQUIRED(511, "Network Authentication Required");

    /**
     *  Get the Integer value of the HTTP Status Code
     */
    @Getter
    private final int value;
    private final String description;

    /**
     * HTTP Status Code Enum
     *
     * @param value       HTTP status code integer
     * @param description HTTP status description
     */
    HttpStatusCode(int value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * Return a string value for the given HTTP Status code
     *
     * @param value HTTP status code integer
     * @return HTTP status code and description
     */
    public static String getByValueToString(int value) {
        for (HttpStatusCode status : values()) {
            if (status.value == value) {
                return status.toString();
            }
        }
        return value + " Unknown Status Code";
    }

    /**
     * String friendly version of the HTTP Status Code
     *
     * @return HTTP status code and description
     */
    @Override
    public String toString() {
        return value + " " + description;
    }
}
