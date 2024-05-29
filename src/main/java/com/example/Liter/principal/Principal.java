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
                    \n
                1 - Agregar un libro a nuestra base de datos
                2 - Buscar por autor en nuestra biblioteca
                3 - Lista de libros guardados
                4 - Lista de autores en nuestra biblioteca
                5 - Lista de autores vivos en un año determinado
                6 - Listar por Idioma
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
                    case 2:
                        buscarAutorPorNombre();
                        break;
                    case 3:
                        listarLibrosRegistrados();
                        break;
                    case 4:
                        listarAutoresRegistrados();
                        break;
                    case 5:
                        listarAutoresVivos();
                        break;
                    case 6:
                        listarLibrosPorIdioma();
                        break;
                    case 0:
                        System.out.println("Cerrando aplicación");
                        break;
                    default:
                        System.out.println("Opción no válida!");
                        break;
                }
            } catch (NumberFormatException e) {
                System.out.println("Opción no válida: " + e.getMessage());

            }
        }
    }
    public void buscarLibroPorTitulo() {
        System.out.println("");
        System.out.println("Ingrese el título a buscar:");
        var nombre = teclado.nextLine();
        var json = consumoAPI.obtenerDatos(URL_BASE + "?search=" + nombre.replace(" ", "+").toLowerCase());

        if (json.isEmpty() || !json.contains("\"count\":0,\"next\":null,\"previous\":null,\"results\":[]")) {
            var datos = conversor.obtenerDatos(json, Datos.class);

            // Process valid data
            Optional<DatosLibro> libroBuscado = datos.libros().stream()
                    .findFirst();
            if (libroBuscado.isPresent()) {
                System.out.println("Título: " + libroBuscado.get().titulo() + ", autor: " + libroBuscado.get().autores().stream()
                                .map(a -> a.nombre()).limit(1).collect(Collectors.joining()) +
                                ", idioma: " + libroBuscado.get().idiomas().stream().collect(Collectors.joining()) +
                                ", descargas: " + libroBuscado.get().descargas() + ". "
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
                        System.out.println("Este libro ya existe en la base de datos.");
                    } else {
                        Autor autor;
                        if (autorBD.isPresent()) {
                            autor = autorBD.get();
                            System.out.println("Este autor ya existe en la base de datos");
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

    public void buscarAutorPorNombre () {

        System.out.println("Ingrese el apellido del autor que deseas buscar:");
        var nombre = teclado.nextLine();
        Optional<Autor> autor = repository.buscarAutorPorNombre(nombre);
        if (autor.isPresent()) {
            System.out.println(
                    "\nAutor: " + autor.get().getNombre() + ", nacimiento: " + autor.get().getNacimiento() +
                            ", defunción: " + autor.get().getFallecimiento() +
                            "\n Obras: " + autor.get().getLibros().stream()
                            .map(l -> l.getTitulo()).collect(Collectors.toList()) + "\n"
            );
        } else {
            System.out.println("El autor no existe en la base de datos local");
        }
    }

    public void listarLibrosRegistrados () {
        System.out.println("");
        List<Libro> libros = repository.buscarTodosLosLibros();
        libros.forEach(l -> System.out.println(
                        "Título: " + l.getTitulo() +
                        ", autor: " + l.getAutor().getNombre() +
                        ", idioma: " + l.getIdioma().getIdioma() +
                        ", número de descargas al momento de guardado: " + l.getDescargas() + "."
        ));
    }

    public void listarAutoresRegistrados () {
        System.out.println("Listado de nuestra selección de autores");
        List<Autor> autores = repository.findAll();
        System.out.println();
        autores.forEach(l -> System.out.println(
                "Autor: " + l.getNombre() +
                        ", nacimiento: " + l.getNacimiento() +
                        ", defunción: " + l.getFallecimiento() + "."
        ));
    }

    public void listarAutoresVivos () {
        System.out.println("Buscar autores vivos en un año determinado.");
        System.out.println("Introduzca un año:");
        try {
            var fecha = Integer.valueOf(teclado.nextLine());
            List<Autor> autores = repository.buscarAutoresVivos(fecha);
            if (!autores.isEmpty()) {
                System.out.println();
                autores.forEach(a -> System.out.println(
                        "- " + a.getNombre() +
                                ", fecha de Nacimiento: " + a.getNacimiento() +
                                ", fecha de Fallecimiento: " + a.getFallecimiento()
                ));
            } else {
                System.out.println("No hay autores vivos en el año registrado");
            }
        } catch (NumberFormatException e) {
            System.out.println("Ingresa un año válido " + e.getMessage());
        }
    }

    public void listarLibrosPorIdioma() {
        System.out.println("");
        var menu = """
                    Elija un número:
                    1 - Español
                    2 - Francés
                    3 - Inglés
                    """;
        System.out.println(menu);

        try {
            var opcion = Integer.parseInt(teclado.nextLine());

            switch (opcion) {
                case 1:
                    buscarLibrosPorIdioma("es");
                    break;
                case 2:
                    buscarLibrosPorIdioma("fr");
                    break;
                case 3:
                    buscarLibrosPorIdioma("en");
                    break;
                default:
                    System.out.println("Opción inválida");
                    break;
            }
        } catch (NumberFormatException e) {
            System.out.println("Opción no válida: " + e.getMessage());
        }
    }

    private void buscarLibrosPorIdioma(String idioma) {
        try {
            Idioma idiomaEnum = Idioma.valueOf(idioma.toUpperCase());
            List<Libro> libros = repository.buscarLibrosPorIdioma(idiomaEnum);
            if (libros.isEmpty()) {
                System.out.println("No hay libros registrados en ese idioma");
            } else {
                System.out.println();
                libros.forEach(l -> System.out.println(
                                "- Título: " + l.getTitulo() +
                                ", autor: " + l.getAutor().getNombre() +
                                ", idioma: " + l.getIdioma().getIdioma() +
                                ", descargas: " + l.getDescargas()
                ));
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Seleccione un número.");
        }
    }
}
