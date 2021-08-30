package com.gistone.filereceive;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sun.rmi.runtime.Log;

import java.io.File;
import java.io.IOException;

@SpringBootApplication
@RestController
public class FileReceiveApp  implements CommandLineRunner {

   private static   Logger LOG = LoggerFactory.getLogger(FileReceiveApp.class);

    @Autowired
    FileReceiveConfig config;

    public static void main(String[] args) {
        SpringApplication.run(FileReceiveApp.class, args);
    }

    @GetMapping("/index")
    public String index() {
        return "这是一个文件放置传输器, 功能是把远程文件放到本地的某个路径";
    }

    @PostMapping("/put")
    public String put(@RequestPart("token") String token,
                      @RequestPart("file") MultipartFile file) {

        if (!config.getToken().equals(token)) {
            LOG.info("有人尝试错误的token: " + token);
            return "token not right";
        }
        try {
            String originalFilename = file.getOriginalFilename();
            file.transferTo(new File(config.getLocation() + originalFilename));
        } catch (IOException e) {
            LOG.error("文件接收异常");
            e.printStackTrace();
        }
        LOG.info(String.format("文件接收成功, 文件名: %s , 大小: %skb", file.getOriginalFilename(), file.getSize() / 1024));
        return "ok";
    }

    @Override
    public void run(String... args)  {
        LOG.info("传送文件路径: "+ config.getLocation());
        LOG.info("token: "+ config.getToken());
    }
}
