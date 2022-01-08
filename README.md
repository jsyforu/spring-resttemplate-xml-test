# spring-resttemplate-xml-test
spring-resttemplate-xml-test

5초 초과시 타임아웃 나오는 xml body를 사용한 resttemplate 예제

1. 오류발생
http://localhost:8080/sendXml?timeoutSecond=6
http://localhost:8080/sendXml?timeoutSecond=7

2. 성공
http://localhost:8080/sendXml?timeoutSecond=4
http://localhost:8080/sendXml?timeoutSecond=3

