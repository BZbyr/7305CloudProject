package hk.hku.cloud.kafka.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Controller;

import javax.websocket.OnClose;
import java.text.SimpleDateFormat;

/**
 * @author: LexKaing
 * @create: 2019-04-01 23:36
 * @description: 目前不处理多页面同时访问导致的竞争情况
 **/
@Controller
@EnableAsync
public class KafkaController {

    private static final Logger logger = LoggerFactory.getLogger(KafkaController.class);

    private static SimpleDateFormat sdf = new SimpleDateFormat("EE MMM dd HH:mm:ss ZZ yyyy");

    @Autowired
    KafkaService kafkaService;

    /**
     * MessageMapping和 RequestMapping功能类似
     * 如果服务器接受到了消息，就会对订阅了@SendTo括号中的地址传送消息。
     */
    @MessageMapping("/welcome")
    @SendTo("/topic/init")
    public String welcome(String message) {
        logger.info("receive msg : " + message);
        // 开启线程处理标志
        kafkaService.setConsumeKafka(true);
        // 启动测试线程
        kafkaService.consumeKafkaTest();
        // 启动kafka 线程
//        kafkaService.consumeKafka();
        return message;
    }

    /**
     * 开关kafka 订阅
     */
    @MessageMapping("/updateConsumer")
    public void updateConsumer(String message) {
        if (message.equals("close")) {
            kafkaService.setConsumeKafka(false);
        } else {
            kafkaService.setConsumeKafka(true);
        }
    }

//    @OnClose
//    public void onClose (){
//        logger.info("websocket onClose");
//        kafkaService.setConsumeKafka(false);
//    }

//    @OnOpen
//    public void onOpen (Session session){
//        System.out.println("有新链接加入!当前在线人数为" + 123);
//    }

//    @OnMessage
//    public void onMessage (String message, Session session) throws IOException {
//        System.out.println("来自客户端的消息:" + message);
//    }

}
