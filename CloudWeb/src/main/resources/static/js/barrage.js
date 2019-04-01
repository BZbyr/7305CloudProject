// jquery
$(document).ready(function () {
    console.log("页面已经加载完毕");

    // 默认显示的情绪为 nlp 的结果；否则显示naive bayes 的结果
    let sentiment = "nlp";
    // stomp socket 客户端
    let stompClient = null;

    let canvasEle = document.querySelector('canvas')
    let barrage = new Barrage(canvasEle, 100, 10)
    let inputEle = document.querySelector('.barrage-input')

    document.querySelector('.send-primary-btn').onclick = function () {
        barrage.pushMessage({text: inputEle.value, color: 'white', speed: 2})
        inputEle.value = ''
    }

    document.querySelector('.clear-btn').onclick = function () {
        // 清理弹幕
        barrage.clear()
    }

    document.querySelector('.close-btn').onclick = function () {
        // 关闭弹幕滚动
        barrage.close();

        // 通知后端停止线程订阅kafka消息
        stompClient.send("/updateConsumer", {}, "close");

        // 关闭 socket 连接
        if (stompClient != null) {
            stompClient.disconnect();
        }
        console.log('Disconnected socket');
    }

    document.querySelector('.open-btn').onclick = function () {
        // 开启弹幕滚动
        barrage.open();

        // 创建 socket 连接
        let socket = new SockJS('/endpointSang');
        stompClient = Stomp.over(socket);

        stompClient.connect({}, function (frame) {
            console.log('Connected:' + frame);

            // 启动时往socket /welcome 发条消息，触发kafka 线程
            stompClient.send("/welcome", {}, "hello world");

            // 订阅 /topic/getResponse
            stompClient.subscribe('/topic/init', function (response) {
                console.log("getResponse : " + response);
                barrage.pushMessage({text: "😊" + response.body, color: 'white', speed: 1});
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

        let length = 30
        let line = emoji + " " + (message.text.length < length ? message.text : message.text.substr(0, length) + "..");

        barrage.pushMessage({text: line, color: 'white', speed: 2});
    }


    document.querySelector('.nlp-btn').onclick = function () {
        sentiment = "nlp";
    }

    document.querySelector('.nb-btn').onclick = function () {
        sentiment = "naive bayes";
    }
})
