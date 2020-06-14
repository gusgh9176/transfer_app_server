package com.server.transfer_app_server.dto;

import com.server.transfer_app_server.vo.MobileTokenVO;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MobileTokenSaveRequestDto {

    private String name;
    private String token;

    public MobileTokenSaveRequestDto(String name, String token){
        this.name = name;
        this.token = token;
    }

    public MobileTokenVO toEntity(){
        return MobileTokenVO.builder()
                .name(name)
                .token(token)
                .build();
    }
}
