# Ejercicios — 2. Flujos

> Ejercicios para que el alumnado los resuelva de forma autónoma.

---

## Ejercicio 1 — Extracción de datos usando clases de análisis de flujos

**Objetivo:** Practicar el análisis de flujos de datos en Java utilizando las clases vistas (`LineNumberReader`, `StreamTokenizer`) para analizar un archivo de texto y contar cuántas palabras y cuántos números hay en cada línea.

### Instrucciones

1. Crea un archivo de texto llamado `entrada.txt` que contenga líneas con una combinación de palabras y números. Por ejemplo:

   ```text
   Hola 123 Mundo
   Prueba 45.67 de texto
   12345
   ```

2. Escribe un programa en Java que lea el archivo y cuente cuántas palabras y cuántos números hay en cada línea. Utiliza `LineNumberReader` para leer línea por línea, y `StreamTokenizer` para diferenciar palabras de números dentro de cada línea.

3. Muestra el resultado en la terminal con este formato (ajustando los valores a lo que obtengas):

   ```text
   Línea 1: Hola 123 Mundo
   Palabras: 2, Números: 1

   Línea 2: Prueba 45.67 de texto
   Palabras: 3, Números: 1

   Línea 3: 12345
   Palabras: 0, Números: 1
   ```
---

## Ejercicio 2 — Lectura de una línea específica de un archivo

**Objetivo:** Practicar el uso de `LineNumberReader` para acceder a una línea concreta de un archivo de texto, indicada por el usuario.

### Instrucciones

1. Crea un archivo de texto llamado `entrada.txt` con varias líneas de contenido. Por ejemplo:

   ```text
   Primera línea
   Segunda línea
   Tercera línea
   Cuarta línea
   ```

2. Escribe un programa en Java que:
   - Pida al usuario, por teclado, el número de línea que desea leer.
   - Utiliza `LineNumberReader` para localizar esa línea.
   - Muestre su contenido en consola.
   - Si el número de línea no existe, muestre un mensaje de aviso.

   Ejemplo de ejecución:

   ```text
   Indica qué línea quieres leer: 3
   Contenido de la línea número 3:
   Tercera línea
   ```

---

## Ejercicio 3 — Total de un ticket de compra

**Utilidad real:** extraer importes de un texto sin parsearlo a mano.

Dado el texto `"Pan 1.5 Leche 1 Huevos 2.25"` (producto seguido de su precio), calcula el **total** de la compra y muestra cada producto con su precio.


**Salida:**
```
Pan: 1.5
Leche: 1.0
Huevos: 2.25
TOTAL: 4.75
```


---

## Ejercicio 4 — Buscar errores en un fichero de log

**Utilidad real:** revisar un log del servidor y saber en qué líneas hay errores.

Dado un fichero `servidor.log`:

```
2026-10-07 09:00:01 INFO Servidor iniciado
2026-10-07 09:00:05 ERROR No se pudo conectar a la base de datos
2026-10-07 09:01:10 INFO Reintentando conexion
2026-10-07 09:01:12 ERROR Timeout
2026-10-07 09:02:00 INFO Conexion establecida
```

Muestra las líneas que contienen `ERROR`, podéis usar una comparación `linea.contains("ERROR")`, indicando su número de línea, y al final cuántos errores hay.


**Salida:**
```
Línea 2: 2026-10-07 09:00:05 ERROR No se pudo conectar a la base de datos
Línea 4: 2026-10-07 09:01:12 ERROR Timeout
Total de errores: 2
```
