package com.gistone.filereceive;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@SpringBootApplication
@RestController
public class FileReceiveApp implements CommandLineRunner {

    private static Logger LOG = LoggerFactory.getLogger(FileReceiveApp.class);

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

    // todo : 每隔10分钟自动生成token
    // todo: 文件重名策略: 覆盖/重命名/拒绝?  默认覆盖

    // 怎么才可以用配置暴露接口呢?
    @PostMapping("/api/put")
    public String apiput(@RequestPart("token") String token,
                         @RequestPart("files") List<MultipartFile> files) {

        if (!config.getToken().equals(token)) {
            LOG.info("有人尝试错误的token: " + token);
            return "token not right";
        }
        for (MultipartFile file : files) {
            String originalFilename = file.getOriginalFilename();
            try {
                file.transferTo(new File(config.getLocation() + originalFilename));
            } catch (IOException e) {
                LOG.error("文件接收异常, file name: " + file);
                e.printStackTrace();
            }
            LOG.info(String.format("文件接收成功, 文件名: %s , 大小: %skb", file.getOriginalFilename(), file.getSize() / 1024));
        }
        return "ok";
    }

    @Override
    public void run(String... args) {
        LOG.info("传送文件路径: " + config.getLocation());
        LOG.info("token: " + config.getToken());
        LOG.info("使用方式: POST host:port/put multipart/form token file");
    }
}
