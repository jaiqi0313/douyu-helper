package io.github.hengyunabc.douyuhelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VideoDownloader {
  static final Logger logger = LoggerFactory.getLogger(VideoDownloader.class);

  ExecutorService executorService = Executors.newCachedThreadPool();
  CloseableHttpClient httpclient;



  @Autowired
  Manager manager;

  @PostConstruct
  public void init() {
    RequestConfig requestConfig =
        RequestConfig.custom().setConnectTimeout(5 * 1000).setConnectionRequestTimeout(5 * 1000)
            .setSocketTimeout(5 * 1000).build();
    PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();

    httpclient =
        HttpClients.custom().setConnectionManager(connManager)
            .setDefaultRequestConfig(requestConfig).build();
  }

  @PreDestroy
  public void destory() throws IOException {
    httpclient.close();
    executorService.shutdown();
  }

  public boolean isDownloading(String room) {
    return false;
  }

  public void addDownloadTask(final String room, final String url) {
    executorService.submit(new Runnable() {
      @Override
      public void run() {
        // 先尝试获取下载的许可
        if (manager.getDownloadPermit(room) == false) {
          return;
        }

        int downloadedSize = 0;
        logger.info("开始下载房间：{}，url: {}", room, url);
        try (CloseableHttpResponse response = httpclient.execute(new HttpGet(url))) {

          if (response.getStatusLine().getStatusCode() == 200) {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
              InputStream inputStream = entity.getContent();
              SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
              Paths.get("video", room).toFile().mkdirs();
              Path path = Paths.get("video", room, simpleDateFormat.format(new Date()) + ".flv");
              File flvFile = path.toFile();
              FileOutputStream outstream = new FileOutputStream(flvFile);
              byte[] buffer = new byte[128 * 1024];
              int size = 0;

              Date last = new Date();
              while ((size = inputStream.read(buffer)) != -1) {
                outstream.write(buffer, 0, size);
                downloadedSize += size;
                Date now = new Date();
                if (((now.getTime() - last.getTime()) / 1000) >= 5) {
                  logger.info("正在下载房间：{}，已下载: {}", room, downloadedSize);
                  last = now;
                }
              }

            }

          }
        } catch (Exception e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } finally {
          manager.returnDownloadPermit(room);
          logger.info("结束下载房间：{}，url: {}", room, url);
        }
      }
    });
  }

  public static void test() throws ClientProtocolException, IOException {
    File myFile = new File("mystuff.bin");

    CloseableHttpClient client = HttpClients.createDefault();
    try (CloseableHttpResponse response =
        client
            .execute(new HttpGet(
                "http://hdl3a.douyutv.com/live/6540rcyiWsxBW8yd_550.flv?wsSecret=127040319200e6b26a82cba954a7f7e8&wsTime=1440905148"))) {

      if (response.getStatusLine().getStatusCode() == 200) {
        HttpEntity entity = response.getEntity();
        if (entity != null) {
          InputStream inputStream = entity.getContent();
          FileOutputStream outstream = new FileOutputStream(myFile);
          byte[] buffer = new byte[128 * 1024];
          int size = 0;
          while ((size = inputStream.read(buffer)) != -1) {
            outstream.write(buffer, 0, size);
          }
        }

      }
    }
  }

}
