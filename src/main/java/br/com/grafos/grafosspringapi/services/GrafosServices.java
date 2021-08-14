package br.com.grafos.grafosspringapi.services;

import org.springframework.http.ResponseEntity;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.Queue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import br.com.grafos.grafosspringapi.Util.RestClient;

public class GrafosServices {

	private int size = 0;
	private String corPessoa = "#1C75CF";

	public JsonObject buildGraphs(JsonObject body){
		int tier = body.get("camada").getAsInt();
		String tituloIndice = body.get("tituloIndice").getAsString();
		RestClient restClient = buildClientForGraphs(tituloIndice);

		Queue<JsonObject> queueNodes = new LinkedList<JsonObject>();

		JsonObject graph = new JsonObject();
		//visiteds
		JsonArray nodes = new JsonArray();
		JsonArray edges = new JsonArray();

		//revisar (são passados para criar o primeiro node)
		String id = body.get("id").getAsString();
		String company = body.get("empresa").getAsString();
		String index = body.get("nomeIndice").getAsString();
		String type = body.get("tipo").getAsString();
		
		
		this.size = 40;
		for (int i = 2; i < tier; i++) {
			this.size = this.size / 2;
		}
		
		
		JsonObject rootNode = this.setUpNode(id, company, this.size, type, index, restClient);
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
			node.addProperty("color", getCor(getSituacaoEmpresa(id, restCliente, indice)));
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
	
	public String getSituacaoEmpresa(String id, RestClient rC, String indice) {
		JsonParser parse = new JsonParser();
		String query = "{\"size\":1000,\"query\":{\"bool\":{\"must\":[{\"match\":{\"cnpj.keyword\":\"" + id
				+ "\"}}]}}}";
		ResponseEntity<?> requestResponse = rC.post(indice + "/_search", query);

		JsonObject bodyResponse = parse.parse((String) requestResponse.getBody()).getAsJsonObject();

		JsonArray hits = bodyResponse.get("hits").getAsJsonObject().get("hits").getAsJsonArray();

		JsonObject hit = hits.get(0).getAsJsonObject();

		String situacao = hit.get("_source").getAsJsonObject().get("situacaoCadastral").getAsString();
		return situacao;
	}

	private String getCor(String situacao) {
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

	private RestClient buildClientForGraphs(String tituloIndice){
		RestClient restClient;
		if (tituloIndice.contains("*")) {
			restClient = new RestClient("http://xtrdb01.consiste.com.br:9200/");
		} else {
			restClient = new RestClient();
		}	
		return restClient;
	}

}
