package br.com.grafos.grafosspringapi.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


import org.springframework.http.ResponseEntity;

import br.com.grafos.grafosspringapi.Util.RestClient;

public class BuildGraphsTools {

	private JsonParser parser = new JsonParser();

	public String detectType(String id) {
		String type;
		if (id.indexOf('*') > -1) {
			type = "pessoa";
		} else {
			type = "empresa";
		}
		return type;
	}

	public String getCompanySituation(String id, RestClient restClient, String index) {
		String query = "{\"size\":1000,\"query\":{\"bool\":{\"must\":[{\"match\":{\"cnpj.keyword\":\"" + id +"\"}}]}}}";
		ResponseEntity<?> requestResponse = restClient.post(index + "/_search", query);

		JsonObject bodyResponse = this.parser.parse((String) requestResponse.getBody()).getAsJsonObject();

		JsonArray hits = bodyResponse.get("hits").getAsJsonObject().get("hits").getAsJsonArray();

		JsonObject hit = hits.get(0).getAsJsonObject();

		String sutuation = hit.get("_source").getAsJsonObject().get("situacaoCadastral").getAsString();
		return sutuation;
	}

	public JsonObject getMatrizCompany(String cnpj, String companyName, RestClient restClient, String index){
		JsonObject company = new JsonObject();
		String matriz_filial = cnpj.substring(8, 12);
		if (!matriz_filial.equals("0001")) {
			cnpj = cnpj.substring(0, 8) + "0001";
			String query = "{\"query\":{\"multi_match\":{\"query\":\"" + cnpj + "\",\"type\":\"phrase_prefix\"}},\"size\":10,\"from\":0}";

			JsonObject matriz = sourceRequest(query, restClient, index).get(0).getAsJsonObject();
			company.addProperty("cnpj", matriz.get("_source").getAsJsonObject().get("cnpj").getAsString());
			company.addProperty("companyName", matriz.get("_source").getAsJsonObject().get("razaoSocial").getAsString());
		} else {
			company.addProperty("cnpj", cnpj);
			company.addProperty("companyName", companyName);
		}
		return company;
	}

	public JsonArray businessPartnersRequest(String cnpj, RestClient restClient, String indice) {
		String query = "{\"size\":1000,\"query\":{\"bool\":{\"must\":[{\"match\":{\"cnpj.keyword\":\"" + cnpj
				+ "\"}}]}}}";

		ResponseEntity<?> requestResponse = restClient.post(indice + "/_search", query);
		JsonObject requestResponseJson = this.parser.parse((String) requestResponse.getBody()).getAsJsonObject();
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

	public JsonArray partnersBusinessRequest(JsonObject partner, RestClient restClient, String indice) {
		String query ="{\"size\":1000, \"query\":{\"bool\":{\"must\":[{\"match\":{\"socios.nomeSocio.keyword\":\"" + partner.get("label").getAsString() + "\"}},"
		+ "{\"match\":{\"socios.cnpj_cpfSocio.keyword\":\"" + splitId(partner.get("id").getAsString()) + "\"}},"
		+ "{\"match\":{\"matriz_filial.keyword\":\"MATRIZ\"}}]}}}";

		ResponseEntity<?> requestResponse = restClient.post(indice + "/_search", query);
		JsonObject requestResponseJson = this.parser.parse((String) requestResponse.getBody()).getAsJsonObject();
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
		ResponseEntity<?> requestResponse = restClient.post(indice + "/_search", query);

		JsonObject bodyResponse = this.parser.parse((String) requestResponse.getBody()).getAsJsonObject();
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

	public boolean verifyExists(JsonArray entities, String id) {
		for (int count = 0; count < entities.size(); count++) {
			JsonObject entity = entities.get(count).getAsJsonObject();
			if (entity.get("id").toString().equals(id)) {
				return true;
			}
		}
		return false;
	}

	public String splitId(String id) {
		if (id.contains("_")){
			String[] idSplit = id.trim().split("_");
			id = idSplit[0];
		}
		return id;
	}

	public String findNodeType(JsonObject currentNode) {
		String id = splitId(currentNode.get("id").getAsString());
		String type = detectType(id);
		return type;
	}

	private String isCompany(String id){
		String[] newId = id.split("_");
		if (newId[0].length() == 14) {
			return newId[0];
		} else{
			return id;
		}
	}

	public String getPartnerCompanyCnpj(String id) {
		if (id.contains("_")){
			if (!id.substring(0, 11).contains("*")){
			id = isCompany(id);
			}
		}
		return id;
	}
}