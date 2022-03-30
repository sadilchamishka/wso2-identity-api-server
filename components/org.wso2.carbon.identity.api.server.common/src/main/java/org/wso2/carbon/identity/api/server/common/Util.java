/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.api.server.common;

import org.slf4j.MDC;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.recovery.ChallengeQuestionManager;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Common util class
 */
public class Util {

    private static final String PAGE_LINK_REL_NEXT = "next";
    private static final String PAGE_LINK_REL_PREVIOUS = "previous";
    private static final String PAGINATION_LINK_FORMAT = Constants.V1_API_PATH_COMPONENT
            + "%s?offset=%d&limit=%d";

    /**
     * Get ChallengeQuestionManager osgi service
     *
     * @return ChallengeQuestionManager
     */
    @Deprecated
    public static ChallengeQuestionManager getChallengeQuestionManager() {
        return (ChallengeQuestionManager) PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .getOSGiService(ChallengeQuestionManager.class, null);
    }

    /**
     * Get correlation id of current thread
     *
     * @return correlation-id
     */
    public static String getCorrelation() {
        String ref;
        if (isCorrelationIDPresent()) {
            ref = MDC.get(Constants.CORRELATION_ID_MDC);
        } else {
            ref = UUID.randomUUID().toString();

        }
        return ref;
    }

    /**
     * Check whether correlation id present in the log MDC
     *
     * @return whether the correlation id is present
     */
    public static boolean isCorrelationIDPresent() {
        return MDC.get(Constants.CORRELATION_ID_MDC) != null;
    }

    /**
     * Base64 URL encodes a given string.
     *
     * @param value String to be encoded.
     * @return Encoded string.
     */
    public static String base64URLEncode(String value) {

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Base64 URL decode a given encoded string.
     *
     * @param value Encoded string to be decoded.
     * @return Decoded string.
     */
    public static String base64URLDecode(String value) {

        return new String(
                Base64.getUrlDecoder().decode(value),
                StandardCharsets.UTF_8);
    }

    /**
     * Build 'next' and 'previous' pagination links.
     * @param limit Value of the 'limit' parameter.
     * @param currentOffset Value of the 'currentOffset' parameter.
     * @param totalResultsFromSearch Value of the 'totalResultsFromSearch' parameter.
     * @param servicePathComponent API service path. E.g: applications/
     * @return A map containing pagination link key-value pairs.
     */
    public static Map<String, String> buildPaginationLinks(int limit, int currentOffset, int totalResultsFromSearch,
                                                           String servicePathComponent) {

        Map<String, String> links = new HashMap<>();

        // Next link.
        if ((currentOffset + limit) < totalResultsFromSearch) {
            links.put(PAGE_LINK_REL_NEXT, ContextLoader.buildURIForBody
                    (String.format(PAGINATION_LINK_FORMAT, servicePathComponent, (currentOffset + limit), limit))
                    .toString());
        }

        /*
        Previous link.
        Previous link matters only if offset is greater than 0.
        */
        if (currentOffset > 0) {
            if ((currentOffset - limit) >= 0) { // A previous page of size 'limit' exists.
                links.put(PAGE_LINK_REL_PREVIOUS, ContextLoader.buildURIForBody
                        (String.format(PAGINATION_LINK_FORMAT, servicePathComponent,
                                calculateOffsetForPreviousLink(currentOffset, limit, totalResultsFromSearch), limit))
                        .toString());
            } else { // A previous page exists but it's size is less than the specified limit.
                links.put(PAGE_LINK_REL_PREVIOUS, ContextLoader.buildURIForBody
                        (String.format(PAGINATION_LINK_FORMAT, servicePathComponent, 0, currentOffset)).toString());
            }
        }

        return links;
    }

    private static int calculateOffsetForPreviousLink(int offset, int limit, int total) {

        int newOffset = (offset - limit);
        if (newOffset < total) {
            return newOffset;
        }

        return calculateOffsetForPreviousLink(newOffset, limit, total);
    }
}
