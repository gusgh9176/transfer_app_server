package com.server.transfer_app_server.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.transfer_app_server.dto.MobileTokenNameResponseDto;
import com.server.transfer_app_server.dto.MobileTokenReadReqeustDto;
import com.server.transfer_app_server.dto.MobileTokenSaveRequestDto;
import com.server.transfer_app_server.service.FCMService;
import com.server.transfer_app_server.service.MobileTokenService;
import com.server.transfer_app_server.vo.MobileTokenVO;
import lombok.AllArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.util.List;


@RestController
@AllArgsConstructor
public class WebRestController {

    private MobileTokenService mobileTokenService;
    private FCMService fcmService;

    @PostMapping(value = "/upload")
    @ResponseBody
    public void fileUpload(HttpServletRequest request, @RequestPart MultipartFile files) {
        String userDir = request.getHeader("User-Agent");
        System.out.println("userDir: " + userDir);
        String baseDir = "C:\\ServerFiles\\" + userDir;
        File userFolder = new File(baseDir); // user 폴더
        try {
            // 유저 폴더 없을 시 생성
            if(!userFolder.exists()){
                userFolder.mkdir();
                System.out.println(baseDir + "의 폴더가 생성되었습니다.");
                System.out.println("경로: " + userFolder);
            }
            File dir = new File(baseDir + "\\" + files.getOriginalFilename());

            files.transferTo(dir);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // https://takeknowledge.tistory.com/62
    // 보안 생각해봐야함
    // vo에 담겨서 송신자의 name과 수신자의 token 전달됨
    @PostMapping(value = "/download")
    @ResponseBody
    public void fileDownload(@RequestBody MobileTokenVO vo, HttpServletResponse response) throws Exception {
        String senderName = vo.getName(); // 송신자 Name
        String receiverName = mobileTokenService.findByToken(vo.getToken()).getName();

        String senderToken = mobileTokenService.findByName(senderName).getToken(); // 송신자 token
        String receiverToken = vo.getToken(); // 수신자 Token

        int senderTokenLength = senderToken.length();

        boolean isExist = mobileTokenService.isExistToken(receiverToken);
        // 수신자 token 이 서버 DB에 저장 안되있으면 이상한 접근이므로 다운로드 실행 안함
        if (!isExist) {
            System.out.println("DB에 수신자 token 미존재");
            System.out.println("앱 외에서의 접근");
            return;
        }
        // 송신자 token 이 서버 DB에 저장 안되있으면 이상한 접근이므로 다운로드 실행 안함
        else if (senderToken.equals("wrongName")) {
            System.out.println("DB에 송신자 token 미존재");
            return;
        }

        File[] fileList = null;
        try {
            String baseDir = "C:\\ServerFiles\\" + senderToken.substring(senderTokenLength-10, senderTokenLength); // 송신자 name 폴더로 들어감
            fileList = new File(baseDir).listFiles();
        } catch (Exception e) {
            e.printStackTrace();
        }

        assert fileList != null;
        String fileName = fileList[0].getName();
        long fileLength = fileList[0].length();
        String contentType = "multipart/formed-data";
        System.out.println("파일이름: " + fileName);


        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\";");
        response.setHeader("Content-Transfer-Encoding", "binary");
        response.setHeader("Content-Type", contentType);
        response.setHeader("Content-Length", "" + fileLength);
        response.setHeader("Pragma", "no-cache;");
        response.setHeader("Expires", "-1;");

        System.out.println("파일경로: " + fileList[0].toString());

        try (FileInputStream fis = new FileInputStream(fileList[0].toString()); OutputStream out = response.getOutputStream();) {
            // saveFileName을 파라미터로 넣어 inputStream 객체를 만들고
            // response에서 파일을 내보낼 OutputStream을 가져와서
            int readCount = 0;
            byte[] buffer = new byte[1024];
            // 파일 읽을 만큼 크기의 buffer를 생성한 후
            while ((readCount = fis.read(buffer)) != -1) {
                out.write(buffer, 0, readCount);
                // outputStream에 씌워준다
            }
        } catch (Exception ex) {
            throw new RuntimeException("file Load Error");
        }

        fcmService.sendSuccessFCMMessage(receiverName, senderToken);

    }

    @PostMapping(value = "mobile/read/UserList")
    @ResponseBody
    public void responseUserList(@RequestBody MobileTokenReadReqeustDto dto, HttpServletResponse response) throws JsonProcessingException {
        String token = dto.getToken();
        boolean isExist = mobileTokenService.isExistToken(token);

        // 보낸쪽 token 서버 DB에 저장 안되있으면 이상한 접근이므로 유저 목록 제공 안함
        if (!isExist) {
            System.out.println("미존재");
            System.out.println("앱 외에서의 접근");
            return;
        }

        List<MobileTokenNameResponseDto> userList = mobileTokenService.returnNameList();

        // Response에 유저목록 Json으로 담아 응답
        ObjectMapper mapper = new ObjectMapper();
        String jsonStr = mapper.writeValueAsString(userList);
        String contentType = "application/json";

        try {
            response.setContentType(contentType);
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Pragma", "no-cache;");
            response.setHeader("Expires", "-1;");
            response.getWriter().write(jsonStr);
        } catch (IOException ioe) {
            ioe.printStackTrace();
//            throw new RuntimeException("Json Parsing Error");
        }
    }

    // DB에 Name과 FCMToken 저장
    @PostMapping(value = "mobile/insert/FCMToken")
    public Long saveFCMToken(@RequestBody MobileTokenSaveRequestDto dto) throws Exception {
        System.out.println("name: " + dto.getName());
        System.out.println("token: " + dto.getToken());
        return mobileTokenService.save(dto);
    }

    // DB에 Name 업데이트
    @RequestMapping(value = "mobile/update/FCMToken")
    public Long updateName(@RequestBody MobileTokenSaveRequestDto dto) throws Exception {
        return mobileTokenService.update(dto);
    }

    // DB 목록 확인
    @RequestMapping(value = "mobile/print/FCMToken")
    public void printName() throws Exception {
        System.out.println("현재 토큰 목록");
        mobileTokenService.findAllDescPrint();
        System.out.println("현재 토큰 목록 끝");
    }

    // Name으로 받아서 Token 찾아야함
    // vo는 보낼 사람의 name
    @PostMapping(value = "mobile/send/FCMToken")
    public void sendFCMMessage(Model model, HttpServletRequest request, HttpSession session, @RequestBody MobileTokenVO vo) throws Exception {

        String receiverToken = mobileTokenService.findByName(vo.getName()).getToken(); // 수신자 token
        String senderName = mobileTokenService.findByToken(vo.getToken()).getName(); // 송신자 name

        if(receiverToken.equals("wrongName") || senderName.equals("wrongToken")){
            System.out.println("DB에 없는 name 또는 token");
            return;
        }
        fcmService.sendDownloadFCMMessage(senderName, receiverToken);

    }
}
