# Casos Prácticos — 1. Introducción al Manejo de Ficheros

---

### Ejemplo 1 — Crear un fichero (`File`)

```java
import java.io.File;

//Ejemplo: Crea un fichero vacío en la ruta indicada si no existe

public class Ejemplo1 {

    public static void main(String[] args) {
        try {
            File fichero = new File(".\\TEMA01\\Ejemplos\\crearFichero.txt");
            if (fichero.createNewFile()) {
                System.out.println("Fichero creado: " + fichero.getName());
            } else {
                System.out.println("El fichero ya existe.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

### Ejemplo 2 — Mover un fichero con `renameTo`

```java
import java.io.File;

// Ejemplo: Mover carpeta con renameTo

public class Ejemplo2 {

    public static void main(String[] args) {
       
        File ficheroOrigen = new File(".\\TEMA01\\Ejemplos\\crearFichero.txt");
        String nombreCarpeta = "Backup";
        File carpeta = new File(".\\TEMA01\\Ejemplos", nombreCarpeta);
        carpeta.mkdirs();

        File ficheroDestino = new File(".\\TEMA01\\Ejemplos\\Backup\\fichero_movido.txt");
        if (ficheroOrigen.renameTo(ficheroDestino))
            System.out.println("El fichero se movió correctamente");
        else
            System.out.println("El fichero no pudo moverse");
    }
}
```

> 💡 Antes de mover, se crea el directorio destino con `mkdirs()`. Recuerda que `mkdirs()` y  `renameTo()` fallan silenciosamente (devuelve `false`, no lanza excepción) si la ruta destino no existe.

### Ejemplo 3 — Crear una carpeta si no existe

```java
import java.io.File;

//Ejemplo: crear una carpeta en caso de que no exista

public class Ejemplo3 {

    public static void main(String[] args) {
        
        String nombreCarpeta = "NuevaCarpeta";
        File carpeta = new File(".\\TEMA01\\Ejemplos", nombreCarpeta);

        if (carpeta.exists())
            System.out.println("La carpeta " + carpeta.getName() + " ya existe");
        else {
            carpeta.mkdirs();
            System.out.println("La carpeta " + carpeta.getName() + " se ha creado");
            System.out.println("Ruta absoluta " + carpeta.getAbsolutePath());
            System.out.println("Ruta relativa " + carpeta.getPath());
            System.out.println("Carpeta padre " + carpeta.getParent());
        }
    }
}
```

### Ejemplo 4 — Lectura y escritura con caracteres (`FileReader` / `FileWriter`)

Fichero de entrada `texto.txt`:
```
123456789ABCDEF
```

```java
import java.io.FileReader;
import java.io.FileWriter;

//Ejemplo: uso de FileReader y FileWriter

public class Ejemplo4 {

    public static void main(String[] args) {

        String path = "./TEMA01/Ejemplos/texto.txt";
        String pathEscritura = "./TEMA01/Ejemplos/textow.txt";

        try {
            FileReader fr = new FileReader(path);
            int data;
            while ((data = fr.read()) != -1) {
                System.out.print((char) data);
            }
            fr.close();
            System.out.println("\nLectura completada.");
        } catch (Exception e) {
            System.err.println("Error al leer el archivo: " + e.getMessage());
        }

        try {
            FileWriter fw = new FileWriter(pathEscritura);
            fw.write("Esto es un ejemplo de escriturá");
            fw.close();
            System.out.println("Fichero escrito correctamente.");
        } catch (Exception e) {
            System.err.println("Error al escribir en el archivo: " + e.getMessage());
        }
    }
}
```

### Ejemplo 5 — Lectura y escritura con bytes (`FileInputStream` / `FileOutputStream`)

```java
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

//Ejemplo de uso de FileInputStream y FileOutputStream

public class Ejemplo5 {

    public static void main(String[] args) {

        String path = "./TEMA01/Ejemplos/texto.txt";
        String pathEscritura = "./TEMA01/Ejemplos/textow.txt";

        try {
            FileInputStream entrada = new FileInputStream(path);
            int data;
            while ((data = entrada.read()) != -1) {
                System.out.print((char) data);
            }
            entrada.close();
        } catch (Exception e) {
            System.err.println("Error al leer el archivo: " + e.getMessage());
        }

        try {
            String cadena = "Esto es una prueba de escriturá";
            byte[] arrayBytes = cadena.getBytes();

            FileOutputStream output = new FileOutputStream(pathEscritura);
            output.write(arrayBytes);
            output.close();
            System.out.println("\nFichero escrito");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

> 💡 Compara este ejemplo con el anterior: la lógica de recorrer el fichero con `while ((data = entrada.read()) != -1)` es idéntica a la de `FileReader`. La diferencia real está "por dentro": aquí no hay decodificación de caracteres, solo bytes.

### Ejemplo 6 — Acceso aleatorio (`RandomAccessFile`)

```java
import java.io.RandomAccessFile;

//Ejemplo de acceso aleatorio o directo

public class Ejemplo6 {

    public static void main(String[] args) {

        try {
            RandomAccessFile file = new RandomAccessFile("./TEMA01/Ejemplos/texto.txt", "rw");
            file.seek(5);
            long filePointer = file.getFilePointer();
            int unByte = file.read();

            System.out.println((char) unByte);
            System.out.println(filePointer);
            file.write('X');
            file.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

> ⚠️ **Cuidado con esta trampa habitual:** `seek(5)` posiciona el puntero en el byte 5 (el carácter `'6'`, contando desde 0 en `123456789ñABCDEF`). Pero justo después se hace `read()`, que **también mueve el puntero** una posición hacia delante. Por eso el `write('X')` no sobrescribe el byte 5, sino el **byte 6** (el carácter `'7'`). Es un buen ejercicio para que el alumnado prediga el contenido final del fichero antes de ejecutarlo.

### Ejemplo 7 — Operaciones con buffer

```java
import java.io.BufferedInputStream;
import java.io.FileInputStream;

//Ejemplo: Operaciones con buffer

public class Ejemplo7 {

    public static void main(String[] args) {

        try {
            int bufferSize = 12;

            BufferedInputStream bufferedInputStream = new BufferedInputStream(
                    new FileInputStream(".\\TEMA01\\Ejemplos\\Texto.txt"));

            byte[] buffer = new byte[bufferSize];
            int info;
            int blockNumber = 1;

            while ((info = bufferedInputStream.read(buffer)) != -1) {
                String contenidoBloque = new String(buffer, 0, info);
                System.out.println("Contenido del bloque " + blockNumber + " (bytes=" + info + "):");
                System.out.println(contenidoBloque);

                System.out.println("Fin bloque " + blockNumber);
                blockNumber++;
            }

            bufferedInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

> 💡 Aquí `bufferSize` (12) se usa como tamaño del **array** que se pasa a `read(buffer)`, no como tamaño del buffer interno del `BufferedInputStream` (que en este ejemplo usa el tamaño por defecto, ya que no se pasa como segundo parámetro al constructor). Con `texto.txt` (17 bytes contando la `ñ` como 2 bytes en UTF-8), este código imprime **2 bloques**: uno de 12 bytes y otro con el resto.

---

## Caso Práctico 1 — Tratamiento de ficheros

**Planteamiento:** Juan está desarrollando en su empresa un gestor de archivos simple, que debe poder crear directorios nuevos, crear ficheros nuevos y moverlos. También necesita obtener la fecha de la última modificación de dichos ficheros.

**Nudo:** Juan ha encontrado el paquete `java.io` y entiende que ahí está lo que necesita, pero no sabe qué clase concreta usar.

<details>
<summary>💡 Ver solución</summary>

Juan debe escoger la clase **`java.io.File`**, ya que le permite:

- Crear ficheros → `createNewFile()`
- Consultar la fecha de modificación → `lastModified()`
- Mover ficheros entre directorios → `renameTo()`

```java
import java.io.File;

public class GestorArchivos {
    public static void main(String[] args) throws Exception {
        File fichero = new File("gestor/documento.txt");

        // Crear
        if (fichero.createNewFile()) {
            System.out.println("Fichero creado: " + fichero.getName());
        }

        // Fecha de última modificación
        System.out.println("Última modificación: " + fichero.lastModified());

        // Mover
        File carpetaDestino = new File("gestor/archivados");
        carpetaDestino.mkdirs();
        File destino = new File(carpetaDestino, fichero.getName());
        if (fichero.renameTo(destino)) {
            System.out.println("Fichero movido a " + destino.getPath());
        }
    }
}
```

</details>

---

## Caso Práctico 2 — Leyendo y escribiendo información

**Planteamiento:** José necesita recorrer un fichero de texto completo y volcar su contenido en otro fichero más grande que actúa como base de información conjunta de muchos ficheros.

**Nudo:** José duda entre un modo de acceso secuencial o uno aleatorio. ¿Cuál conviene aquí?

<details>
<summary>💡 Ver solución</summary>

José debe elegir un **modo secuencial**: necesita recorrer el fichero de principio a fin, no acceder a posiciones concretas. Las clases adecuadas son `FileReader` (lectura) y `FileWriter` (escritura), por estar orientadas a caracteres.

La lógica es: leer byte a byte con `read()` desde el `FileReader`, y escribir cada byte leído con `write()` en el `FileWriter`. Al terminar, cerrar ambos flujos con `close()` para liberar recursos.

```java
import java.io.FileReader;
import java.io.FileWriter;

public class CopiarContenido {
    public static void main(String[] args) throws Exception {
        FileReader lector = new FileReader("origen.txt");
        FileWriter escritor = new FileWriter("baseGeneral.txt", true); // true = append

        int data = lector.read();
        while (data != -1) {
            escritor.write(data);
            data = lector.read();
        }

        lector.close();
        escritor.close();
        System.out.println("Copia terminada.");
    }
}
```

</details>


