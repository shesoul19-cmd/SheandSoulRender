package com.sheandsoul.v1update.Interface;

import org.springframework.web.bind.annotation.RequestBody;

import com.sheandsoul.v1update.dto.AuthDto;
import com.sheandsoul.v1update.dto.LoginRequest;
import com.sheandsoul.v1update.dto.ResendOtpRequest;
import com.sheandsoul.v1update.dto.SignUpRequest;
import com.sheandsoul.v1update.dto.SignupResponse;
import com.sheandsoul.v1update.dto.VerifyEmailRequest;

import retrofit2.Call;
import retrofit2.http.POST;

public interface ApiService {

    @POST("/api/signup")
    Call<SignupResponse> signup(@RequestBody SignUpRequest request);

    @POST("/api/verify-email")
    Call<Void> verifyEmail(@RequestBody VerifyEmailRequest request);
    
    @POST("/api/resend-otp")
    Call<Void> resendOtp(@RequestBody ResendOtpRequest request);

    @POST("/api/authenticate")
    Call<AuthDto> authenticate(@RequestBody LoginRequest request);

}
