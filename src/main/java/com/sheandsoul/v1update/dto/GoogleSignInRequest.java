package com.sheandsoul.v1update.dto;


import jakarta.validation.constraints.NotBlank;

public record  GoogleSignInRequest (
     @NotBlank String idToken
){

}
