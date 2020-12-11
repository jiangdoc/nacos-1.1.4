## 部署nacos

```java
// 1、下载源码
git clone https://github.com/alibaba/nacos.git

cd nacos/
// 2、安装
mvn -Prelease-nacos -Dmaven.test.skip=true clean install -U  
ls -al distribution/target/

// 3、change the $version to your actual path
cd distribution/target/nacos-server-$version/nacos/bin

// 4、执行Linux/Unix/Mac
//启动命令(standalone代表着单机模式运行，非集群模式):

sh startup.sh -m standalone

// 5、关闭服务器Linux/Unix/Mac
sh shutdown.sh
```
## 服务注册&发现和配置管理
### 服务注册
> curl -X POST 'http://127.0.0.1:8848/nacos/v1/ns/instance?serviceName=nacos.naming.serviceName&ip=20.18.7.10&port=8080'

### 服务发现
> curl -X GET 'http://127.0.0.1:8848/nacos/v1/ns/instance/list?serviceName=nacos.naming.serviceName'

### 发布配置
> curl -X POST "http://127.0.0.1:8848/nacos/v1/cs/configs?dataId=nacos.cfg.dataId&group=test&content=HelloWorld"

### 获取配置
> curl -X GET "http://127.0.0.1:8848/nacos/v1/cs/configs?dataId=nacos.cfg.dataId&group=test"

## 可视化管理
访问地址
http://127.0.0.1:8848/nacos/#/login
user:nacos password:nacos

