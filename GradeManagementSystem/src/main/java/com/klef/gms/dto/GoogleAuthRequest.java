package com.klef.gms.dto;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.constraints.NotBlank;


@Data
public class GoogleAuthRequest {

    private static final Logger logger = LoggerFactory.getLogger(GoogleAuthRequest.class);

    @NotBlank(message = "Credential must not be blank")
    private String credential; // token from Google One Tap

    public String getCredential() {
        logger.info("Getting credential value.");
        try {
            return credential;
        } catch (Exception ex) {
            logger.error("Error while getting credential: {}", ex.getMessage(), ex);
            throw ex;
        }
    }

    public void setCredential(String credential) {
        logger.info("Setting credential value.");
        try {
            this.credential = credential;
        } catch (Exception ex) {
            logger.error("Error while setting credential: {}", ex.getMessage(), ex);
            throw ex;
        }
    }
}
