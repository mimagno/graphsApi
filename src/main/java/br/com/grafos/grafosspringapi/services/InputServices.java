package br.com.grafos.grafosspringapi.services;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class InputServices {

	public JsonObject readInputStreamData(InputStream data) {
		StringBuilder stringBuilder = new StringBuilder();
		try {
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(data, "utf-8"));
			String line = null;

			while ((line = bufferedReader.readLine()) != null) {
				stringBuilder.append(line);
			}
		} catch (Exception e) {

		}
		@SuppressWarnings("deprecation")
		JsonParser parse = new JsonParser();
		@SuppressWarnings("deprecation")
		JsonObject body = parse.parse(stringBuilder.toString()).getAsJsonObject();
		return body;
	}
}
