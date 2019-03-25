package com.example.kestutis.cargauges.holders;

import com.example.kestutis.cargauges.constants.Enums.MEASUREMENT_SYSTEM;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UserInfoHolder
{
    public int _id;
    public String _username;
    public String _password;
    public String _description;
    public MEASUREMENT_SYSTEM _measurementSystem;
    public String _token;
    public String _refreshToken;
}