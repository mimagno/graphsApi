package br.com.grafos.grafosspringapi.controller;

import java.io.InputStream;

import br.com.grafos.grafosspringapi.exception.UnsuportedParamsException;
import com.google.gson.JsonObject;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.grafos.grafosspringapi.services.BuildGraphsTools;
import br.com.grafos.grafosspringapi.services.GraphsServices;
import br.com.grafos.grafosspringapi.services.InputServices;
@CrossOrigin
@RestController
@RequestMapping("/grafos")
public class GraphsController {
	private GraphsServices graphsServices = new GraphsServices();

	@CrossOrigin
	@GetMapping("/helloWorld")
	public String helloWorld() {
		return "hello world"; 
	}

	@CrossOrigin
	@PostMapping
	public ResponseEntity<String> createGraphs(InputStream data) throws UnsuportedParamsException {
		return new ResponseEntity<>(this.graphsServices.getGraphs(data).toString(), HttpStatus.OK);
	}	
}
