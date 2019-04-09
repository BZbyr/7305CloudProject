package hk.hku.cloud.runner;

import hk.hku.cloud.kafka.controller.KafkaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

/**
 * @author: LexKaing
 * @create: 2019-04-10 00:47
 * @description:
 **/
@Component
@EnableAsync
public class MyApplicationRunner implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(MyApplicationRunner.class);

    @Autowired
    KafkaService kafkaService;


    @Override
    public void run(ApplicationArguments args) {
        logger.info("启动 ApplicationRunner");
        kafkaService.computeDL4JSentiment();
        logger.info("开启 dl4j 线程 成功");
    }
}