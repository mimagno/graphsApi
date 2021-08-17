package br.com.grafos.grafosspringapi.Util;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class RestClient {

	private String server;
	private RestTemplate rest;
	private HttpHeaders headers;
	private HttpStatus status;

	public RestClient() {
		this.rest = new RestTemplate();
		this.headers = new HttpHeaders();
		this.server = "http://xtr-verde.consiste.com.br:9200/";
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
	    this.setStatus(responseEntity.getStatusCode());
	    return responseEntity;
	  }

	  public HttpStatus getStatus() {
	    return status;
	  }

	  public void setStatus(HttpStatus status) {
	    this.status = status;
	  } 
}

