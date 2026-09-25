# Ejercicios — 1. Introducción al Manejo de Ficheros

---

## Ejercicio 1 — Copiar texto de un fichero a otro

Crea un programa en Java que copie el contenido de un archivo de texto `texto.txt` a otro archivo `copia.txt`, siguiendo estos pasos:

1. Usar **`FileReader`** para leer el archivo de origen, carácter a carácter.
2. Usar **`FileWriter`** para escribir cada carácter leído en el archivo de destino, dentro del mismo bucle.
3. Cerrar ambos ficheros correctamente al finalizar.
4. Manejar las excepciones si el archivo de origen no existe o hay un error de lectura/escritura.
5. Comprueba que el contenido de `copia.txt` es idéntico al de `texto.txt`.


---


## Ejercicio 2 — Copiar una imagen y contar bytes con FileInputStream

Crea un programa en Java que copie una imagen de un lugar a otro, contando cuántos bytes tiene el archivo, siguiendo estos pasos:

1. Definir dos rutas: una imagen de origen (por ejemplo `foto.jpg`) y una de destino (`foto_copia.jpg`).
2. Usar **`FileInputStream`** para leer la imagen de origen byte a byte, y **`FileOutputStream`** para escribir cada byte leído en el archivo de destino.
3. Declarar un contador (`int contador = 0`) que se incremente en cada vuelta del bucle, una por cada byte leído.
4. Al terminar la copia, mostrar por consola el número total de bytes leídos/copiados.
5. Cerrar ambos ficheros correctamente.
6. Manejar las excepciones si el archivo de origen no existe o hay un error de lectura/escritura.
7. Ir al Explorador de Windows, click derecho sobre `foto.jpg` y sobre `foto_copia.jpg` → "Propiedades" → comprobar el tamaño en bytes de cada uno. **¿Coincide con el número que ha impreso tu programa?**
8. Repite el ejercicio completo, pero cambiando `FileInputStream`/`FileOutputStream` por **`FileReader`**/**`FileWriter`**. Vuelve a comprobar el tamaño de la copia resultante en el Explorador de Windows. **¿Coincide esta vez con el original? ¿Se puede abrir la imagen copiada? ¿Por qué crees que pasa esto?**

---

## Ejercicio 3 — Modificar datos en un archivo de texto

Crea un programa en Java que permita modificar el contenido de un archivo de texto llamado `datos.txt`, siguiendo estos pasos:

1. Escribir el abecedario en el fichero mediante `FileWriter`.
2. Pedir al usuario una **posición** (entero) del archivo donde quiere modificar.
3. Pedir al usuario el **carácter** que quiere escribir en esa posición.
4. Usar **`RandomAccessFile`** para posicionarse en esa posición y sobrescribir el contenido.
5. Cerrar el archivo correctamente.
6. Manejar las excepciones si el archivo no existe, la posición es inválida, o hay un error de lectura/escritura.


---

## Ejercicio 4 — Copiar un fichero con buffers

Crea un programa que copie el contenido de `foto.jpg` en `foto_copia_buffer.jpg` usando **buffers**, para mejorar la eficiencia frente a una copia byte a byte. El programa debe:

1. Abrir `foto.jpg` en modo lectura con BufferedInputStream  y `foto_copia_buffer.jpg` en modo escritura (sobrescribiendo si ya existe)usando BufferedOutputStream.
2. Definir un buffer de lectura/escritura, por ejemplo de `1024` bytes.
3. Mientras haya datos en el origen: leer un bloque en el buffer, escribirlo en el destino, y mostrar por consola un mensaje del tipo `Fin copia bloque N`.
4. Al terminar, añadir al final un mensaje de "Copia finalizada correctamente."
5. Cerrar ambos ficheros correctamente.
6. Controlar las excepciones si el fichero origen no existe o hay errores de E/S.


---

## Ejercicio 5 — Copiar un archivo binario (imagen)

Copiar ficheros grandes (por ejemplo, un log de un servidor) es una tarea habitual. Este ejercicio compara, con datos reales, por qué se usa buffer en la práctica.
1. Copia un fichero (usa uno que pese al menos varios MB — una imagen grande o un vídeo corto sirven) usando FileInputStream/FileOutputStream, leyendo byte a byte (sin buffer).
2. Copia el mismo fichero, pero usando BufferedInputStream/BufferedOutputStream, leyendo con un array de 4096 bytes.
3. En los dos casos, mide el tiempo que tarda la copia con System.currentTimeMillis() (antes y después de copiar, y calculando la diferencia).
4. Compara los tiempos por consola y comprueba que el tamaño de las dos copias coincide con el original.


---


## Ejercicio 6 — Reserva de asientos de cine (RandomAccessFile)

Contexto: el fichero asientos.txt ya existe y contiene 20 caracteres L seguidos, uno por cada asiento de la sala (asientos numerados del 0 al 19, todos libres):

LLLLLLLLLLLLLLLLLLLL

1. Pide al usuario el número de asiento que quiere comprar (0-19).
2. Si el número está fuera de ese rango, avisa de que ese asiento no existe/no está disponible.
3. Si el asiento existe, comprueba su estado actual: si ya está comprado (C), avisa de que ya está ocupado; si está libre (L), cámbialo a C usando RandomAccessFile.
4. Cierra el fichero.
5. Maneja la excepción si se produce un error de lectura/escritura.

---

## Ejercicio 7 — Consultar un rango de asientos de golpe (RandomAccessFile + bloque)

Seguimos con asientos.txt (20 caracteres, L o C, uno por asiento). En vez de consultar los asientos uno a uno, queremos consultar de golpe un rango completo — por ejemplo, para mostrar en pantalla "la fila A" (asientos 5 al 9) sin hacer 5 lecturas sueltas.

1. Pide al usuario la posición inicial del rango que quiere consultar (por ejemplo, 5).
2. Pide al usuario cuántos asientos quiere consultar a partir de ahí (por ejemplo, 5, para ver del 5 al 9).
3. Usa RandomAccessFile para saltar (seek) a la posición inicial, y leer ese rango de golpe en un array de bytes (read(array, 0, cantidad)).
4. Muestra por consola el estado de esos asientos, indicando el número de cada uno junto a su estado (L o C).
5. Cierra el fichero.
6. Maneja la excepción si hay un error de lectura.

---


## Ejercicio 8 — Formulario de matriculación de alumnos

iseña un programa que simule un formulario de matriculación de alumnos, con los siguientes campos:

- **Nombre y Apellidos**
- **Email**
- **Fecha de Nacimiento**
- **Género** (Masculino / Femenino)
- **Titulación de Acceso** (FP Grado Medio / FP Grado Superior / Bachillerato)
- **Observaciones**

**Enunciado:**

1. Al arrancar, el programa muestra un pequeño menú con dos opciones: **Guardar** e **Imprimir**.
2. Si el usuario elige **Guardar**:
   - Pide por teclado, con `Scanner`, todos los datos anteriores.
   - Junta todos los datos recogidos en un único `String`, con este formato:

```text
----- Formulario de Matriculación -----
Nombre y Apellidos: Juan Pérez García
Email: juan.perez@gmail.com
Fecha de Nacimiento: 12/04/2005
Género: Masculino
Titulación de Acceso: FP Grado Medio
Observaciones:
Interesado en horario de tarde.
---------------------------------------
```
   - Escribe ese `String` en un fichero `matricula.txt`, usando `FileWriter`.
3. Si el usuario elige **Imprimir**:
   - No pide ningún dato nuevo.
   - Lee el contenido de `matricula.txt` (usando `FileReader`) y lo muestra por pantalla.
4. Maneja las excepciones si hay un error de lectura/escritura.