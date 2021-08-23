package br.com.grafos.grafosspringapi.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.http.ResponseEntity;
import br.com.grafos.grafosspringapi.Util.RestClient;

@SuppressWarnings("deprecation")
public class BuildGraphsTools {

	private JsonParser parser = new JsonParser();

	public String detectType(String id) {
		String type = null;
		if (id.indexOf('*') > -1) {
			type = "pessoa";
		} else if (id.length() == 14){
			type = "empresa";
		}
		return type;
	}

	public String getCompanySituation(String id, RestClient restClient, String index) {
		String query = "{\"size\":1000,\"query\":{\"bool\":{\"must\":[{\"match\":{\"cnpj.keyword\":\"" + id +"\"}}]}}}";

		JsonArray hits = sourceRequest(query, restClient, index);
				
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

	public JsonArray businessPartnersRequest(String cnpj, RestClient restClient, String index) {
		String query = "{\"size\":1000,\"query\":{\"bool\":{\"must\":[{\"match\":{\"cnpj.keyword\":\"" + cnpj
				+ "\"}}]}}}";
	
		JsonArray company = sourceRequest(query, restClient, index);
		
		JsonObject fullDocument = company.get(0).getAsJsonObject();
		JsonObject sourceDocument = fullDocument.get("_source").getAsJsonObject();
		JsonArray businessPartenrsArray = sourceDocument.get("socios").getAsJsonArray();
		String cor = this.getColor(sourceDocument.get("situacaoCadastral").getAsString());
		JsonArray businessPartnersFinalArray = new JsonArray();

		for (int i = 0; i < businessPartenrsArray.size(); i++) {
			JsonObject businessPartner = businessPartenrsArray.get(i).getAsJsonObject();
			businessPartner.addProperty("color", cor);
			businessPartnersFinalArray.add(businessPartner);
		}
		return businessPartnersFinalArray;
	}

	public JsonArray partnersBusinessRequest(JsonObject partner, RestClient restClient, String index) {
		String query ="{\"size\":1000, \"query\":{\"bool\":{\"must\":[{\"match\":{\"socios.nomeSocio.keyword\":\"" + partner.get("label").getAsString() + "\"}},"
		+ "{\"match\":{\"socios.cnpj_cpfSocio.keyword\":\"" + splitId(partner.get("id").getAsString()) + "\"}},"
		+ "{\"match\":{\"matriz_filial.keyword\":\"MATRIZ\"}}]}}}";
		
		JsonArray hits = sourceRequest(query, restClient, index);
		
		return hits;
	}

	public JsonArray sourceRequest(String query, RestClient restClient, String index) {
		ResponseEntity<?> requestResponse = restClient.post(index + "/_search", query);

		JsonObject bodyResponse = this.parser.parse((String) requestResponse.getBody()).getAsJsonObject();
		JsonArray hits = bodyResponse.get("hits").getAsJsonObject().get("hits").getAsJsonArray();

		return hits;
	}

	public String getColor(String situacao) {
		String color;

		if (situacao.trim().equals("ATIVA")) {
			color = "#000000";
		} else if (situacao.trim().equals("BAIXADA")) {
			color = "#C74B4B";
		} else if (situacao.trim().equals("INAPTA")) {
			color = "#FFA500";
		} else if (situacao.trim().equals("SUSPENSA")) {
			color = "#D4D44D";
		} else if (situacao.trim().equals("NULA")) {
			color = "#B1AAAA";
		} else {
			color = "#FFFFFF";
		}
		return color;
	}

	public boolean verifyExists(JsonArray entities, String id) {
		for (int count = 0; count < entities.size(); count++) {
			JsonObject entity = entities.get(count).getAsJsonObject();
			if (entity.get("id").getAsString().equals(id)) {
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
}
