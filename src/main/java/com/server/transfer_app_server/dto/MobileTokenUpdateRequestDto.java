package com.server.transfer_app_server.dto;

import com.server.transfer_app_server.vo.MobileTokenVO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MobileTokenUpdateRequestDto {

    private Long id;
    private String name;
    private String token;

    public MobileTokenVO toEntity(){
        return MobileTokenVO.builder()
                .id(id)
                .name(name)
                .token(token)
                .build();
    }

    @Builder
    public MobileTokenUpdateRequestDto(Long id, String name, String token){
        this.id = id;
        this.name = name;
        this.token = token;
    }
}
