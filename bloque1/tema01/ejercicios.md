# Ejercicios — 1. Introducción al Manejo de Ficheros

---

## Ejercicio 1 — Copiar texto de un fichero a otro con FileReader/FileWriter

Crea un programa en Java que copie el contenido de un archivo de texto `texto.txt` a otro archivo `copia.txt`, siguiendo estos pasos:

1. Usar **`FileReader`** para leer el archivo de origen, carácter a carácter.
2. Usar **`FileWriter`** para escribir cada carácter leído en el archivo de destino, dentro del mismo bucle.
3. Cerrar ambos ficheros correctamente al finalizar.
4. Manejar las excepciones si el archivo de origen no existe o hay un error de lectura/escritura.
5. Comprueba que el contenido de `copia.txt` es idéntico al de `texto.txt`.

--- 


## Ejercicio 2 — Copiar una imagen y contar bytes con FileInputStream/FileOutputStream

Crea un programa en Java que copie una imagen de un lugar a otro, contando cuántos bytes tiene el archivo, siguiendo estos pasos:

1. Definir dos rutas: una imagen de origen (por ejemplo `foto.jpg`) y una de destino (`foto_copia.jpg`).
2. Usar **`FileInputStream`** para leer la imagen de origen byte a byte, y **`FileOutputStream`** para escribir cada byte leído en el archivo de destino.
3. Declarar un contador (`int contador = 0`) que se incremente en cada vuelta del bucle, una por cada byte leído.
4. Al terminar la copia, mostrar por consola el número total de bytes leídos/copiados.
5. Cerrar ambos ficheros correctamente.
6. Manejar las excepciones si el archivo de origen no existe o hay un error de lectura/escritura.
7. Ir al Explorador de Windows, click derecho sobre `foto.jpg` → "Propiedades" → comprobar el tamaño en bytes de cada uno. **¿Coincide con el número que ha impreso tu programa?**

8. Repite el ejercicio completo, pero cambiando `FileInputStream`/`FileOutputStream` por **`FileReader`**/**`FileWriter`**. **¿Se puede abrir la imagen copiada? ¿Por qué crees que pasa esto?**


