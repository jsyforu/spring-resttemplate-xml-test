package com.ayoen.simplerestxmlsample;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.StringReader;
import java.security.InvalidParameterException;
import java.time.Duration;

/**
 * xml요청/응답의 타임아웃 나오는 테스트 샘플
 */
@RestController
public class SimpleRestXmlSampleController {

    @Autowired
    private RestTemplate restTemplate;

    /**
     * xml 포맷의 요청을 보내서 타임아웃이 제대로 동작하는지 확인
     * @param timeoutSecond 5초 초과시 타임아웃을 낸다.
     * @return
     */
    @GetMapping("/sendXml")
    public String sendXml(@RequestParam(name="timeoutSecond", required=false, defaultValue="3") long timeoutSecond) {

        if( timeoutSecond > Duration.ofMinutes(5).toSeconds() ){
            throw new InvalidParameterException("5분 이내로 입력하세요.");
        }

        String xmlBody = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";
        xmlBody += "<root><timeout>"+timeoutSecond+"</timeout></root>";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);
        HttpEntity<String> request = new HttpEntity<>(xmlBody, headers);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start("request call");

        ResponseEntity<String> response = restTemplate.postForEntity("http://localhost:8080/echoXml", request, String.class);

        stopWatch.stop();
        System.out.println(stopWatch.shortSummary());
        System.out.println(stopWatch.prettyPrint());
        System.out.println("response >> " + response.getBody());

        return "success";
    }

    /**
     * xml 요청을 받아서 xml형태로 응답을 주는 단순 처리
     * @param requestXml
     * @return
     * @throws InterruptedException
     */
    @PostMapping(path = "/echoXml", produces = MediaType.APPLICATION_ATOM_XML_VALUE)
    public String echoXml(@RequestBody String requestXml) throws InterruptedException, JAXBException {

        JAXBContext jaxbContext = JAXBContext.newInstance(TimeoutXmlData.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        TimeoutXmlData timeoutXmlData = (TimeoutXmlData) unmarshaller.unmarshal(new StringReader(requestXml));

        String xmlBody = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";
        xmlBody += "<root><success>true</success></root>";

        // 5초 초과시 timeout을 발생시킨다. @see restTemplateLong
        Thread.sleep( Duration.ofSeconds(timeoutXmlData.getTimeout()).toMillis());

        return xmlBody;
    }


    /**
     * 타임아웃 설정 (5초)
     * @param builder
     * @return
     */
    @Bean
    public RestTemplate restTemplateLong(RestTemplateBuilder builder) {
        return builder.setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(5))
                .build();
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "root")
    @Getter
    @Setter
    @ToString
    static class TimeoutXmlData {
        private long timeout;
    }

}
