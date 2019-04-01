// jquery
$(document).ready(function () {
    console.log("页面已经加载完毕");

    // 默认显示的情绪为 nlp 的结果；否则显示naive bayes 的结果
    let sentiment = "nlp";
    // stomp socket 客户端
    let stompClient = null;

    let analysisMethod = document.getElementById("method");
    let canvasEle = document.querySelector('canvas')
    let barrage = new Barrage(canvasEle, 100, 10)

    // let inputEle = document.querySelector('.barrage-input')
    // document.querySelector('.send-primary-btn').onclick = function () {
    //     // 测试普通发射弹幕
    //     barrage.pushMessage({text: inputEle.value, color: 'white', speed: 2})
    //     inputEle.value = ''
    // }

    document.querySelector('.clear-btn').onclick = function () {
        // 清理弹幕
        barrage.clear()
    }

    document.querySelector('.close-btn').onclick = function () {
        // 关闭弹幕滚动
        barrage.close();

        // 关闭 socket 连接
        if (stompClient != null) {
            // 通知后端停止线程订阅kafka消息
            stompClient.send("/updateConsumer", {}, "close");

            stompClient.disconnect();
            stompClient = null;
            console.log('Disconnected socket');
        }
    }

    document.querySelector('.open-btn').onclick = function () {
        // 开启弹幕滚动
        barrage.open();

        // 创建 socket 连接
        let socket = new SockJS('/endpointSang');
        stompClient = Stomp.over(socket);

        /*
        * 1. 获取到stomp 子协议后，可以设置心跳连接时间，认证连接，主动断开连接
        * 2，连接心跳有的版本的stomp.js 是默认开启的，这里我们不管版本，手工设置
        * 3. 心跳是双向的，客户端开启心跳，必须要服务端支持心跳才行
        * 4. heartbeat.outgoing 表示客户端给服务端发送心跳的间隔时间
        * 5. 客户端接收服务端心跳的间隔时间，如果为0 表示客户端不接收服务端心跳
        */
        stompClient.heartbeat.outgoing = 5000;
        stompClient.heartbeat.incoming = 0;


        stompClient.connect({}, function (frame) {
            console.log('Connected:' + frame);

            // 启动时往socket /welcome 发条消息，触发kafka 线程
            stompClient.send("/welcome", {}, "hello world");

            // 订阅 /topic/init
            stompClient.subscribe('/topic/init', function (response) {
                console.log("init : " + response);
                barrage.pushMessage({text: "😊" + response.body, color: 'white', speed: 1.5});
            })

            // 订阅 /topic/consumeKafka, 解析消息并显示弹幕
            stompClient.subscribe('/topic/consumeKafka', function (response) {
                showResponse(JSON.parse(response.body));
            })
        });
    }

    // 接受socket 消息，显示弹幕. 1:positive; 0:neutral; -1:negative
    function showResponse(message) {
        let emoji = "";
        if (sentiment == "nlp") {
            emoji = message.nlpPolarity == 1 ? "😍" : (message.nlpPolarity == 0 ? "😐" : "😭");
        } else {
            emoji = message.nbPolarity == 1 ? "😍" : (message.nbPolarity == 0 ? "😐" : "😭");
        }

        let length = 50;
        let line = emoji + " " + (message.text.length < length ? message.text : message.text.substr(0, length) + "..");

        barrage.pushMessage({text: line, color: 'white', speed: 2});
    }


    document.querySelector('.nlp-btn').onclick = function () {
        sentiment = "nlp";
        analysisMethod.innerHTML = "stanford core nlp";
    }

    document.querySelector('.nb-btn').onclick = function () {
        sentiment = "naive bayes";
        analysisMethod.innerHTML = "spark mllib naive bayes";
    }

    //刷新or关闭浏览器前，先断开socket连接，onbeforeunload 在 onunload之前执行
    window.onbeforeunload = function () {
        if (stompClient != null) {
            // 通知后端停止线程订阅kafka消息
            stompClient.send("/updateConsumer", {}, "close");

            stompClient.disconnect();
            stompClient = null;
            console.log("stompClient disconnect");
        }
        console.log("onbeforeunload");
    }
})
