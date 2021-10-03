package br.com.grafos.grafosspringapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UnsuportedParamsException extends Exception{

    public UnsuportedParamsException(){
        super("Parâmetros Inválidos");
    }
}
