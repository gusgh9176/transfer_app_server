package com.server.transfer_app_server.vo;


import lombok.*;

import javax.persistence.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class MobileTokenVO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String token;

    @Builder
    public MobileTokenVO(Long id, String name, String token){
        this.id = id;
        this.name = name;
        this.token = token;
    }
}
