package com.example.Liter.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DatosLibro(
        @JsonAlias("id") Long id,
        @JsonAlias("title") String titulo,
        @JsonAlias("authors") List<DatosAutor> autores,
        @JsonAlias("languages") List<String> idiomas,
        @JsonAlias("copyright") String copyright,
        @JsonAlias("download_count") Integer descargas) {
}

/*@JsonAlias solo lee, @JsonProperty se puede leer y escribir para adicionar datos en una api*/