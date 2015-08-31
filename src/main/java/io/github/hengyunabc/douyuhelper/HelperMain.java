package io.github.hengyunabc.douyuhelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.support.GenericXmlApplicationContext;

import com.google.common.collect.Lists;

public class HelperMain {
  public static void main(String[] args) throws IOException {

    GenericXmlApplicationContext ctx = new GenericXmlApplicationContext();
    ctx.load("spring-context-helper.xml");
    ctx.refresh();

    List<String> rooms = Lists.newLinkedList();

    if (args != null && args.length > 0) {
      for (String room : args) {
        rooms.add(room.trim());
      }
    } else {
      System.out.println("请输入数字房间号，可以同时输入多个，以空格或者英文逗号分隔：");
      BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
      String line = br.readLine();
      line = StringUtils.replaceChars(line, ',', ' ');
      String[] split = StringUtils.split(line, ' ');
      for (String room : split) {
        rooms.add(room.trim());
      }
    }
    if (rooms.isEmpty()) {
      System.err.println("请输入房间号！");
      System.exit(-1);
    }

    Manager manager = ctx.getBean(Manager.class);
    manager.addRooms(rooms);
  }
}
