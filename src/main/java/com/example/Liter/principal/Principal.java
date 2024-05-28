package com.example.Liter.principal;

import com.example.Liter.model.*;
import com.example.Liter.repository.AutorRepository;
import com.example.Liter.service.ConsumoAPI;
import com.example.Liter.service.ConvierteDatos;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Principal {

    private Scanner teclado = new Scanner(System.in);
    private ConsumoAPI consumoAPI = new ConsumoAPI();
    private ConvierteDatos conversor = new ConvierteDatos();
    private String URL_BASE = "https://gutendex.com/books/";
    private AutorRepository repository;

    public Principal(AutorRepository repository){
        this.repository = repository;
    }

    public void mostrarMenu() {
        var opcion = -1;
        var menu = """
            1 - Buscar por título para almacenar en BD

            0 - Cerrar programa
            ----------------------------------------------
            Elija una opción:
            """;

        while (opcion != 0) {
            System.out.println(menu);
            try {
                opcion = Integer.valueOf(teclado.nextLine());
                switch (opcion) {
                    case 1:
                        buscarLibroPorTitulo();
                        break;

                    case 0:
                        System.out.println("Aplicción cerrada exitosamente.");
                        break;
                    default:
                        System.out.println("Opción inválida. Elija una opción del menú");
                        break;
                }
            } catch (NumberFormatException e) {
                System.out.println("Opción no válida: " + e.getMessage());

            }
        }
    }
    public void buscarLibroPorTitulo() {

        System.out.println("Ingrese el título a buscar:");
        var nombre = teclado.nextLine();
        var json = consumoAPI.obtenerDatos(URL_BASE + "?search=" + nombre.replace(" ", "+").toLowerCase());

        if (json.isEmpty() || !json.contains("\"count\":0,\"next\":null,\"previous\":null,\"results\":[]")) {
            var datos = conversor.obtenerDatos(json, Datos.class);

            Optional<DatosLibro> libroBuscado = datos.libros().stream()
                    .findFirst();
            if (libroBuscado.isPresent()) {
                System.out.println("Título: " + libroBuscado.get().titulo() + ", autor: " + libroBuscado.get().autores().stream()
                                .map(a -> a.nombre()).limit(1).collect(Collectors.joining()) +
                                ", idioma: " + libroBuscado.get().idiomas().stream().collect(Collectors.joining()) +
                                ",  descargas: " + libroBuscado.get().descargas() + ". "
                );

                try {
                    List<Libro> libroEncontrado = libroBuscado.stream().map(a -> new Libro(a)).collect(Collectors.toList());
                    Autor autorAPI = libroBuscado.stream().
                            flatMap(l -> l.autores().stream()
                                    .map(a -> new Autor(a)))
                            .collect(Collectors.toList()).stream().findFirst().get();
                    Optional<Autor> autorBD = repository.buscarAutorPorNombre(libroBuscado.get().autores().stream()
                            .map(a -> a.nombre())
                            .collect(Collectors.joining()));
                    Optional<Libro> libroOptional = repository.buscarLibroPorNombre(nombre);
                    if (libroOptional.isPresent()) {
                        System.out.println("Este libro ya existe en la BD.");
                    } else {
                        Autor autor;
                        if (autorBD.isPresent()) {
                            autor = autorBD.get();
                            System.out.println("Este autor ya existe en la BD");
                        } else {
                            autor = autorAPI;
                            repository.save(autor);
                        }
                        autor.setLibros(libroEncontrado);
                        repository.save(autor);
                    }
                } catch (Exception e) {
                    System.out.println("ERROR: " + e.getMessage());
                }
            } else {
                System.out.println("Libro no encontrado!");
            }
        }
    }

}
