package com.example.Liter.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DatosLibro(@JsonAlias("title") String titulo,
                         @JsonAlias("authors") String autor,
                         @JsonAlias("languages") String idioma,
                         @JsonAlias("copyright") String copyright,
                         @JsonAlias("download_count") Integer descargas) {
}

/*@JsonAlias solo lee, @JsonProperty se puede leer y escribir para adicionar datos en una api*/