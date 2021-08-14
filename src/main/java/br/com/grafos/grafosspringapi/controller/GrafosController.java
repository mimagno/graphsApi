package br.com.grafos.grafosspringapi.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.io.InputStream;
import com.google.gson.JsonObject;
import br.com.grafos.grafosspringapi.services.GrafosServices;
import br.com.grafos.grafosspringapi.services.InputServices;

@RestController
@RequestMapping("/grafos")
public class GrafosController {
	private JsonObject cnpjEmpresas = new JsonObject();
	private InputServices inputServices = new InputServices();
	private GrafosServices grafosServices = new GrafosServices();
	
	
	@GetMapping("/helloWorld")
	public String helloWorld() {
		return "hello world"; 
	}
	
	@GetMapping
	public ResponseEntity<String> createGrafos(InputStream data) {
		JsonObject body = this.inputServices.readInputStreamData(data);

		String type = body.get("tipo").getAsString();
		
		if (type.equals("empresa") || type.equals("pessoa")) {
			return new ResponseEntity<>(this.grafosServices.buildGraphs(body).toString(), HttpStatus.OK);
		} else {
			return new ResponseEntity<>("Parâmetros inválidos", HttpStatus.BAD_REQUEST);
		}	
	}	
}
