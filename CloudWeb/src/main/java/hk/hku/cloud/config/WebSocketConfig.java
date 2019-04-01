package hk.hku.cloud.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * @author: LexKaing
 * @create: 2019-04-01 23:33
 * @description:
 * @EnableWebSocketMessageBroker 开启使用STOMP协议来传输基于代理的消息，Broker就是代理的意思。
 **/

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * 注册STOMP协议的节点，并指定映射的URL，供客户端与服务器端建立连接
     *
     * @param registry
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        System.out.println("registerStompEndpoints");
        // 注册STOMP协议节点，同时指定使用SockJS协议
        registry.addEndpoint("/endpointSang").withSockJS();
    }

    /**
     * 配置消息代理，实现推送功能，这里的消息代理是/topic
     * 服务端发送消息给客户端的域,多个用逗号隔开
     * @param registry
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        System.out.println("configureMessageBroker");
        registry.enableSimpleBroker("/topic");

//        //定义一对一推送的时候前缀
//        registry.setUserDestinationPrefix("/user");
//        //定义websoket前缀
//        registry.setApplicationDestinationPrefixes("/ws-push");
    }

}