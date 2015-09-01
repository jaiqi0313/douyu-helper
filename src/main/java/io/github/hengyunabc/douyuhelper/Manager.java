package io.github.hengyunabc.douyuhelper;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@Service
public class Manager {

  Map<String, RoomState> roomStateMap = Maps.newConcurrentMap();

  // flv下载链接的缓存
  // <room, url>
  Cache<String, String> urlCache = CacheBuilder.newBuilder()
      .expireAfterWrite(30 * 1000, TimeUnit.MILLISECONDS).maximumSize(1000).build();

  Timer timer = new Timer();

  @Autowired
  VideoDownloader videoDownloader;

  @PostConstruct
  public void init() {
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        List<String> rooms = getNeedDownloadRooms();
        for (String room : rooms) {
          RoomState roomState = roomStateMap.get(room);
          if (!roomState.isDownloading()) {
            String url = urlCache.getIfPresent(room);
            if (url != null) {
              //开始任务前，先把前一个下载的url缓存删掉
              urlCache.invalidate(room);
              videoDownloader.addDownloadTask(room, url);
            }
          }

        }
      }
    }, 0, 3000);
  }

  public void addRooms(List<String> rooms) {
    for (String room : rooms) {
      roomStateMap.put(room, new RoomState());
    }
  }

  /**
   * HttpServer获取到下载的url然后提交给Manager
   * 
   * @param url
   */
  public void addDownloadUrl(String url) {
    // 换成最高清的url，提取房间号
    // http://hdla.douyutv.com/live/6906ryHardOjl9Ng_550.flv?wsSecret=237f962f2600f9220997f98bb5ac57ef&wsTime=1440693587
    // 换成超清的url
    // 貌似这个不行。。去掉之后那个签名就不对了，下载不了视频了
    // url = StringUtils.remove(url, "_550");
    // url = StringUtils.remove(url, "_900");

    // 提取房间号
    int index = StringUtils.indexOf(url, "/live/");
    if (index > 0) {
      String roomStartString = url.substring(index + "/live/".length());
      StringBuffer sb = new StringBuffer();
      for (char c : roomStartString.toCharArray()) {
        if (CharUtils.isAsciiNumeric(c)) {
          sb.append(c);
        } else {
          break;
        }
      }
      urlCache.put(sb.toString(), url);
    }

  }

  /**
   * 获取到那些没有在下载的房间的列表
   * 
   * @return
   */
  public List<String> getNeedDownloadRooms() {
    List<String> result = Lists.newLinkedList();
    for (Entry<String, RoomState> entry : roomStateMap.entrySet()) {
      String room = entry.getKey();
      RoomState roomState = entry.getValue();
      if (!roomState.isDownloading()) {
        result.add(room);
      }
    }
    return result;
  }

  /**
   * 获取下载房间的许可，由VideoDownloader来获取
   * 
   * @param room
   */
  synchronized public boolean getDownloadPermit(String room) {
    RoomState roomState = roomStateMap.get(room);
    if (roomState != null && roomState.isDownloading() == false) {
      roomState.setDownloading(true);
      return true;
    }
    return false;
  }

  synchronized public void returnDownloadPermit(String room) {
    RoomState roomState = roomStateMap.get(room);
    if (roomState != null) {
      roomState.setDownloading(false);
    }
  }
}
