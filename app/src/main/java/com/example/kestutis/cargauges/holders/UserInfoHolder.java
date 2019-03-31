package com.example.kestutis.cargauges.holders;

import com.example.kestutis.cargauges.constants.Enums.MEASUREMENT_SYSTEM;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UserInfoHolder
{
    private int _id;
    private String _username;
    private String _password;
    private String _description;
    private MEASUREMENT_SYSTEM _measurementSystem;
    private String _token;
    private String _refreshToken;
}