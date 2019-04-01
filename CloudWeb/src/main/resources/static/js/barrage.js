// jquery
$(document).ready(function () {
    console.log("页面已经加载完毕");

    let canvasEle = document.querySelector('canvas')
    let barrage = new Barrage(canvasEle, 100, 10)
    let inputEle = document.querySelector('.barrage-input')
    document.querySelector('.send-primary-btn').onclick = function() {
        barrage.pushMessage({text: inputEle.value,color:'white',speed:2})
        inputEle.value = ''
    }

    document.querySelector('.clear-btn').onclick = function() {
        barrage.clear()
    }
    document.querySelector('.close-btn').onclick = function() {
        barrage.close()
    }
    document.querySelector('.open-btn').onclick = function() {
        barrage.open()
    }

})
