package com.allobank.idrrate.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * Unified API response wrapper for all resource types.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse(
        String resourceType,
        boolean success,
        Object data,
        String error
) {

    public static ApiResponse success(String resourceType, Object data) {
        return new ApiResponse(resourceType, true, data, null);
    }

    public static ApiResponse error(String resourceType, String error) {
        return new ApiResponse(resourceType, false, null, error);
    }
}
