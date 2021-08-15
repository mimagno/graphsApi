package br.com.grafos.grafosspringapi.services;

import org.springframework.http.ResponseEntity;

import java.util.LinkedList;
import java.util.Queue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import br.com.grafos.grafosspringapi.Util.RestClient;

public class GraphsServices {

	private int size = 0;
	private String corPessoa = "#1C75CF";
	private BuildGraphsTools buildGraphsTools = new BuildGraphsTools();

	public JsonObject buildGraphs(JsonObject body){
		int tier = body.get("camada").getAsInt();
		String indiceTitle = body.get("tituloIndice").getAsString();
		RestClient restClient = buildClientForGraphs(indiceTitle);

		Queue<JsonObject> queueNodes = new LinkedList<JsonObject>();

		JsonObject graph = new JsonObject();
		//visiteds
		JsonArray nodes = new JsonArray();
		JsonArray edges = new JsonArray();

		this.size = 40;
		for (int i = 2; i < tier; i++) {
			this.size = this.size / 2;
		}

		//revisar (são passados para criar o primeiro node)
		String id = body.get("id").getAsString();
		String type = buildGraphsTools.detectType(id);
		String index = body.get("nomeIndice").getAsString();
		String name = body.get("empresa").toString();

		JsonObject rootNode = null;
		if (body.get("tipo").getAsString().equals("empresa")) {
			JsonObject company = this.getMatrizCompany(id, name, restClient, index);
			rootNode = this.setUpNode(company.get("cnpj").toString(), company.get("companyName").toString(), this.size, type, index, restClient);
		} else {
			rootNode = this.setUpNode(id, name, this.size, type, index, restClient);
		}
		
		
		nodes.add(rootNode);
		queueNodes.add(rootNode);

		JsonObject currentNode;
		while((currentNode = queueNodes.poll()) != null ){
			System.out.println(currentNode.toString());
		}
		
		graph.add("nodes", nodes);
		graph.add("edges", edges);

		return graph;
	}


	public JsonObject setUpNode(String id, String nome, int size, String tipo,
		String indice, RestClient restCliente) {
		JsonObject node = new JsonObject();
		node.addProperty("id", id);
		node.addProperty("label", nome);
		node.addProperty("size", size);
		node.addProperty("tipo", tipo);
		if (node.get("tipo").getAsString().equals("pessoa")) {
			node.addProperty("color", this.corPessoa);
		} else {
			node.addProperty("color", buildGraphsTools.getCor(getCompanySituation(id, restCliente, indice)));
		}
		return node;
	}

	public JsonObject setUpEdge(String id, int forController, JsonObject socio, JsonArray socios) {
		JsonObject edge = new JsonObject();
				edge.addProperty("id", socio.get("id").getAsString() + "_" + id);
				edge.addProperty("source", socio.get("id").getAsString());
				edge.addProperty("target", id);
				edge.addProperty("label", " ");
				// SE MUDAR ESSE VALOR ELA FICA CURVA OU RETA
				edge.addProperty("type", "arrow");
				edge.addProperty("size", 1);
				edge.addProperty("color", socios.get(forController).getAsJsonObject().get("color").getAsString());

		return null;

	}
	
	public String getCompanySituation(String id, RestClient restClient, String index) {
		JsonParser parse = new JsonParser();
		String query = "{\"size\":1000,\"query\":{\"bool\":{\"must\":[{\"match\":{\"cnpj.keyword\":\"" + id
				+ "\"}}]}}}";
		ResponseEntity<?> requestResponse = restClient.post(index + "/_search", query);

		JsonObject bodyResponse = parse.parse((String) requestResponse.getBody()).getAsJsonObject();

		JsonArray hits = bodyResponse.get("hits").getAsJsonObject().get("hits").getAsJsonArray();

		JsonObject hit = hits.get(0).getAsJsonObject();

		String sutuation = hit.get("_source").getAsJsonObject().get("situacaoCadastral").getAsString();
		return sutuation;
	}

	
	private RestClient buildClientForGraphs(String indiceTitle){
		RestClient restClient;
		if (indiceTitle.contains("*")) {
			restClient = new RestClient("http://xtrdb01.consiste.com.br:9200/");
		} else {
			restClient = new RestClient();
		}	
		return restClient;
	}

	private JsonObject getMatrizCompany(String cnpj, String companyName, RestClient restClient, String index){
		JsonObject company = new JsonObject();
		String matriz_filial = cnpj.substring(8, 12);
		if (!matriz_filial.equals("0001")) {
			cnpj = cnpj.substring(0, 8) + "0001";
			String query = "{\"query\":{\"multi_match\":{\"query\":\"" + cnpj + "\",\"type\":\"phrase_prefix\"}},\"size\":10,\"from\":0}";

			JsonObject matriz = buildGraphsTools.sourceRequest(query, restClient, index).get(0).getAsJsonObject();
			company.addProperty("cnpj", matriz.get("_source").getAsJsonObject().get("cnpj").getAsString());
			company.addProperty("companyName", matriz.get("_source").getAsJsonObject().get("razaoSocial").getAsString());
		} else {
			company.addProperty("cnpj", cnpj);
			company.addProperty("companyName", companyName);
		}
		return company;
	}

}
