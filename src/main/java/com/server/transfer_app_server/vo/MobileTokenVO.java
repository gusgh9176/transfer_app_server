package com.server.transfer_app_server.vo;

import lombok.Getter;

@Getter
public class MobileTokenVO {
    private String name;
    private String token;
    MobileTokenVO(String name, String token){
        this.name = name;
        this.token = token;
    }
    @Override
    public String toString(){
        return "name: " + name +", " + "token: "+ token;
    }
}
