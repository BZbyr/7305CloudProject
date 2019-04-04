//为地图创建容器
var dom1 = document.getElementById("map-wrap");
//初始化地图
var myChart = echarts.init(dom1);
var app = {};
option = null;

/* var geoCoordMap = {
    '拉萨':[91.11,29.97],
    '上海':[121.48,31.22],
    '深圳':[114.07,22.62],
    '苏州':[120.62,31.32],
    '三亚':[109.511909,18.252847],
    '南京':[118.78,32.04],
    '北京':[116.46,39.92],
    '衡水':[115.72,37.72],
    '广州':[113.23,23.16],
    '沈阳':[123.38,41.8],
    '石家庄':[114.48,38.03],
}; */

var data = [
    {value:[91.11,29.97,24]},
    {value:[121.48,31.22,25]},
    {value:[114.07,22.62,50]},
    {value:[120.62,31.32,41]},
    {value:[109.511909,18,54]},
    {value:[118.78,32.04,67]},
    {value:[116.46,39.92,79]},
    {value:[115.72,37.72,80]},
    {value:[113.23,23.16,38]},
    {value:[123.38,41.8,50]},
    {value:[114.48,38.03,147]}
];

option = {
    // backgroundColor: '#404a59',
    title: {
        text: '全球twitter情绪指数',
        subtext: 'data from twitter',
        sublink: 'www.gayhub.com',
        left: 'center',
        textStyle: {
            color: '#fff'
        }
    },
    tooltip : {
        trigger: 'item',
        formatter: function(data){
            return data.value[2]
        }
    },
    bmap: {
        center: [104.114129, 37.550339],
        zoom: 5,
        roam: true,
        mapStyle: {
            styleJson: [
                    {
                        "featureType": "water",
                        "elementType": "all",
                        "stylers": {
                            "color": "#044161"
                        }
                    },
                    {
                        "featureType": "land",
                        "elementType": "all",
                        "stylers": {
                            "color": "#004981"
                        }
                    },
                    {
                        "featureType": "boundary",
                        "elementType": "geometry",
                        "stylers": {
                            "color": "#064f85"
                        }
                    },
                    {
                        "featureType": "railway",
                        "elementType": "all",
                        "stylers": {
                            "visibility": "off"
                        }
                    },
                    {
                        "featureType": "highway",
                        "elementType": "geometry",
                        "stylers": {
                            "color": "#004981"
                        }
                    },
                    {
                        "featureType": "highway",
                        "elementType": "geometry.fill",
                        "stylers": {
                            "color": "#005b96",
                            "lightness": 1
                        }
                    },
                    {
                        "featureType": "highway",
                        "elementType": "labels",
                        "stylers": {
                            "visibility": "off"
                        }
                    },
                    {
                        "featureType": "arterial",
                        "elementType": "geometry",
                        "stylers": {
                            "color": "#004981"
                        }
                    },
                    {
                        "featureType": "arterial",
                        "elementType": "geometry.fill",
                        "stylers": {
                            "color": "#00508b"
                        }
                    },
                    {
                        "featureType": "poi",
                        "elementType": "all",
                        "stylers": {
                            "visibility": "off"
                        }
                    },
                    {
                        "featureType": "green",
                        "elementType": "all",
                        "stylers": {
                            "color": "#056197",
                            "visibility": "off"
                        }
                    },
                    {
                        "featureType": "subway",
                        "elementType": "all",
                        "stylers": {
                            "visibility": "off"
                        }
                    },
                    {
                        "featureType": "manmade",
                        "elementType": "all",
                        "stylers": {
                            "visibility": "off"
                        }
                    },
                    {
                        "featureType": "local",
                        "elementType": "all",
                        "stylers": {
                            "visibility": "off"
                        }
                    },
                    {
                        "featureType": "arterial",
                        "elementType": "labels",
                        "stylers": {
                            "visibility": "off"
                        }
                    },
                    {
                        "featureType": "boundary",
                        "elementType": "geometry.fill",
                        "stylers": {
                            "color": "#029fd4"
                        }
                    },
                    {
                        "featureType": "building",
                        "elementType": "all",
                        "stylers": {
                            "color": "#1a5787"
                        }
                    },
                    {
                        "featureType": "label",
                        "elementType": "all",
                        "stylers": {
                            "visibility": "off"
                        }
                    }
            ]
        }
    },
    series : [
        {
            name: 'twitter',
            type: 'effectScatter',
            coordinateSystem: 'bmap',
            data: data,
            symbolSize: function (value) {
                return value[2] / 10;
            },
            //涟漪闪烁效果
            rippleEffect: { 
                period: 1,
                scale: 10,
                brushType: 'stroke'
            },
            //标点地名
            label: {
                normal: {
                    show: false
                },
                emphasis: {
                    show: false
                }
            },
            //标点样式
            itemStyle: {
                normal: {
                    color: '#ddb926'
                }
            }
        }
    ]
};;
if (option && typeof option === "object") {
    myChart.setOption(option, true);
}