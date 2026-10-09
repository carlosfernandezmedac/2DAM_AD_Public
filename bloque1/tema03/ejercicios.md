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

