package com.example.kestutis.cargauges.holders;

import com.example.kestutis.cargauges.constants.Enums.MEASUREMENT_SYSTEM;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class UserInfoHolder
{
    private String _userId;
    private String _password;
    private String _description;
    private MEASUREMENT_SYSTEM _measurementSystem;
}