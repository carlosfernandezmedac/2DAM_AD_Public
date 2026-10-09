# Ejercicios — 3. Trabajo con Ficheros XML

---

## Ejercicio 1 — Mostrar el catálogo de bibliotecas con DOM

**Objetivo:** Practicar la navegación de un árbol DOM ya cargado en memoria-

### Instrucciones

Partiendo de `fichero.xml`, escribe un programa Java que, usando **DOM** (`DocumentBuilder`, `Document`, `NodeList`, `Element`...), muestre por consola, para cada biblioteca:

1. El **nombre** de la biblioteca y su **ubicación** (atributo `location`).
2. El **título**, **autor** y **año** de cada libro que contiene.
3. El **número total de libros** de esa biblioteca..

Formato de salida esperado (ejemplo):

```text
Biblioteca: Biblioteca Pública (Jaén)
 - El Gran Gatsby (F. Scott Fitzgerald, 1925)
 - Cien años de soledad (Gabriel García Márquez, 1967)
Total de libros: 2

Biblioteca: BIBLIOTECA JARDIN DE MALAGA (Málaga)
 - 1984 (George Orwell, 1949)
 - Don Quijote de la Mancha (Miguel de Cervantes, 1605)
Total de libros: 2
```

---

# Ejercicio 2 — Mi propio XML: recorrer varios nodos con DOM

**Objetivo:** Crear tu propio fichero XML sobre una temática a tu elección y practicar la navegación del árbol DOM recorriendo **varios niveles de nodos** (igual que en el ejercicio de las bibliotecas).

### Instrucciones

1. **Crea un fichero XML propio** (por ejemplo `mi_fichero.xml`) sobre la temática que quieras: videojuegos, películas, equipos de fútbol, recetas, instituto con alumnos y asignaturas, tienda con productos, etc. Debe cumplir:
   - Un **elemento raíz**.
   - Al menos **2 elementos del primer nivel** (como las `library` del ejemplo), cada uno con **al menos un atributo** (como `location`).
   - Dentro de cada uno, **al menos 2 elementos hijos** (como los `book`), y dentro de ellos **al menos 3 elementos con texto** (como `title`, `author`, `year`).

2. **Escribe un programa Java con DOM**  que recorra el fichero y muestre **por consola**, para cada elemento del primer nivel:
   1. Su **nombre o atributo** identificativo.
   2. Los **datos de cada hijo** que contiene (todos los campos con texto).
   3. El **número total de hijos** que tiene.

3. Al final del programa, muestra un **resumen global** (por ejemplo, el total de elementos recorridos en todo el fichero).

