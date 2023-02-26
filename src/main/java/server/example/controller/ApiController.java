package server.example.controller;

import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import server.example.domain.ResultEmail;
import server.example.domain.ResultVO;
import server.example.handler.MailHandler;

import java.util.List;
import java.util.Map;

@Controller
public class ApiController {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String recentURL;

    @PostMapping(value="/foot-prints")
    @ResponseBody
    public String hello6(@RequestBody ResultVO result) {
        HttpHeaders headers=new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // JSON 객체 선언
        JsonObject parameter = new JsonObject();

        //화면에서받은 파라미터(년도,ID)를 선언한 JSON에 넣기
        parameter.addProperty("id",1);
        parameter.addProperty("data", result.getRawData());

        //데이터가 담긴 json을 hhtp객체에 담아 요청
        HttpEntity<String> req = new HttpEntity<>(parameter.toString(), headers);
        RestTemplate client=new RestTemplate();
        ResponseEntity<String> responseEntity =client.postForEntity("http://52.193.9.214:5000/image", req, String.class);

        //db연결해서 key mapping 하기
        System.out.println(responseEntity);
        // Select Scalar value
        int count = jdbcTemplate.queryForObject("SELECT MAX(id) FROM users;", Integer.class);
        List<Map<String, Object>> results = jdbcTemplate.queryForList("SELECT url FROM users WHERE id = ?", Integer.parseInt(responseEntity.getBody()));
        String[] a = results.get(0).toString().split("=");
        String[] b = a[1].split("}");

        recentURL = b[0];

        return b[0];
    }

    @Autowired
    private JavaMailSender mailSender;
    private static final String FROM_ADDRESS = "shwoodysh@gmail.com";

    @PostMapping(value="/email")
    @ResponseBody
    public boolean SendEmail(@RequestBody ResultEmail result) {
//        //version 1(very simple)
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setTo(result.getEmail());
//        message.setFrom(ApiController.FROM_ADDRESS);
//        message.setSubject("안녕하세요 ~");
//        message.setText("반갑습니다 ~");
//
//        mailSender.send(message);
//
//        return true;

        try {
            MailHandler mailHandler = new MailHandler(mailSender);

            // 받는 사람
            mailHandler.setTo(result.getEmail());
            // 보내는 사람
            mailHandler.setFrom(ApiController.FROM_ADDRESS);
            // 제목
            mailHandler.setSubject("케어엔코");
            // 메시지
            mailHandler.setText("반갑습니다~");
            //mailHandler.setText(recentURL);
//            // 첨부 파일
//            mailHandler.setAttach("foot_image.jpg", recentURL);
//            // 이미지 삽입
//            mailHandler.setInline("foot_image.jpg", recentURL);

            mailHandler.send();
            return true;
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }
}

