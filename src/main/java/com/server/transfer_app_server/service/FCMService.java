package com.server.transfer_app_server.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@AllArgsConstructor
@Service
public class FCMService {

    public void sendDownloadFCMMessage(String senderName, String receiverToken) throws Exception{
        // FireBase API Key
        final String apiKey = "AAAAo3HPVw0:APA91bEdVX4pA3qspRIA8H-ie_Qda8f9c2sFIBsT2Ocz9sUFXwGKljl3xT5wEbABQ906kOAk8h33SBI7HhXr0AUmkHHSXR7o3kStRfyoVEm7e8QEpL3D1p1UQwCeKz23MNH1ZcqLDiNN";
        URL url = new URL("https://fcm.googleapis.com/fcm/send");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "key=" + apiKey);

        conn.setDoOutput(true);

//        String userId = (String) request.getSession().getAttribute("ssUserId");
//        System.out.println("userId: " + userId);

        // 이렇게 보내면 주제를 ALL로 지정해놓은 모든 사람들한테 알림을 날려준다.
//        String input = "{\"notification\" : {\"title\" : \"여기다 제목 넣기 \", \"body\" : \"여기다 내용 넣기\"}, \"to\":\"/topics/ALL\"}";

        // 이걸로 보내면 특정 토큰을 가지고있는 어플에만 알림을 날려준다  위에 둘중에 한개 골라서 날려주자
        String input = "{" +
                "\"data\" : {" +
                "\"title\" : \"전송요청\", " +
                "\"body\" : \"" + senderName + "님이 전송요청을 보냈습니다.\", " +
                "\"senderName\" : \"" + senderName + "\", " +
                "\"clickAction\" : \"DownloadActivity\"" +
                "}, " +
                "\"to\":\" " + receiverToken + "\"" +
                "}";

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
        conn.disconnect();
        in.close();
        // print result
        System.out.println(response.toString());
    }
}
