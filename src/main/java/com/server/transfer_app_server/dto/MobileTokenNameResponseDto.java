package com.server.transfer_app_server.dto;

import lombok.Getter;

@Getter
public class MobileTokenNameResponseDto {

    private String name;

    public MobileTokenNameResponseDto(String name){
        this.name = name;
    }
}
