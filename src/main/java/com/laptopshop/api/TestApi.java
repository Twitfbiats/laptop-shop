package com.laptopshop.api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;

import java.io.FileWriter;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("api/test")
public class TestApi 
{
    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/cr")
    public String GetCurrencyRate()
	{
		HttpHeaders headers = new HttpHeaders();
        headers.set("X-Rapidapi-Key", "37712bc8d8mshf511bbf8400e00ep1e1544jsn6a78193d4661");
        headers.set("X-Rapidapi-Host", "currency-exchange.p.rapidapi.com");

        RequestEntity<Void> requestEntity
        = RequestEntity.get("https://currency-exchange.p.rapidapi.com/exchange?from=USD&to=VND&q=1.0").headers(headers).build();

        var res = restTemplate.exchange(requestEntity, String.class).getBody();

        return res;
	}
}
