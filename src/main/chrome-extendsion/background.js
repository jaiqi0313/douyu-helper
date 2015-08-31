var roomsToOpen = [];

var tabToOpen;

// 标记当前是不是有打开房间的任务在跑
var bOpeningRoom = false;

chrome.tabs.onRemoved.addListener(function(tabId, removeInfo) {
	if (tabToOpen && tabToOpen.id === tabId) {
		tabToOpen = null;
	}
});

var openRoom = function() {
	console.log('roomsToOpen:' + roomsToOpen);
	if (roomsToOpen && roomsToOpen.length <= 0) {
		return;
	}

	if (!tabToOpen) {
		chrome.tabs.create({
			url : 'about:blank',
			active : false,
			index : 0
		}, function(tab) {
			tabToOpen = tab;
		});
		return;
	}
	// 如果上一个房间还在打开状态，则跳过
	if (bOpeningRoom) {
		return;
	}

	var room = roomsToOpen.pop();
	if (room) {
		bOpeningRoom = true;
		chrome.tabs.update(tabToOpen.id, {
			url : 'http://www.douyutv.com/' + room
		}, function() {
			// 打开房间后，5秒重新切换回空白页
			window.setTimeout(function() {
				chrome.tabs.update(tabToOpen.id, {
					url : 'about:blank'
				});
				bOpeningRoom = false;
			}, 5000);

		});
	}

	console.log(tabToOpen);
}

window.setInterval(openRoom, 1 * 1000);

var getRooms = function() {
	// 如果当前没有房间要处理的，则从服务器拉取
	if (roomsToOpen.length > 0) {
		return;
	}
	console.log('getRooms');
	$.getJSON("http://127.0.0.1:7373/douyu/video/rooms").done(function(rooms) {
		console.log('rooms:' + rooms);
		// 把从服务器拉取的房间列表放到任务数组里
		rooms.forEach(function(room, index, array) {
			roomsToOpen.push(room);
		});

	});
}

window.setInterval(getRooms, 3 * 1000);

console.log("background");

chrome.webRequest.onBeforeRequest.addListener(function(info) {
	console.log("Cat intercepted: " + info.url);
	$.post("http://127.0.0.1:7373/douyu/video/url", {
		url : info.url
	});
},
// filters
{
	urls : [ "http://*.douyutv.com/live/*" ]
// types: ["image"]
},
// extraInfoSpec
[ "blocking" ]);
