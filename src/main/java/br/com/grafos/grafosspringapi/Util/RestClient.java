package br.com.grafos.grafosspringapi.Util;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class RestClient {

	private String server;
	private RestTemplate rest;
	private HttpHeaders headers;

	public RestClient() {
		this.rest = new RestTemplate();
		this.headers = new HttpHeaders();
		this.server = "productionServerURL";
		headers.add("Content-Type", "application/json;charset=UTF-8");
		headers.add("Accept", "*/*");
	}
	
	public RestClient(String server) {
		this.rest = new RestTemplate();
		this.headers = new HttpHeaders();
		this.server = server;
		headers.add("Content-Type", "application/json;charset=UTF-8");
		headers.add("Accept", "*/*");
	}
	
		public ResponseEntity<?> post(String uri, String json) {   
	    HttpEntity<String> requestEntity = new HttpEntity<String>(json, headers);
	    ResponseEntity<String> responseEntity = rest.exchange(server + uri, HttpMethod.POST, requestEntity, String.class);
	    return responseEntity;
	  }
}

