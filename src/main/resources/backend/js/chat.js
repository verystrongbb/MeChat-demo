'use strict';

var usernamePage = document.querySelector('#username-page');
var chatPage = document.querySelector('#chat-page');
var LUCKY_MONEY_BTN = document.getElementById("LuckyMoneyBtn");
var usernameForm = document.querySelector('#usernameForm');
var messageForm = document.querySelector('#messageForm');
var messageInput = document.querySelector('#message');
var moneyNumInput = document.querySelector('#MoneyNum');
var topicInput = document.querySelector('#topic');
var messageArea = document.querySelector('#messageArea');
var connectingElement = document.querySelector('.connecting');

var stompClient = null;
var myusername = null;
var totopic=null;
var userInfo=null;
var colors = [
    '#2196F3', '#32c787', '#00BCD4', '#ff5652',
    '#ffc107', '#ff85af', '#FF9800', '#39bbb0'
];
window.onload = function() {
    connect()
    // 在页面加载完成后执行的代码
};
//function connect(event)
function connect() {
    //username = document.querySelector('#name').value.trim();

    const userInfo = window.localStorage.getItem('userInfo')

    if (userInfo) {
        var un=JSON.parse(userInfo)

    }
    myusername=un.username


    if(myusername) {
        //usernamePage.classList.add('hidden');
        chatPage.classList.remove('hidden');

        var socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);

        stompClient.connect({}, onConnected, onError);
    }
    //event.preventDefault();
}


function onConnected() {
    // Subscribe to the Public Topic
    stompClient.subscribe('/topic/public', onMessageReceived);
    stompClient.subscribe('/topic/'+myusername, onMessageReceived);

    // Tell your username to the server
    stompClient.send("/app/chat.addUser",
        {},
        JSON.stringify({sender: myusername, type: 'JOIN'})
    )

    connectingElement.classList.add('hidden');
}


function onError(error) {
    connectingElement.textContent = 'Could not connect to WebSocket server. Please refresh this page to try again!';
    connectingElement.style.color = 'red';
}
function sendMoney(event) {
    //强制moneyNumInput输入为整数
    var moneyNum = moneyNumInput.value;
    var topicContent = topicInput.value;
    totopic=topicContent;
    // stompClient.subscribe('/topic/'+totopic, onMessageReceived);
    if(moneyNum && stompClient&&topicContent) {
        var chatMessage = {
            sender: myusername,
            content: "LuckyMoney",
            topic: totopic,
            type: 'MONEY',
            num: moneyNum,
            id: 0
        };
        stompClient.send("/app/chat.sendMoney", {}, JSON.stringify(chatMessage));
        moneyNumInput.value = '';topicInput.value = '';
    }
    event.preventDefault();

}

function sendMessage(event) {
    var messageContent = messageInput.value.trim();
    var topicContent = topicInput.value.trim();
    totopic=topicContent;
    // stompClient.subscribe('/topic/'+totopic, onMessageReceived);
    if(messageContent && stompClient&&topicContent) {
        var chatMessage = {
            sender: myusername,
            content: messageContent,
            topic: topicInput.value,
            num: 0,
            type: 'CHAT',
            id: 0
        };
        stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));
        messageInput.value = '';topicInput.value = '';
    }
    event.preventDefault();
}


function onMessageReceived(payload) {
    var message = JSON.parse(payload.body);

    var messageElement = document.createElement('li');

    if(message.type === 'JOIN') {
        messageElement.classList.add('event-message');
        message.content = message.sender + ' joined!';
    } else if (message.type === 'LEAVE') {
        messageElement.classList.add('event-message');
        message.content = message.sender + ' left!';
    }
    else if (message.type === 'MONEY') {
        messageElement.classList.add('chat-message');

        var avatarElement = document.createElement('i');
        var avatarText = document.createTextNode(message.sender[0]);
        avatarElement.appendChild(avatarText);
        avatarElement.style['background-color'] = getAvatarColor(message.sender);

        messageElement.appendChild(avatarElement);

        var usernameElement = document.createElement('span');
        var usernameText = document.createTextNode(message.sender+" (to "+message.topic+")\n");
        usernameElement.appendChild(usernameText);
        messageElement.appendChild(usernameElement);

        //页面上发送一个按钮，点击后调用robMoney并刷新按钮文本

        var button = document.createElement('button');
        button.innerHTML = "[Lucky Money: "+message.num+"]";
        button.onclick = function() {
            function robMoney(message) {
                var chatMessage = {
                    sender: myusername,
                    content: "robMoney",
                    num: message.num,
                    topic: myusername,
                    type: 'ROB',
                    id: message.id
                };
                stompClient.send("/app/chat.robMoney", {}, JSON.stringify(chatMessage));

            }
            robMoney(message);
        };
        messageElement.appendChild(button);
    }
    else {
        messageElement.classList.add('chat-message');

        var avatarElement = document.createElement('i');
        var avatarText = document.createTextNode(message.sender[0]);
        avatarElement.appendChild(avatarText);
        avatarElement.style['background-color'] = getAvatarColor(message.sender);

        messageElement.appendChild(avatarElement);

        var usernameElement = document.createElement('span');
        var usernameText = document.createTextNode(message.sender+" (to "+message.topic+")");
        usernameElement.appendChild(usernameText);
        messageElement.appendChild(usernameElement);
    }

    var textElement = document.createElement('p');
    var messageText = document.createTextNode(message.content);
    textElement.appendChild(messageText);

    messageElement.appendChild(textElement);

    messageArea.appendChild(messageElement);
    messageArea.scrollTop = messageArea.scrollHeight;
}


function getAvatarColor(messageSender) {
    var hash = 0;
    for (var i = 0; i < messageSender.length; i++) {
        hash = 31 * hash + messageSender.charCodeAt(i);
    }
    var index = Math.abs(hash % colors.length);
    return colors[index];
}

//usernameForm.addEventListener('submit', connect, true)
messageForm.addEventListener('submit', sendMessage, true)

LUCKY_MONEY_BTN.addEventListener("click",sendMoney)