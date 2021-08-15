package br.com.grafos.grafosspringapi.services;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.springframework.http.ResponseEntity;

import br.com.grafos.grafosspringapi.Util.RestClient;

public class BuildGraphsTools {

     public String detectType(String id) {
		String type;
		if (id.indexOf('*') > -1) {
			type = "pessoa";
		} else {
			type = "empresa";
		}
		return type;
	}
public JsonArray businessPartnersRequest(String query, RestClient restClient, String indice) {
		ResponseEntity<?> requestResponse = restClient.post(indice + "/_search", query);

		JsonParser parse = new JsonParser();

		JsonObject requestResponseJson = parse.parse((String) requestResponse.getBody()).getAsJsonObject();
		JsonObject hits = requestResponseJson.get("hits").getAsJsonObject();
		JsonArray company = hits.get("hits").getAsJsonArray();

		JsonObject fullDocument = company.get(0).getAsJsonObject();
		JsonObject sourceDocument = fullDocument.get("_source").getAsJsonObject();
		JsonArray businessPartenrsArray = sourceDocument.get("socios").getAsJsonArray();
		String cor = getCor(sourceDocument.get("situacaoCadastral").getAsString());
		JsonArray businessPartnersFinalArray = new JsonArray();

		for (int i = 0; i < businessPartenrsArray.size(); i++) {
			JsonObject businessPartner = businessPartenrsArray.get(i).getAsJsonObject();
			businessPartner.addProperty("color", cor);
			businessPartnersFinalArray.add(businessPartner);
		}
		return businessPartnersFinalArray;
	}
	

	public JsonArray sourceRequest(String query, RestClient restClient, String indice) {
		JsonParser parse = new JsonParser();

		ResponseEntity<?> requestResponse = restClient.post(indice + "/_search", query);

		JsonObject bodyResponse = parse.parse((String) requestResponse.getBody()).getAsJsonObject();
		JsonArray hits = bodyResponse.get("hits").getAsJsonObject().get("hits").getAsJsonArray();
		
		return hits;
	}

	public String getCor(String situacao) {
		String cor;

		if (situacao.trim().equals("ATIVA")) {
			cor = "#000000";
		} else if (situacao.trim().equals("BAIXADA")) {
			cor = "#C74B4B";
		} else if (situacao.trim().equals("INAPTA")) {
			cor = "#FFA500";
		} else if (situacao.trim().equals("SUSPENSA")) {
			cor = "#D4D44D";
		} else if (situacao.trim().equals("NULA")) {
			cor = "#B1AAAA";
		} else {
			cor = "#FFFFFF";
		}
		return cor;
	}
}