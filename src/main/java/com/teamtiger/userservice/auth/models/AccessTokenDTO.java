package com.teamtiger.userservice.auth.models;

import lombok.Builder;
import lombok.Data;

/**
 * Wrapper class for the access token
 */
@Data
@Builder
public class AccessTokenDTO {
    private String accessToken;
}
