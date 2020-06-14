package com.server.transfer_app_server.service;

import com.server.transfer_app_server.dto.MobileTokenMainReponseDto;
import com.server.transfer_app_server.dto.MobileTokenNameResponseDto;
import com.server.transfer_app_server.dto.MobileTokenSaveRequestDto;
import com.server.transfer_app_server.dto.MobileTokenUpdateRequestDto;
import com.server.transfer_app_server.repository.MobileTokenRepository;
import com.server.transfer_app_server.vo.MobileTokenVO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class MobileTokenService {

    private MobileTokenRepository mobileTokenRepository;

    @Transactional
    public Long save(MobileTokenSaveRequestDto dto) {
        return mobileTokenRepository.save(dto.toEntity()).getId();
    }

    @Transactional
    public Long update(MobileTokenSaveRequestDto dto) {
        Optional<MobileTokenVO> mobileTokenVOWrapper = mobileTokenRepository.findByToken(dto.getToken());
        if (!mobileTokenVOWrapper.isPresent()) { // 해당 token 값이 db에 없을 때 실행
            return (long) -1;
        }
        MobileTokenVO mobileTokenVO = mobileTokenVOWrapper.get();
        Long id = mobileTokenVO.getId();

        return mobileTokenRepository.save(new MobileTokenUpdateRequestDto(id, dto.getName(), dto.getToken()).toEntity()).getId();
    }

    @Transactional(readOnly = true)
    public List<MobileTokenMainReponseDto> findAllDesc() {

        return mobileTokenRepository.findAllDesc()
                .map(MobileTokenMainReponseDto::new)
                .collect(Collectors.toList());
    }

    // 해당 Token이 존재하는지 확인
    @Transactional(readOnly = true)
    public Boolean isExistToken(String token){
        Optional<MobileTokenVO> mobileTokenVO = mobileTokenRepository.findByToken(token);
        return mobileTokenVO.isPresent();
    }

    // 테스트용 삭제예정
    @Transactional(readOnly = true)
    public void findAllDescPrint() {
//         들어있는 내용 확인
        for(MobileTokenMainReponseDto m : mobileTokenRepository.findAllDesc().map(MobileTokenMainReponseDto::new).collect(Collectors.toList())){
            System.out.println("name: "+ m.getName() + ", " + "token: " + m.getToken());
        }
    }

    @Transactional(readOnly = true)
    public MobileTokenVO findByName(String name){

        return mobileTokenRepository.findByName(name).get();
    }

    @Transactional(readOnly = true)
    public List<MobileTokenNameResponseDto> returnNameList(){
        List<MobileTokenNameResponseDto> dtoList = new ArrayList<>();
        for(MobileTokenMainReponseDto m : mobileTokenRepository.findAllDesc().map(MobileTokenMainReponseDto::new).collect(Collectors.toList())){
            dtoList.add(new MobileTokenNameResponseDto(m.getName()));
        }

        return dtoList;
    }

}
