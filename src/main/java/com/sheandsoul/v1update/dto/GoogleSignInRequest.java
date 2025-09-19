package com.sheandsoul.v1update.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record  GoogleSignInRequest (
  @JsonProperty("idToken") String idToken
){

}
