package br.com.grafos.grafosspringapi.services;

import org.springframework.http.ResponseEntity;

import java.util.LinkedList;
import java.util.Queue;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import br.com.grafos.grafosspringapi.Util.RestClient;
import ch.qos.logback.core.recovery.ResilientSyslogOutputStream;

public class GraphsServices {

	private int size = 0;
	private String corPessoa = "#1C75CF";
	private BuildGraphsTools buildGraphsTools = new BuildGraphsTools();

	public JsonObject buildGraphs(JsonObject body, String type) {
		int tier = body.get("camada").getAsInt();
		String indiceTitle = body.get("tituloIndice").getAsString();
		RestClient restClient = buildClientForGraphs(indiceTitle);

		Queue<JsonObject> queueNodes = new LinkedList<JsonObject>();

		JsonObject graph = new JsonObject();
		// visiteds
		JsonArray nodes = new JsonArray();
		JsonArray edges = new JsonArray();

		this.size = 40;
		for (int i = 2; i < tier; i++) {
			this.size = this.size / 2;
		}

		// revisar (são passados para criar o primeiro node)
		String id = body.get("id").getAsString();
		String index = body.get("nomeIndice").getAsString();
		String name = body.get("empresa").getAsString();

		JsonObject rootNode = null;
		int level = 0;
		if (type.equals("empresa")) {
			id = buildGraphsTools.splitId(id);
			JsonObject company = buildGraphsTools.getMatrizCompany(id, name, restClient, index);
			id = company.get("cnpj").getAsString();
			name = company.get("companyName").getAsString();
			rootNode = this.setUpNode(id, name, this.size, type, index, level, restClient);
		} else {
			rootNode = this.setUpNode(id, name, this.size, type, index, level, restClient);
		}

		queueNodes.add(rootNode);
		nodes.add(rootNode);
		JsonObject edge;
		JsonObject currentNode;
		while ((currentNode = queueNodes.poll()) != null) {
			type = buildGraphsTools.findNodeType(currentNode);
			level = currentNode.get("level").getAsInt();
			level++;
			if (level <= tier) {

				id = currentNode.get("id").getAsString();

				if (type.equals("empresa")) {

					JsonArray partners = buildGraphsTools.businessPartnersRequest(id, restClient, index);

					for (int count = 0; count < partners.size(); count++) {
						JsonObject partner = partners.get(count).getAsJsonObject();
						id = partner.get("cnpj_cpfSocio").getAsString();
						if (!buildGraphsTools.verifyExists(nodes, id)) {
							name = partner.get("nomeSocio").getAsString();
							type = buildGraphsTools.detectType(id);

							partner = setUpNode(id, name, size, type, index, level, restClient);

							nodes.add(partner);

							queueNodes.add(partner);
							edge = setUpEdge(partner, currentNode);

							edges.add(edge);

						}
					}

				} else {
					// this is a people node.
					JsonArray companies = buildGraphsTools.partnersBusinessRequest(currentNode, restClient, index);
					System.out.println("aaaaaaaaaaaa " + companies.toString());
					for (int count = 0; count < companies.size(); count++) {
						JsonObject company = companies.get(count).getAsJsonObject();
						id = company.get("cnpj").getAsString();
						if (!buildGraphsTools.verifyExists(nodes, id)) {
							name = company.get("razaoSocial").getAsString();

							company = setUpNode(id, name, size, type, index, level, restClient);

							nodes.add(company);

							queueNodes.add(company);

							// ver ordem
							edge = setUpEdge(currentNode, company);

							edges.add(edge);
						}
					}
				}
			}
		}

		graph.add("nodes", nodes);
		graph.add("edges", edges);

		return graph;
	}

	public JsonObject setUpNode(String id, String name, int size, String type, String index, int level,
			RestClient restClient) {
		JsonObject node = new JsonObject();
		node.addProperty("id", id);
		node.addProperty("label", name);
		node.addProperty("size", size);
		node.addProperty("tipo", type);
		node.addProperty("level", level);
		if (node.get("tipo").getAsString().equals("pessoa")) {
			node.addProperty("color", this.corPessoa);
		} else {
			node.addProperty("color",
					buildGraphsTools.getCor(buildGraphsTools.getCompanySituation(id, restClient, index)));
		}
		return node;
	}

	public JsonObject setUpEdge(JsonObject nodeOrigin, JsonObject nodeTarget) {

		// Setup Edge from nodeOrigin to nodeTarget
		JsonObject edge = new JsonObject();
		edge.addProperty("id", nodeTarget.get("id").getAsString() + "_" + nodeOrigin.get("id").getAsString());
		edge.addProperty("source", nodeOrigin.get("id").getAsString());
		edge.addProperty("target", nodeTarget.get("id").getAsString());
		edge.addProperty("label", " ");
		// IF YOU CHANGE THIS VALUE IT IS CURVED OR STRAIGHT
		edge.addProperty("type", "arrow");
		edge.addProperty("size", 1);
		edge.addProperty("color", nodeOrigin.get("color").getAsString());

		return edge;

	}

	private RestClient buildClientForGraphs(String indiceTitle) {
		RestClient restClient;
		if (indiceTitle.contains("*")) {
			restClient = new RestClient("http://xtrdb01.consiste.com.br:9200/");
		} else {
			restClient = new RestClient();
		}
		return restClient;
	}

}
