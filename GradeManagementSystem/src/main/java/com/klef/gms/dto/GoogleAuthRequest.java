package com.klef.gms.dto;

import lombok.Data;

@Data
public class GoogleAuthRequest {
    private String credential; // token from Google One Tap

	public String getCredential() {
		return credential;
	}

	public void setCredential(String credential) {
		this.credential = credential;
	}
}
