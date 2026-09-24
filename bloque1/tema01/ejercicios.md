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

Crea un programa que copie el contenido de `archivo_origen.txt` en `archivo_destino.txt` usando **buffers**, para mejorar la eficiencia frente a una copia byte a byte. El programa debe:

1. Abrir `foto.jpg` en modo lectura y `foto_copia_buffer.jpg` en modo escritura (sobrescribiendo si ya existe).
2. Definir un buffer de lectura/escritura, por ejemplo de `1024` bytes.
3. Mientras haya datos en el origen: leer un bloque en el buffer, escribirlo en el destino, y mostrar por consola un mensaje del tipo `Fin copia bloque N`.
4. Al terminar, añadir al final un mensaje de "Copia finalizada correctamente."
5. Cerrar ambos ficheros correctamente.
6. Controlar las excepciones si el fichero origen no existe o hay errores de E/S.




