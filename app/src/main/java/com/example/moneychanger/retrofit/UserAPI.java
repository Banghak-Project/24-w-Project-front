package com.example.moneychanger.retrofit;

import com.example.moneychanger.model.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface UserAPI {
    @GET("/user/get-all") // 다 갖고오기
    Call<List<User>> getAllUsers();

    @POST("/user/save") //저장하기
    Call<User> save(@Body User user);
}
