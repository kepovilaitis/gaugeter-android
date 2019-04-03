package com.example.kestutis.cargauges.holders;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LiveDataHolder {
    private float _oilTemperature;
    private float _oilPressure;
    private float _waterTemperature;
    private float _charge;
}
