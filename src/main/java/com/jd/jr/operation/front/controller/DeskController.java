package com.jd.jr.operation.front.controller;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.ser.BeanSerializer;
import com.fasterxml.jackson.databind.util.JSONPObject;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 桌面总控
 *
 * @author:dongzhihua
 * @time:2018/3/6 17:45:32
 */
@Controller
@RequestMapping("http")
public class DeskController {

	static Logger logger = LoggerFactory.getLogger(DeskController.class);

	@Value("${sys.plugins.path.http}")
	String sys_plugins_path_http;

	@RequestMapping("nothing.biz")
	@ResponseBody
	Object nothing(@RequestParam Map param) {
		logger.info("DeskController.nothing param:{}", param);
		return param;
	}

	/**
	 * view视图解析
	 * @author: dongzhihua
	 * @time: 2018/3/6 18:26:39
	 */
	@RequestMapping("{project}/{module}/{action}.view")
	String view(Model model, @RequestParam Map param, @PathVariable("project") String project, @PathVariable("module") String module, @PathVariable("action") String action) throws IOException {
		logger.info("view: http/{}/{}/{}.view, param:{}", project, module, action, param);

		Map result = bizDeal(param, project, module, action);
		model.addAllAttributes(result);
		logger.info("model: {}", model);
		return String.format("%s/%s/%s", project, model, action);
	}

	@RequestMapping("{project}/{module}/{action}.biz")
	@ResponseBody
	Object biz(@RequestParam Map param, @PathVariable("project") String project, @PathVariable("module") String module, @PathVariable("action") String action) throws IOException {
		logger.info("biz: http/{}/{}/{}.biz, param:{}", project, module, action, param);

		Map mapResult = bizDeal(param, project, module, action);
		return mapResult;
	}

	Map bizDeal(Map param, String project, String module, String action) throws IOException {
		String pm = sys_plugins_path_http + String.format("%s/%s.properties", project, module);
		logger.info("DeskController.bizDeal 配置文件路径：{}", pm);
		Properties properties = PropertiesLoaderUtils.loadProperties(new FileSystemResource(pm));
		Assert.notNull(properties, "没有找到配置：" + pm);
		String url = properties.getProperty(action + "_url");
		logger.info("DeskController.bizDeal url配置： {}", url);
		if(url == null) {
			return null;
		}
		Map map = getContentFromUrl(url, param);
		logger.info("DeskController.bizDeal result:", map);
		return map;
	}

	static Map getContentFromUrl(String url, Map param) throws IOException {
		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
		HttpClient httpClient = httpClientBuilder.build();

		HttpPost httpPost = new HttpPost(url);
		httpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
		String postParam = new ObjectMapper().writeValueAsString(param);
		logger.info("DeskController.getContentFromUrl postParam:{}", postParam);
		logger.info("DeskController.getContentFromUrl Entity: {}", httpPost.getEntity());
		if(param != null) {
			StringEntity entity = new StringEntity(postParam, Charset.forName("UTF-8"));
			entity.setContentEncoding("UTF-8");
			// 发送Json格式的数据请求
			entity.setContentType("application/json");
			httpPost.setEntity(entity);
		}
		logger.info("DeskController.getContentFromUrl Entity: {}", httpPost.getEntity());

		HttpResponse httpResponse = httpClient.execute(httpPost);

		InputStream is = httpResponse.getEntity().getContent();

		return new ObjectMapper().readValue(is, Map.class);
	}

	static String getCharsetNameByContentType(HttpResponse httpResponse) {
		String defaultCharset = "UTF-8";
		Header[] headers = httpResponse.getHeaders("Content-Type");
		if(headers == null || headers.length ==0) {
			return defaultCharset;
		}
		String contentType = headers[0].getValue();
		if(StringUtils.isEmpty(contentType)) {
			return defaultCharset;
		}

		String key = "charset=";
		contentType = contentType.substring(contentType.indexOf(key) + key.length());
		return contentType;
	}

	public static void main(String[] args) throws IOException {

		Map map = new HashMap();
		map.put("dongzhihua", "12345");
		NameValuePair nameValuePair = new BasicNameValuePair("name", "dong");
		System.out.println(getContentFromUrl("http://localhost:8088/http/absmall/abs/nothing.biz", map));
	}
}
