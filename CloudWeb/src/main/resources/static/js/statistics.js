$(document).ready(function () {
    console.log("统计图表开始加载");

    // 柱状图：统计
    let barChart = echarts.init(document.getElementById('BarChart'));
    // 饼图：
    let pieChart = echarts.init(document.getElementById('PieChart'));


    // 更新柱状图数据
    let barTitle = "Analyze the composition of the Twitter language in the past 1w"
    // 横轴坐标名, 初定6批数据
    let barxAxis = ['batch1', 'batch2', 'batch3', 'batch4', 'batch5', 'batch6']
    let barData = {
        // 初定分为 4类数据, 根据实际情况配置
        name: ['en', 'ch', 'jp', 'other'],
        // 显示的数据, 4*N 的矩阵, N<=6, 去掉旧数据保存新数据, 形成水流一样的效果
        en_data: [0],
        ch_data: [0],
        jp_data: [0],
        other_data: [0]
    }
    let barChartOption = setBarChartOption(barTitle, barxAxis, barData)
    barChart.setOption(barChartOption)
    // 更新柱状图的定时器id
    let barTimerId = undefined;
    // kafka 传来的最新 Lang 统计数据
    let langDataNewest = []


    // 更细饼图数据
    let pieTitle = "Analysis of the composition of Twitter users in the past 1w"
    let pieData = []
    let pieOption = setPieChartOption(pieTitle, pieData);
    pieChart.setOption(pieOption);
    // 更新饼图的定时器id
    let pieTimerId = undefined;
    // kafka 传来的最新 Fans 统计数据
    let fansDataNewest = []


    // stomp socket 客户端
    let stompClient = null;

    // 开启socket
    startSocket()

    function startSocket() {
        // 创建 socket 连接
        let socket = new SockJS('/endpointSang');
        stompClient = Stomp.over(socket);

        stompClient.heartbeat.outgoing = 15000;
        stompClient.heartbeat.incoming = 0;

        stompClient.connect({}, function (frame) {
            // 启动时往socket /initStatistic 发条消息，触发kafka 线程
            stompClient.send("/initStatistic", {}, "hello world");

            // 订阅 /topic/initStatistic
            stompClient.subscribe('/topic/initStatistic', function (response) {
                console.log("init statistic : " + response.body)
            })

            // 订阅 /topic/consumeLang 语言统计数据
            stompClient.subscribe('/topic/consumeLang', function (response) {
                if (response.body == "ping-alive"){
                    console.log("consumeLang alive")
                } else {
                    let tmp = response.body.split("|")
                    if (tmp.length < 4)
                        console.log("consume lang data format exeception : " + response.body)
                    else
                        langDataNewest = [tmp[0], tmp[1], tmp[2], tmp[3]]
                }
            })

            // 订阅 /topic/consumeFans 用户fans统计数据
            stompClient.subscribe('/topic/consumeFans', function (response) {
                if (response.body == "ping-alive"){
                    console.log("consumeFans alive")
                } else {
                    let tmp = response.body.split("|")
                    if (tmp.length < 5)
                        console.log("consume fans data format exeception : " + response.body)
                    else
                        fansDataNewest = [tmp[0], tmp[1], tmp[2], tmp[3], tmp[4]]
                }
            })
        });
    }

    // 关闭socket
    function stopSocket() {
        if (stompClient != null) {
            stompClient.disconnect();
            stompClient = null;
        }
        console.log('Disconnected socket');
    }


    // 开启统计图表更新 & socket 连接
    startSocket()
    switchBarTimer()
    switchPieTimer()

    // 开启/关闭柱状图刷新
    function switchBarTimer() {
        if (barTimerId == undefined) {
            function barTimer() {
                // 已经满6列数据，去除第一条，再新增最新数据
                while (barData.en_data.length >= 6) {
                    barData.en_data.shift()
                    barData.ch_data.shift()
                    barData.jp_data.shift()
                    barData.other_data.shift()
                }

                // 免得 langDataNewest 被改变了
                let displayData = langDataNewest;

                if (displayData.length < 4) {
                    // 使用测试数据
                    let randomData = [getRandomInt(100), getRandomInt(50), getRandomInt(50)]
                    barData.en_data.push(200 - randomData[0] - randomData[1] - randomData[2])
                    barData.ch_data.push(randomData[0])
                    barData.jp_data.push(randomData[1])
                    barData.other_data.push(randomData[2])
                } else {
                    // 插入 kafka 数据
                    barData.en_data.push(displayData[0])
                    barData.ch_data.push(displayData[1])
                    barData.jp_data.push(displayData[2])
                    barData.other_data.push(displayData[3])
                }

                //更新bar 显示数据
                barChart.setOption({
                    series: [{
                        name: barData.name[0],
                        type: 'line',
                        stack: '总量',
                        areaStyle: {},
                        data: barData.en_data
                    },
                        {
                            name: barData.name[1],
                            type: 'line',
                            stack: '总量',
                            areaStyle: {},
                            data: barData.ch_data
                        },
                        {
                            name: barData.name[2],
                            type: 'line',
                            stack: '总量',
                            areaStyle: {},
                            data: barData.jp_data
                        },
                        {
                            name: barData.name[3],
                            type: 'line',
                            stack: '总量',
                            areaStyle: {
                                normal: {}
                            },
                            data: barData.other_data
                        }
                    ]
                });
                // 每2秒刷新一次报表展示数据
                barTimerId = setTimeout(barTimer, 2000);
            }

            // 启动定时器
            barTimer();
        } else {
            clearTimeout(barTimerId);
            barTimerId = undefined;
        }
    }

    window.switchBarTimer = switchBarTimer;

    // 开启/关闭饼图刷新
    function switchPieTimer() {
        if (pieTimerId == undefined) {
            function pieTimer() {
                // 更新pie 显示数据
                let name = ['fans <= 100', '100 < fans <= 300', '300 < fans <= 500', '500 < fans <= 1000', '1000 < fans']
                let data = []

                // 免得 fansDataNewest 被改变了
                let displayData = fansDataNewest;

                if (displayData.length < 5) {
                    // 使用测试数据
                    for (let i = 0; i < 5; i++) {
                        data.push({
                            value: getRandomInt(1000),
                            name: name[i]
                        })
                    }
                } else {
                    // 使用 kafka 传来的最新数据
                    for (let i = 0; i < 5; i++) {
                        data.push({
                            value: displayData[i],
                            name: name[i]
                        })
                    }
                }
                pieChart.setOption({
                    series: [{
                        data: data
                    }]
                });
                pieTimerId = setTimeout(pieTimer, 2000);
            }

            // 启动定时器
            pieTimer();
        } else {
            clearTimeout(pieTimerId);
            pieTimerId = undefined;
        }
    }

    window.switchPieTimer = switchPieTimer;
})

function setBarChartOption(title, xAxis, barData) {
    let option = {
        title: {
            text: title,
            textStyle: {
                fontSize: 14,
                color: '#235894'
            }
        },
        tooltip: {
            trigger: 'axis',
            axisPointer: {
                type: 'cross',
                label: {
                    backgroundColor: '#6a7985'
                }
            }
        },
        grid: {
            // top:'5%',
            left: '3%',
            right: '4%',
            bottom: '3%',
            containLabel: true
        },
        xAxis: [{
            type: 'category',
            boundaryGap: false,
            data: xAxis
        }],
        yAxis: [{
            type: 'value'
        }],
    };
    return option
}

function setPieChartOption(title, pieData) {
    let piePatternImg = new Image();
    piePatternImg.src = "./images/piePatternImg.jpg";

    let itemStyle = {
        normal: {
            opacity: 0.7,
            color: {
                image: piePatternImg,
                repeat: 'repeat'
            },
            borderWidth: 3,
            borderColor: '#235894'
        }
    };
    let option = {
        title: {
            text: title,
            textStyle: {
                fontSize: 14,
                color: '#235894'
            }
        },
        tooltip: {},
        series: [{
            name: 'pie',
            type: 'pie',
            selectedMode: 'single',
            selectedOffset: 30,
            clockwise: true,
            label: {
                normal: {
                    textStyle: {
                        fontSize: 10,
                        color: '#235894'
                    }
                }
            },
            labelLine: {
                normal: {
                    lineStyle: {
                        color: '#235894'
                    }
                }
            },
            data: pieData,
            itemStyle: itemStyle
        }]
    };

    return option;
}

function getRandomInt(max) {
    return Math.floor(Math.random() * Math.floor(max));
}