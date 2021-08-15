package br.com.grafos.grafosspringapi.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.io.InputStream;
import com.google.gson.JsonObject;
import br.com.grafos.grafosspringapi.services.GraphsServices;
import br.com.grafos.grafosspringapi.services.InputServices;

@RestController
@RequestMapping("/grafos")
public class GraphsController {
	private JsonObject cnpjEmpresas = new JsonObject();
	private InputServices inputServices = new InputServices();
	private GraphsServices graphsServices = new GraphsServices();
	
	
	@GetMapping("/helloWorld")
	public String helloWorld() {
		return "hello world"; 
	}
	
	@GetMapping
	public ResponseEntity<String> createGrafos(InputStream data) {
		JsonObject body = this.inputServices.readInputStreamData(data);
		String type = body.get("tipo").getAsString();
		
		if (type.equals("empresa") || type.equals("pessoa")) {
			return new ResponseEntity<>(this.graphsServices.buildGraphs(body).toString(), HttpStatus.OK);
		} else {
			return new ResponseEntity<>("Parâmetros inválidos", HttpStatus.BAD_REQUEST);
		}	
	}	
}
