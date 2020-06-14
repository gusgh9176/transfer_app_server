package com.server.transfer_app_server.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.transfer_app_server.dto.MobileTokenMainReponseDto;
import com.server.transfer_app_server.dto.MobileTokenNameResponseDto;
import com.server.transfer_app_server.dto.MobileTokenReadReqeustDto;
import com.server.transfer_app_server.dto.MobileTokenSaveRequestDto;
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
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;


@RestController
@AllArgsConstructor
public class WebRestController {

    private MobileTokenService mobileTokenService;

    @PostMapping(value = "/upload")
    @ResponseBody
    public void fileUpload(@RequestPart MultipartFile files) {
        try {
            String baseDir = "C:\\ServerFiles";
            files.transferTo(new File(baseDir + "\\" + files.getOriginalFilename()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // https://takeknowledge.tistory.com/62
    @PostMapping(value = "/download")
    @ResponseBody
    public void fileDownload(@RequestBody HashMap<String, Object> tokenMap, HttpServletResponse response) {
        System.out.println("토큰: " + tokenMap.values());

        File[] fileList = null;
        try {
            String baseDir = "C:\\ServerFiles";
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
    }

    @PostMapping(value = "mobile/read/UserList")
    @ResponseBody
    public void responseUserList(@RequestBody MobileTokenReadReqeustDto dto, HttpServletResponse response) throws JsonProcessingException {
        String token = dto.getToken();
        boolean isExist = mobileTokenService.isExistToken(token);

        // 보낸쪽 token 서버 DB에 저장 안되있으면 이상한 접근이므로 유저 목록 제공 안함
        if(!isExist){
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
        }catch (IOException ioe){
            ioe.printStackTrace();
//            throw new RuntimeException("Json Parsing Error");
        }
    }

    // DB에 Name과 FCMToken 저장
    @PostMapping(value = "mobile/insert/FCMToken")
    public Long saveFCMToken(@RequestBody MobileTokenSaveRequestDto dto) throws Exception{
        System.out.println("name: "+dto.getName());
        System.out.println("token: "+dto.getToken());
        return mobileTokenService.save(dto);
    }

    // DB에 Name 업데이트
    @RequestMapping(value = "mobile/update/FCMToken")
    public Long updateName(@RequestBody MobileTokenSaveRequestDto dto) throws Exception{
        return mobileTokenService.update(dto);
    }

    // DB 목록 확인
    @RequestMapping(value = "mobile/print/FCMToken")
    public void printName() throws Exception{
        System.out.println("현재 토큰 목록");
        mobileTokenService.findAllDescPrint();
        System.out.println("현재 토큰 목록 끝");
    }

    // Name으로 받아서 Token 찾아야함
    // vo는 보낼 사람의 name
    @RequestMapping(value = "mobile/send/FCMToken")
    public String index(Model model, HttpServletRequest request, HttpSession session, @RequestBody MobileTokenVO vo) throws Exception {

        String token = mobileTokenService.findByName(vo.getName()).getToken();

        // FireBase API Key
        final String apiKey = "AAAAo3HPVw0:APA91bEdVX4pA3qspRIA8H-ie_Qda8f9c2sFIBsT2Ocz9sUFXwGKljl3xT5wEbABQ906kOAk8h33SBI7HhXr0AUmkHHSXR7o3kStRfyoVEm7e8QEpL3D1p1UQwCeKz23MNH1ZcqLDiNN";
        URL url = new URL("https://fcm.googleapis.com/fcm/send");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "key=" + apiKey);

        conn.setDoOutput(true);

        String userId = (String) request.getSession().getAttribute("ssUserId");

        // 이렇게 보내면 주제를 ALL로 지정해놓은 모든 사람들한테 알림을 날려준다.
//        String input = "{\"notification\" : {\"title\" : \"여기다 제목 넣기 \", \"body\" : \"여기다 내용 넣기\"}, \"to\":\"/topics/ALL\"}";

        // 이걸로 보내면 특정 토큰을 가지고있는 어플에만 알림을 날려준다  위에 둘중에 한개 골라서 날려주자
        String input = "{\"notification\" : {\"title\" : \" 여기다 제목넣기 \", \"body\" : \"여기다 내용 넣기\"}, \"to\":\" " + token + "\"}";

        OutputStream os = conn.getOutputStream();

        // 서버에서 날려서 한글 깨지는 사람은 아래처럼  UTF-8로 인코딩해서 날려주자
        os.write(input.getBytes("UTF-8"));
        os.flush();
        os.close();

        int responseCode = conn.getResponseCode();
        System.out.println("\nSending 'POST' request to URL : " + url);
        System.out.println("Post parameters : " + input);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        // print result
        System.out.println(response.toString());

        return "jsonView";
    }


}
