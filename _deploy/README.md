## 一、部署说明

### 1、docker 部署方式

```
docker run -p 18602:18602 -p 8602:8602 noearorg/folkmq-server:1.0.10 
```

### 2、docker-compose 部署方式

下载 `docker-compose.yml` 文件，通过 `docker-compose up` 启动服务


### 3、jar 部署方式

编译源码提到 `folkmq-server/target/folkmq-server.jar`，运行指令：

```
java -jar folkmq-server.jar
```

## 二、端口说明

| 端口    |          |
|-------|----------|
| 8602  | 管理端口     |
| 18602 | 消息服务端口   |
