package com.writenbite.bisonfun.api.config;

import info.movito.themoviedbapi.TmdbApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TmdbConfig {
    @Value("${bisonfun.tmdb.read-access-key}")
    String tmdbReadAccessKey;

    @Bean
    public TmdbApi getTmdbApi(){
        return new TmdbApi(tmdbReadAccessKey);
    }
}
