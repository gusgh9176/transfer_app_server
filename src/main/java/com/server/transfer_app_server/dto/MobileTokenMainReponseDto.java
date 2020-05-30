package com.server.transfer_app_server.dto;

import com.server.transfer_app_server.vo.MobileTokenVO;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
public class MobileTokenMainReponseDto {

    private Long id;
    private String name;
    private String token;

    public MobileTokenMainReponseDto(MobileTokenVO mobileTokenVO){
        id = mobileTokenVO.getId();
        name = mobileTokenVO.getName();
        token = mobileTokenVO.getToken();
    }
}
