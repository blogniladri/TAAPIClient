package com.ibm.isl.ta.controller;

import java.io.File;
import java.nio.file.Files;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.ibm.isl.ta.AppProperties;
import com.ibm.isl.ta.util.HttpsSSLClient;
import com.ibm.isl.ta.vo.Preferences;
import com.ibm.isl.ta.vo.TaskPayload;

import io.swagger.annotations.Api;

@RequestMapping("/client")
@RestController
@Api(value = "TA API Test", description = "Operations to test the TA APIs")
public class TAClientController {

	Logger logger = LoggerFactory.getLogger(TAClientController.class);

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	private AppProperties appProperties;

	String auth_url_template = "https://%s:8443/idprovider/v1/auth/identitytoken";
	String srv_url_template = "https://%s:8443/iam-token/serviceids/";
	String apikey_url_template = "https://%s:8443/iam-token/apikeys/";

	String workspace_url_template = "https://%s/tatest-server/lands_advisor/advisor/landsw/workspaces";
	String datacollector_upload_url_template = "https://%s/tatest-server/lands_advisor/advisor/landsw/upload?collection=%s";
	String report_url_template = "https://%s/tatest-server/lands_advisor/advisor/landsw/pdfReport";
	String profile_url_template = "https://%s/tatest-server/lands_advisor/advisor/landsw/profiles";
	String application_url_template = "https://%s/tatest-server/lands_advisor/advisor/landsw/applications";

	String preference_url_template = "https://%s/tatest-server/lands_advisor/advisor/landsw/preferences";

	@GetMapping(value = "/apikey", produces = { "application/JSON" })
	public String getAPIKey(@RequestParam(value = "username", required = false) final String username,
			@RequestParam(value = "password", required = false) final String password) {

		logger.debug("inside getToken");

		String apiKey = null;
		String icp_ip = appProperties.getIcp_host();

		// 1. Get access_token
		String access_token = null;
		MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
		map.add("grant_type", "password");
		map.add("username", username);
		map.add("password", password);
		map.add("scope", "openid");
		try {
			String data = restTemplate.postForObject(String.format(auth_url_template, icp_ip), map, String.class);
			JSONObject jsonObj = new JSONObject(data);
			access_token = jsonObj.getString("access_token");
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		// 2. Get service ids
		String crn = null;
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(access_token);
		HttpEntity entity = new HttpEntity(headers);
		try {
			ResponseEntity<String> response = restTemplate.exchange(String.format(srv_url_template, icp_ip),
					HttpMethod.GET, entity, String.class);
			JSONObject jsonObj = new JSONObject(response.getBody());
			crn = ((JSONObject) jsonObj.getJSONArray("items").get(0)).getJSONObject("metadata").getString("crn");
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		// 3. Get API Key
		headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(access_token);
		String reqdata = "{\"name\": \"test_serviceid_apikey\",\"description\": \"Description\",\"boundTo\": \"" + crn
				+ "\"}";
		try {
			entity = new HttpEntity(reqdata, headers);
			String data = restTemplate.postForObject(String.format(apikey_url_template, icp_ip), entity, String.class);
			JSONObject jsonObj = new JSONObject(data);
			apiKey = jsonObj.getJSONObject("entity").getString("apiKey");
			logger.debug("apiKey:" + apiKey);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return apiKey;
	}

	@GetMapping(value = "/workspaces", produces = { "application/JSON" })
	public String getWorkspaces(@RequestHeader(value = "authorization", required = true) final String auth) {

		logger.debug("inside getWorkspaces:" + auth);
		JSONArray jsonArray = null;
		String responseStr = null;

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("authorization", auth);
		HttpEntity entity = new HttpEntity(headers);

		try {
			ResponseEntity<String> response = restTemplate.exchange(
					String.format(workspace_url_template, appProperties.getApi_host()), HttpMethod.GET, entity,
					String.class);
			responseStr = response.getBody();
			logger.debug("responseStr:" + responseStr);
			jsonArray = new JSONArray(responseStr);
			for (int i = 0; i < jsonArray.length(); i++) {
				logger.debug("-->:" + jsonArray.get(i));
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return responseStr;
	}

	@PostMapping(value = "/workspaces", produces = { "application/JSON" })
	public String createWorkspace(@RequestHeader(value = "authorization", required = true) final String auth,
			@RequestParam(value = "workspace", required = true) final String workspace,
			@RequestParam(value = "collection", required = true) final String collection,
			@RequestParam(value = "uploadkey", required = true) final String uploadkey,
			@RequestParam(value = "devEffort", required = true) final int devEffort,
			@RequestParam(value = "cloudChoice", required = true) final String cloudChoice,
			@RequestParam(value = "overhead", required = true) final int overhead) {

		logger.debug("inside createWorkspace:");
		String responseStr = null;

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("authorization", auth);
		headers.set("workspace", workspace);

		TaskPayload payload = new TaskPayload();
		payload.setWorkspace(workspace);
		payload.setTenantId(workspace);
		payload.setCollection(collection);
		payload.setStatus(2);
		payload.setTaskname(collection);
		payload.setUploadKey(uploadkey);

		Preferences preferences = new Preferences();
		preferences.setDevEffort(devEffort);
		preferences.setCloudChoice(cloudChoice);
		preferences.setOverhead(overhead);
		preferences.setUseFlexibleEnv("YES");

		payload.setPreferences(preferences);

		HttpEntity<TaskPayload> entity = new HttpEntity<>(payload, headers);

		try {
			ResponseEntity<String> response = restTemplate.exchange(
					String.format(preference_url_template, appProperties.getApi_host()), HttpMethod.POST, entity,
					String.class);
			responseStr = response.getBody();
			logger.debug("responseStr:" + responseStr);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return responseStr;
	}

	@GetMapping(value = "/profiles", produces = { "application/JSON" })
	public String getProfiles(@RequestHeader(value = "authorization", required = true) final String auth,
			@RequestParam(value = "workspace", required = true) final String workspace,
			@RequestParam(value = "collection", required = true) final String collection) throws Exception {

		logger.debug("inside getProfiles");

		String responseStr = null;

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("authorization", auth);
		headers.set("workspace", workspace);
		headers.set("collection", collection);

		HttpEntity requestEntity = new HttpEntity(headers);

		try {
			ResponseEntity<String> response = restTemplate.exchange(
					String.format(profile_url_template, appProperties.getApi_host()), HttpMethod.GET, requestEntity,
					String.class);
			responseStr = response.getBody();
			logger.debug(response.getBody());

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return responseStr;
	}

	@GetMapping(value = "/applications", produces = { "application/JSON" })
	public String getApplication(@RequestHeader(value = "authorization", required = true) final String auth,
			@RequestParam(value = "workspace", required = true) final String workspace,
			@RequestParam(value = "collection", required = true) final String collection,
			@RequestParam(value = "profileName", required = true) final String profileName) throws Exception {

		logger.debug("inside getApplication");

		String responseStr = null;

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("authorization", auth);
		headers.set("workspace", workspace);
		headers.set("collection", collection);
		headers.set("profileName", profileName);

		HttpEntity requestEntity = new HttpEntity(headers);

		try {
			ResponseEntity<String> response = restTemplate.exchange(
					String.format(application_url_template, appProperties.getApi_host()), HttpMethod.GET, requestEntity,
					String.class);
			responseStr = response.getBody();
			logger.debug(response.getBody());

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return responseStr;
	}

	@PostMapping(value = "/datacollector", produces = { "application/JSON" })
	public String uploadCollectorData(@RequestHeader(value = "authorization", required = true) final String auth,
			@RequestParam(value = "workspace", required = true) final String workspace,
			@RequestParam(value = "collection", required = true) final String collection,
			@RequestParam(value = "profileName", required = true) final String profileName,
			@RequestParam(value = "uploadKey", required = true) final String uploadKey) throws Exception {

		logger.debug("inside uploadCollectorData");

		CloseableHttpClient httpclient = HttpsSSLClient.createSSLInsecureClient();

		HttpPost post = new HttpPost(
				"https://9.202.181.183:443/tatest-server/lands_advisor/advisor/landsw/upload?collection=" + collection
						+ "&uploadKey=" + uploadKey);
		post.addHeader("authorization", auth);
		post.addHeader("workspace", workspace);
		post.addHeader("profileName", profileName);

		File file = new File("/Users/niladri/docspace/TransformationAdvisor/Dmgr01.zip");
		byte[] fileContent = Files.readAllBytes(file.toPath());
		
		org.apache.http.HttpEntity httpEntity = MultipartEntityBuilder.create()
				.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
				.setContentType(ContentType.APPLICATION_OCTET_STREAM)
				.addBinaryBody("file", fileContent, ContentType.APPLICATION_OCTET_STREAM, "Dmgr01.zip").build();
		post.setEntity(httpEntity);

		HttpResponse response = httpclient.execute(post);
		logger.debug("response:" + response.toString());

		return null;
	}

	@GetMapping(value = "/datacollector", produces = { "application/JSON" })
	public String downloadDataCollector(@RequestHeader(value = "authorization", required = true) final String auth,
			@RequestParam(value = "workspace", required = true) final String workspace,
			@RequestParam(value = "collection", required = true) final String collection,
			@RequestParam(value = "platform", required = true) final String platform,
			@RequestParam(value = "uploadKey", required = true) final String uploadKey) throws Exception {

		logger.debug("inside downloadDataCollector");
		String responseStr = null;

		responseStr = checkIfDataCollectorAvailable(auth, workspace, platform, collection);
		if (responseStr != null) {
			return responseStr;
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Accept", "application/json");
		headers.set("authorization", auth);
		headers.set("workspace", workspace);
		headers.set("platform", platform);
		headers.set("collection", collection);
		headers.set("apiKey", uploadKey);

		HttpEntity entity = new HttpEntity(headers);
		try {
			ResponseEntity<String> response = restTemplate.exchange(
					"https://9.202.181.183:443/tatest-server/lands_advisor/advisor/landsw/datacollector",
					HttpMethod.POST, entity, String.class);
			responseStr = response.getBody();
			JSONObject obj = new JSONObject(responseStr);
			boolean isSuccess = obj.getBoolean("created");
			if (isSuccess) {
				String response_url_template = "https://%s:443/%s/lands_advisor/%s/transformationadvisor-%s_%s_%s.tgz";
				responseStr = String.format(response_url_template, appProperties.getApi_host(),
						appProperties.getApi_host_context(), workspace, platform, workspace, collection);
			}

			logger.debug(responseStr);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return responseStr;
	}

	private String checkIfDataCollectorAvailable(String auth, String workspace, String platform, String collection) {
		logger.debug("inside checkIfDataCollectorAvailable");

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Accept", "application/json");
		headers.set("authorization", auth);
		headers.set("workspace", workspace);
		headers.set("platform", platform);
		headers.set("collection", collection);

		String responseStr = null;
		HttpEntity entity = new HttpEntity(headers);
		try {
			ResponseEntity<String> response = restTemplate.exchange(
					"https://9.202.181.183:443/tatest-server/lands_advisor/advisor/landsw/datacollector/availability",
					HttpMethod.GET, entity, String.class);
			JSONObject obj = new JSONObject(response.getBody());
			boolean isAvailable = obj.getBoolean("available");
			if (isAvailable) {
				String response_url_template = "https://%s:443/%s/lands_advisor/%s/transformationadvisor-%s_%s_%s.tgz";
				responseStr = String.format(response_url_template, appProperties.getApi_host(),
						appProperties.getApi_host_context(), workspace, platform, workspace, collection);
			}
			logger.debug(responseStr);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return responseStr;
	}

	@GetMapping(value = "/reports/all", produces = { "application/JSON" })
	public String downloadReport(@RequestHeader(value = "authorization", required = true) final String auth,
			@RequestParam(value = "workspace", required = true) final String workspace,
			@RequestParam(value = "collection", required = true) final String collection) throws Exception {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("authorization", auth);
		headers.set("workspace", workspace);
		headers.set("collection", collection);
		headers.set("choice", "all");
		headers.set("ingressLibertyServerUrl", "https://9.202.181.183/tatest-server/");

		HttpEntity requestEntity = new HttpEntity(headers);

		try {
			ResponseEntity<String> response = restTemplate.exchange(
					String.format(report_url_template, appProperties.getApi_host()), HttpMethod.GET, requestEntity,
					String.class);
			logger.debug(response.getBody());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return "";
	}

	@GetMapping(value = "/reports/recommendations", produces = { "application/JSON" })
	public String getRecommendations(@RequestHeader(value = "authorization", required = true) final String auth,
			@RequestParam(value = "workspace", required = true) final String workspace,
			@RequestParam(value = "collection", required = true) final String collection,
			@RequestParam(value = "profileName", required = true) final String profileName) throws Exception {

		logger.debug("inside getRecommendations");
		logger.debug("ingressLibertyServerUrl:"
				+ String.format("https://%s/%s/", appProperties.getApi_host(), appProperties.getApi_host_context()));
		logger.debug("report url:" + String.format(report_url_template, appProperties.getApi_host()));

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("authorization", auth);
		headers.set("workspace", workspace);
		headers.set("collection", collection);
		headers.set("profileName", profileName);

		headers.set("ingressLibertyServerUrl", "https://9.202.181.183/tatest-server/");

		HttpEntity requestEntity = new HttpEntity(headers);

		try {
			ResponseEntity<String> response = restTemplate.exchange(
					String.format(report_url_template, appProperties.getApi_host()), HttpMethod.GET, requestEntity,
					String.class);

			logger.debug(response.getBody());

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return "";
	}

}