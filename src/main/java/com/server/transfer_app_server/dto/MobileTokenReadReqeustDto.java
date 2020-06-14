package com.server.transfer_app_server.dto;

import com.server.transfer_app_server.vo.MobileTokenVO;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MobileTokenReadReqeustDto {

    private String token;

    public MobileTokenReadReqeustDto(String token){
        this.token = token;
    }

}
