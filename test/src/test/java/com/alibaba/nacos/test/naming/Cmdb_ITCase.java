/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.nacos.test.naming;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;

import org.junit.Ignore;
import org.springframework.http.HttpMethod;
import com.alibaba.nacos.naming.NamingApp;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import static com.alibaba.nacos.test.naming.NamingBase.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NamingApp.class, properties = {"server.servlet.context-path=/nacos",
    "server.port=7001"},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Ignore
public class Cmdb_ITCase {

    private NamingService naming;
    private URL base;
    public static final long TIME_OUT = 3000;

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    @Before
    public void setUp() throws Exception {
        String url = String.format("http://%s:%s", TEST_IP_4_DOM_1, port);
        this.base = new URL(url);

        if (naming == null) {
            naming = NamingFactory.createNamingService(TEST_IP_4_DOM_1 + ":" + port);
        }
    }

    @After
    public void cleanup() throws Exception {
    }

    /**
     * @throws Exception
     * @TCDescription : cmdb注册的label,同机房优先
     */
    @Test
    public void cmdb_getInstanceList_1() throws Exception {
        String serviceName = randomDomainName();
        System.out.println(serviceName);
        namingServiceCreate(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP);

        instanceRegister(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "11.11.11.11", String.valueOf(TEST_PORT_4_DOM_1), "c1");
        instanceRegister(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "22.22.22.22", String.valueOf(TEST_PORT_4_DOM_1), "c1");
        instanceRegister(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "33.33.33.33", String.valueOf(TEST_PORT_4_DOM_1), "c2");
        instanceRegister(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "44.44.44.44", String.valueOf(TEST_PORT_4_DOM_1), "c2");
        instanceRegister(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "55.55.55.55", String.valueOf(TEST_PORT_4_DOM_1), "c1");
        instanceRegister(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "66.66.66.66", String.valueOf(TEST_PORT_4_DOM_1), "c1");

        String serviceName2 = randomDomainName();
        namingServiceCreate(serviceName2, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP);
        instanceRegister(serviceName2, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "77.77.77.77", String.valueOf(TEST_PORT_4_DOM_1), "c2");
        instanceRegister(serviceName2, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "88.88.88.88", String.valueOf(TEST_PORT_4_DOM_1), "c2");

        TimeUnit.SECONDS.sleep(5L);

        JSONObject json = new JSONObject();
        json.put("type", "label");
        json.put("expression", "CONSUMER.label.label1 = PROVIDER.label.label1");
        ResponseEntity<String> httpResult = request("/nacos/v1/ns/service",
            Params.newParams()
                .appendParam("serviceName", serviceName)
                .appendParam("protectThreshold", "0")
                .appendParam("selector", json.toJSONString())
                .done(),
            String.class,
            HttpMethod.PUT);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, httpResult.getStatusCodeValue());

        httpResult = request("/nacos/v1/ns/instance/list",
            Params.newParams()
                .appendParam("serviceName", serviceName)
                .appendParam("clientIP", "11.11.11.11")
                .done(),
            String.class,
            HttpMethod.GET);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, httpResult.getStatusCodeValue());
        json = JSON.parseObject(httpResult.getBody());
        Assert.assertEquals(1, json.getJSONArray("hosts").size());
    }


    /**
     * @throws Exception
     * @TCDescription : cmdb未注册的label,获取所有的instance
     */
    @Test
    public void cmdb_getInstanceList_2() throws Exception {
        String serviceName = randomDomainName();

        namingServiceCreate(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP);

        instanceRegister(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "11.11.11.11", String.valueOf(TEST_PORT_4_DOM_1), "c1");
        instanceRegister(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "22.22.22.22", String.valueOf(TEST_PORT_4_DOM_1), "c1");
        instanceRegister(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "33.33.33.33", String.valueOf(TEST_PORT_4_DOM_1), "c2");
        instanceRegister(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "44.44.44.44", String.valueOf(TEST_PORT_4_DOM_1), "c2");
        instanceRegister(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "55.55.55.55", String.valueOf(TEST_PORT_4_DOM_1), "c1");
        instanceRegister(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "66.66.66.66", String.valueOf(TEST_PORT_4_DOM_1), "c1");

        String serviceName2 = randomDomainName();
        namingServiceCreate(serviceName2, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP);
        instanceRegister(serviceName2, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "77.77.77.77", String.valueOf(TEST_PORT_4_DOM_1), "c2");
        instanceRegister(serviceName2, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "88.88.88.88", String.valueOf(TEST_PORT_4_DOM_1), "c2");

        TimeUnit.SECONDS.sleep(10);

        JSONObject json = new JSONObject();
        json.put("type", "label");
        json.put("expression", "CONSUMER.label.label1 = PROVIDER.label.label1");
        ResponseEntity<String> httpResult = request("/nacos/v1/ns/service",
            Params.newParams()
                .appendParam("serviceName", serviceName)
                .appendParam("protectThreshold", "0")
                .appendParam("selector", json.toJSONString())
                .done(),
            String.class,
            HttpMethod.PUT);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, httpResult.getStatusCodeValue());

        httpResult = request("/nacos/v1/ns/instance/list",
            Params.newParams()
                .appendParam("serviceName", serviceName)
                .done(),
            String.class,
            HttpMethod.GET);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, httpResult.getStatusCodeValue());
        json = JSON.parseObject(httpResult.getBody());
        Assert.assertEquals(6, json.getJSONArray("hosts").size());
    }

    /**
     * @throws Exception
     * @TCDescription : cmdb规则不同,根据IP获取优先的instance
     */
    @Test
    public void cmdb_getInstanceList_3() throws Exception {
        String serviceName = randomDomainName();

        namingServiceCreate(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP);

        instanceRegister(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "11.11.11.11", String.valueOf(TEST_PORT_4_DOM_1), "c1");
        instanceRegister(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "22.22.22.22", String.valueOf(TEST_PORT_4_DOM_1), "c1");
        instanceRegister(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "33.33.33.33", String.valueOf(TEST_PORT_4_DOM_1), "c2");
        instanceRegister(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "44.44.44.44", String.valueOf(TEST_PORT_4_DOM_1), "c2");
        instanceRegister(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "55.55.55.55", String.valueOf(TEST_PORT_4_DOM_1), "c1");
        instanceRegister(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "66.66.66.66", String.valueOf(TEST_PORT_4_DOM_1), "c1");

        String serviceName2 = randomDomainName();
        namingServiceCreate(serviceName2, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP);
        instanceRegister(serviceName2, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "77.77.77.77", String.valueOf(TEST_PORT_4_DOM_1), "c2");
        instanceRegister(serviceName2, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "88.88.88.88", String.valueOf(TEST_PORT_4_DOM_1), "c2");

        TimeUnit.SECONDS.sleep(10);

        JSONObject json = new JSONObject();
        json.put("type", "label");
        json.put("expression", "CONSUMER.label.label1 = PROVIDER.label.label1 & CONSUMER.label.label2 = PROVIDER.label.label2");

        ResponseEntity<String> httpResult = request("/nacos/v1/ns/service",
            Params.newParams()
                .appendParam("serviceName", serviceName)
                .appendParam("protectThreshold", "0")
                .appendParam("selector", json.toJSONString())
                .done(),
            String.class,
            HttpMethod.PUT);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, httpResult.getStatusCodeValue());

        httpResult = request("/nacos/v1/ns/instance/list",
            Params.newParams()
                .appendParam("serviceName", serviceName)
                .appendParam("clientIP", "66.66.66.66")
                .done(),
            String.class,
            HttpMethod.GET);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, httpResult.getStatusCodeValue());
        json = JSON.parseObject(httpResult.getBody());
        System.out.println("instance list = " + json);
        Assert.assertEquals(2, json.getJSONArray("hosts").size());
    }

    /**
     * @throws Exception
     * @TCDescription : cmdb规则不同,对不同的serviceName的不影响
     */
    @Test
    public void cmdb_getInstanceList_4() throws Exception {
        String serviceName = randomDomainName();
        String serviceName2 = randomDomainName();
        System.out.println(serviceName);

        namingServiceCreate(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP);

        instanceRegister(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "11.11.11.11", String.valueOf(TEST_PORT_4_DOM_1), "c1");
        instanceRegister(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "22.22.22.22", String.valueOf(TEST_PORT_4_DOM_1), "c1");
        instanceRegister(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "33.33.33.33", String.valueOf(TEST_PORT_4_DOM_1), "c2");
        instanceRegister(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "44.44.44.44", String.valueOf(TEST_PORT_4_DOM_1), "c2");
        instanceRegister(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "55.55.55.55", String.valueOf(TEST_PORT_4_DOM_1), "c1");
        instanceRegister(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "66.66.66.66", String.valueOf(TEST_PORT_4_DOM_1), "c1");

        namingServiceCreate(serviceName2, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP);
        instanceRegister(serviceName2, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "77.77.77.77", String.valueOf(TEST_PORT_4_DOM_1), "c2");
        instanceRegister(serviceName2, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "88.88.88.88", String.valueOf(TEST_PORT_4_DOM_1), "c2");

        TimeUnit.SECONDS.sleep(10);

        JSONObject json = new JSONObject();
        json.put("type", "label");
        json.put("expression", "CONSUMER.label.label1 = PROVIDER.label.label1 & CONSUMER.label.label2 = PROVIDER.label.label2");
        ResponseEntity<String> httpResult = request("/nacos/v1/ns/service",
            Params.newParams()
                .appendParam("serviceName", serviceName)
                .appendParam("protectThreshold", "0")
                .appendParam("selector", json.toJSONString())
                .done(),
            String.class,
            HttpMethod.PUT);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, httpResult.getStatusCodeValue());

        httpResult = request("/nacos/v1/ns/service",
            Params.newParams()
                .appendParam("serviceName", serviceName)
                .appendParam("namespaceId", Constants.DEFAULT_NAMESPACE_ID)
                .done(),
            String.class,
            HttpMethod.GET);
        System.out.println("service list = " + JSON.parseObject(httpResult.getBody()));

        httpResult = request("/nacos/v1/ns/instance/list",
            Params.newParams()
                .appendParam("serviceName", serviceName)
                .appendParam("clientIP", "66.66.66.66")
                .done(),
            String.class,
            HttpMethod.GET);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, httpResult.getStatusCodeValue());
        json = JSON.parseObject(httpResult.getBody());

        System.out.println("instance list = " + json);
        Assert.assertEquals(2, json.getJSONArray("hosts").size());
    }


    /**
     * @throws Exception
     * @TCDescription : cmdb规则不同,根据IP获取优先的instance,对不同的serviceName的不影响
     */
    @Test
    public void cmdb_getInstanceList_5() throws Exception {
        String serviceName = randomDomainName();
        String serviceName2 = randomDomainName();
        System.out.println(serviceName);

        namingServiceCreate(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP);

        instanceRegister(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "11.11.11.11", String.valueOf(TEST_PORT_4_DOM_1), "c1");
        instanceRegister(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "22.22.22.22", String.valueOf(TEST_PORT_4_DOM_1), "c1");
        instanceRegister(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "33.33.33.33", String.valueOf(TEST_PORT_4_DOM_1), "c2");
        instanceRegister(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "44.44.44.44", String.valueOf(TEST_PORT_4_DOM_1), "c2");
        instanceRegister(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "55.55.55.55", String.valueOf(TEST_PORT_4_DOM_1), "c1");
        instanceRegister(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "66.66.66.66", String.valueOf(TEST_PORT_4_DOM_1), "c1");

        namingServiceCreate(serviceName2, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP);
        instanceRegister(serviceName2, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "77.77.77.77", String.valueOf(TEST_PORT_4_DOM_1), "c2");
        instanceRegister(serviceName2, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "88.88.88.88", String.valueOf(TEST_PORT_4_DOM_1), "c2");

        TimeUnit.SECONDS.sleep(10);

        JSONObject json = new JSONObject();
        json.put("type", "label");
        json.put("expression", "CONSUMER.label.label1 = PROVIDER.label.label1 & CONSUMER.label.label2 = PROVIDER.label.label2");
        ResponseEntity<String> httpResult = request("/nacos/v1/ns/service",
            Params.newParams()
                .appendParam("serviceName", serviceName)
                .appendParam("protectThreshold", "0")
                .appendParam("selector", json.toJSONString())
                .done(),
            String.class,
            HttpMethod.PUT);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, httpResult.getStatusCodeValue());

        httpResult = request("/nacos/v1/ns/instance/list",
            Params.newParams()
                .appendParam("serviceName", serviceName)
                .appendParam("clientIP", "77.77.77.77")
                .done(),
            String.class,
            HttpMethod.GET);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, httpResult.getStatusCodeValue());
        json = JSON.parseObject(httpResult.getBody());
        System.out.println("instance list = " + json);
        Assert.assertEquals(6, json.getJSONArray("hosts").size());
    }

    /**
     * @throws Exception
     * @TCDescription : cmdb规则不同,selector为空时
     */
    @Test
    public void cmdb_getInstanceList_6() throws Exception {
        String serviceName = randomDomainName();
        String serviceName2 = randomDomainName();
        System.out.println(serviceName);

        namingServiceCreate(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP);

        instanceRegister(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "11.11.11.11", String.valueOf(TEST_PORT_4_DOM_1), "c1");
        instanceRegister(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "22.22.22.22", String.valueOf(TEST_PORT_4_DOM_1), "c1");
        instanceRegister(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "33.33.33.33", String.valueOf(TEST_PORT_4_DOM_1), "c2");
        instanceRegister(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "44.44.44.44", String.valueOf(TEST_PORT_4_DOM_1), "c2");
        instanceRegister(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "55.55.55.55", String.valueOf(TEST_PORT_4_DOM_1), "c1");
        instanceRegister(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "66.66.66.66", String.valueOf(TEST_PORT_4_DOM_1), "c1");

        namingServiceCreate(serviceName2, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP);
        instanceRegister(serviceName2, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "77.77.77.77", String.valueOf(TEST_PORT_4_DOM_1), "c2");
        instanceRegister(serviceName2, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "88.88.88.88", String.valueOf(TEST_PORT_4_DOM_1), "c2");

        TimeUnit.SECONDS.sleep(10);

        JSONObject json = new JSONObject();
        json.put("type", "label");
        json.put("expression", "CONSUMER.label.label1 = PROVIDER.label.label1 & CONSUMER.label.label2 = PROVIDER.label.label2");
        ResponseEntity<String> httpResult = request("/nacos/v1/ns/service",
            Params.newParams()
                .appendParam("serviceName", serviceName)
                .appendParam("protectThreshold", "0")
                .appendParam("selector", json.toJSONString())
                .done(),
            String.class,
            HttpMethod.PUT);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, httpResult.getStatusCodeValue());

        httpResult = request("/nacos/v1/ns/instance/list",
            Params.newParams()
                .appendParam("serviceName", serviceName)
                .appendParam("clientIP", "11.11.11.11")
                .done(),
            String.class,
            HttpMethod.GET);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, httpResult.getStatusCodeValue());
        json = JSON.parseObject(httpResult.getBody());

        System.out.println("instance list = " + json);
        Assert.assertEquals(1, json.getJSONArray("hosts").size());
    }

    /**
     * @throws Exception
     * @TCDescription : cmdb规则不同,selector规则改变
     */
    @Test
    public void cmdb_getInstanceList_7() throws Exception {
        String serviceName = randomDomainName();
        String serviceName2 = randomDomainName();
        System.out.println(serviceName);

        namingServiceCreate(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP);

        instanceRegister(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "11.11.11.11", String.valueOf(TEST_PORT_4_DOM_1), "c1");
        instanceRegister(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "22.22.22.22", String.valueOf(TEST_PORT_4_DOM_1), "c1");
        instanceRegister(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "33.33.33.33", String.valueOf(TEST_PORT_4_DOM_1), "c2");
        instanceRegister(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "44.44.44.44", String.valueOf(TEST_PORT_4_DOM_1), "c2");
        instanceRegister(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "55.55.55.55", String.valueOf(TEST_PORT_4_DOM_1), "c1");
        instanceRegister(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "66.66.66.66", String.valueOf(TEST_PORT_4_DOM_1), "c1");

        namingServiceCreate(serviceName2, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP);
        instanceRegister(serviceName2, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "77.77.77.77", String.valueOf(TEST_PORT_4_DOM_1), "c2");
        instanceRegister(serviceName2, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "88.88.88.88", String.valueOf(TEST_PORT_4_DOM_1), "c2");


        TimeUnit.SECONDS.sleep(10);

        JSONObject json = new JSONObject();
        json.put("type", "label");
        json.put("expression", "CONSUMER.label.label1 = PROVIDER.label.label1 & CONSUMER.label.label2 = PROVIDER.label.label2");
        List<String> params = Arrays.asList("serviceName", serviceName, "protectThreshold", "0", "selector", json.toString());


        ResponseEntity<String> httpResult = request("/nacos/v1/ns/service",
            Params.newParams()
                .appendParam("serviceName", serviceName)
                .appendParam("protectThreshold", "0")
                .appendParam("selector", json.toJSONString())
                .done(),
            String.class,
            HttpMethod.PUT);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, httpResult.getStatusCodeValue());

        httpResult = request("/nacos/v1/ns/instance/list",
            Params.newParams()
                .appendParam("serviceName", serviceName)
                .appendParam("clientIP", "11.11.11.11")
                .done(),
            String.class,
            HttpMethod.GET);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, httpResult.getStatusCodeValue());
        json = JSON.parseObject(httpResult.getBody());
        System.out.println("instance list = " + json);
        Assert.assertEquals(1, json.getJSONArray("hosts").size());

        httpResult = request("/nacos/v1/ns/service",
            Params.newParams()
                .appendParam("serviceName", serviceName)
                .appendParam("protectThreshold", "0")
                .appendParam("selector", "")
                .done(),
            String.class,
            HttpMethod.PUT);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, httpResult.getStatusCodeValue());

        httpResult = request("/nacos/v1/ns/instance/list",
            Params.newParams()
                .appendParam("serviceName", serviceName)
                .appendParam("clientIP", "11.11.11.11")
                .done(),
            String.class,
            HttpMethod.GET);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, httpResult.getStatusCodeValue());
        json = JSON.parseObject(httpResult.getBody());

        System.out.println("instance list = " + json);
        Assert.assertEquals(6, json.getJSONArray("hosts").size());
    }


    /**
     * @throws Exception
     * @TCDescription : cmdb规则不同,expression为空
     */
    @Test
    public void cmdb_getInstanceList_8() throws Exception {
        String serviceName = randomDomainName();
        String serviceName2 = randomDomainName();
        System.out.println(serviceName);

        namingServiceCreate(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP);

        instanceRegister(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "11.11.11.11", String.valueOf(TEST_PORT_4_DOM_1), "c1");
        instanceRegister(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "22.22.22.22", String.valueOf(TEST_PORT_4_DOM_1), "c1");
        instanceRegister(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "33.33.33.33", String.valueOf(TEST_PORT_4_DOM_1), "c2");
        instanceRegister(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "44.44.44.44", String.valueOf(TEST_PORT_4_DOM_1), "c2");
        instanceRegister(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "55.55.55.55", String.valueOf(TEST_PORT_4_DOM_1), "c1");
        instanceRegister(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "66.66.66.66", String.valueOf(TEST_PORT_4_DOM_1), "c1");

        namingServiceCreate(serviceName2, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP);
        instanceRegister(serviceName2, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "77.77.77.77", String.valueOf(TEST_PORT_4_DOM_1), "c2");
        instanceRegister(serviceName2, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "88.88.88.88", String.valueOf(TEST_PORT_4_DOM_1), "c2");

        TimeUnit.SECONDS.sleep(10);

        JSONObject json = new JSONObject();
        json.put("type", "label");
        json.put("expression", "");
        ResponseEntity<String> httpResult = request("/nacos/v1/ns/service",
            Params.newParams()
                .appendParam("serviceName", serviceName)
                .appendParam("protectThreshold", "0")
                .appendParam("selector", json.toJSONString())
                .done(),
            String.class,
            HttpMethod.PUT);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, httpResult.getStatusCodeValue());

        httpResult = request("/nacos/v1/ns/instance/list",
            Params.newParams()
                .appendParam("serviceName", serviceName)
                .appendParam("clientIP", "11.11.11.11")
                .done(),
            String.class,
            HttpMethod.GET);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, httpResult.getStatusCodeValue());
        json = JSON.parseObject(httpResult.getBody());
        System.out.println("instance list = " + json);
        Assert.assertEquals(6, json.getJSONArray("hosts").size());
    }

    /**
     * @throws Exception
     * @TCDescription : cmdb规则不同,expression为null,获取所有的instance
     */
    @Test
    public void cmdb_getInstanceList_9() throws Exception {
        String serviceName = randomDomainName();
        String serviceName2 = randomDomainName();
        System.out.println(serviceName);

        namingServiceCreate(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP);

        instanceRegister(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "11.11.11.11", String.valueOf(TEST_PORT_4_DOM_1), "c1");
        instanceRegister(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "22.22.22.22", String.valueOf(TEST_PORT_4_DOM_1), "c1");
        instanceRegister(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "33.33.33.33", String.valueOf(TEST_PORT_4_DOM_1), "c2");
        instanceRegister(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "44.44.44.44", String.valueOf(TEST_PORT_4_DOM_1), "c2");
        instanceRegister(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "55.55.55.55", String.valueOf(TEST_PORT_4_DOM_1), "c1");
        instanceRegister(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "66.66.66.66", String.valueOf(TEST_PORT_4_DOM_1), "c1");

        namingServiceCreate(serviceName2, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP);
        instanceRegister(serviceName2, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "77.77.77.77", String.valueOf(TEST_PORT_4_DOM_1), "c2");
        instanceRegister(serviceName2, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "88.88.88.88", String.valueOf(TEST_PORT_4_DOM_1), "c2");


        TimeUnit.SECONDS.sleep(10);

        JSONObject json = new JSONObject();
        json.put("type", "label");
        json.put("expression", "");
        ResponseEntity<String> httpResult = request("/nacos/v1/ns/service",
            Params.newParams()
                .appendParam("serviceName", serviceName)
                .appendParam("protectThreshold", "0")
                .appendParam("selector", json.toJSONString())
                .done(),
            String.class,
            HttpMethod.PUT);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, httpResult.getStatusCodeValue());

        httpResult = request("/nacos/v1/ns/instance/list",
            Params.newParams()
                .appendParam("serviceName", serviceName)
                .appendParam("clientIP", "11.11.11.11")
                .done(),
            String.class,
            HttpMethod.GET);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, httpResult.getStatusCodeValue());
        json = JSON.parseObject(httpResult.getBody());
        System.out.println("instance list = " + json);
        Assert.assertEquals(6, json.getJSONArray("hosts").size());
    }

    /**
     * @throws Exception
     * @TCDescription : cmdb规则不同,type为label填写异常
     */
    @Test
    public void cmdb_getInstanceList_10() throws Exception {
        String serviceName = randomDomainName();
        String serviceName2 = randomDomainName();
        System.out.println(serviceName);

        namingServiceCreate(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP);

        instanceRegister(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "11.11.11.11", String.valueOf(TEST_PORT_4_DOM_1), "c1");
        instanceRegister(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "22.22.22.22", String.valueOf(TEST_PORT_4_DOM_1), "c1");
        instanceRegister(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "33.33.33.33", String.valueOf(TEST_PORT_4_DOM_1), "c2");
        instanceRegister(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "44.44.44.44", String.valueOf(TEST_PORT_4_DOM_1), "c2");
        instanceRegister(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "55.55.55.55", String.valueOf(TEST_PORT_4_DOM_1), "c1");
        instanceRegister(serviceName, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "66.66.66.66", String.valueOf(TEST_PORT_4_DOM_1), "c1");

        namingServiceCreate(serviceName2, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP);
        instanceRegister(serviceName2, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "77.77.77.77", String.valueOf(TEST_PORT_4_DOM_1), "c2");
        instanceRegister(serviceName2, Constants.DEFAULT_NAMESPACE_ID, Constants.DEFAULT_GROUP, "88.88.88.88", String.valueOf(TEST_PORT_4_DOM_1), "c2");

        TimeUnit.SECONDS.sleep(10);

        JSONObject json = new JSONObject();
        json.put("type", "label1");
        json.put("expression", "CONSUMER.label.label1 = PROVIDER.label.label1 & CONSUMER.label.label2 = PROVIDER.label.label2");
        ResponseEntity<String> httpResult = request("/nacos/v1/ns/service",
            Params.newParams()
                .appendParam("serviceName", serviceName)
                .appendParam("protectThreshold", "0")
                .appendParam("selector", json.toJSONString())
                .done(),
            String.class,
            HttpMethod.PUT);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, httpResult.getStatusCodeValue());
    }

    public void instanceRegister(String serviceName, String namespace, String groupName, String ip, String port, String clusterName) throws
        IOException {
        ResponseEntity<String> httpResult = request("/nacos/v1/ns/instance",
            Params.newParams()
                .appendParam("serviceName", serviceName)
                .appendParam("ip", ip)
                .appendParam("port", port)
                .appendParam("namespaceId", namespace)
                .appendParam("groupName", groupName)
                .appendParam("clusterName", clusterName)
                .appendParam("ephemeral", "false")
                .done(),
            String.class,
            HttpMethod.POST);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, httpResult.getStatusCodeValue());
    }

    public void namingServiceCreate(String serviceName, String namespace) throws IOException {
        namingServiceCreate(serviceName, namespace, Constants.DEFAULT_GROUP);
    }

    public void namingServiceCreate(String serviceName, String namespace, String groupName) throws IOException {
        List<String> listParams = Arrays.asList("serviceName", serviceName, "protectThreshold", "0.3", "namespaceId", namespace, "groupName", groupName);
        ResponseEntity<String> httpResult = request("/nacos/v1/ns/service",
            Params.newParams()
                .appendParam("serviceName", serviceName)
                .appendParam("protectThreshold", "0.3")
                .appendParam("namespaceId", namespace)
                .appendParam("groupName", groupName)
                .done(),
            String.class,
            HttpMethod.POST);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, httpResult.getStatusCodeValue());
    }

    private <T> ResponseEntity<T> request(String path, MultiValueMap<String, String> params, Class<T> clazz) {
        return request(path, params, clazz, org.springframework.http.HttpMethod.GET);
    }

    private <T> ResponseEntity<T> request(String path, MultiValueMap<String, String> params, Class<T> clazz, org.springframework.http.HttpMethod httpMethod) {

        HttpHeaders headers = new HttpHeaders();

        HttpEntity<?> entity = new HttpEntity<T>(headers);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(this.base.toString() + path)
            .queryParams(params);

        return this.restTemplate.exchange(
            builder.toUriString(), httpMethod, entity, clazz);
    }
}


