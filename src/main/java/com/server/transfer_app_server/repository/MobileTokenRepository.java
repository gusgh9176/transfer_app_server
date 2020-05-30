package com.server.transfer_app_server.repository;

import com.server.transfer_app_server.vo.MobileTokenVO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.stream.Stream;

public interface MobileTokenRepository extends JpaRepository<MobileTokenVO, Long> {

    @Query("SELECT vo " +
            "FROM MobileTokenVO vo " +
            "ORDER BY vo.name DESC")
    Stream<MobileTokenVO> findAllDesc();

    Optional<MobileTokenVO> findByName(String name);
    Optional<MobileTokenVO> findByToken(String token);

}
