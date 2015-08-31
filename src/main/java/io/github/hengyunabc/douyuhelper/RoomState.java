package io.github.hengyunabc.douyuhelper;


public class RoomState {

  volatile boolean isDownloading = false;

  public boolean isDownloading() {
    return isDownloading;
  }

  public void setDownloading(boolean isDownloading) {
    this.isDownloading = isDownloading;
  }

}
