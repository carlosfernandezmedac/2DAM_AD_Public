# Tema 1 — Introducción al Manejo de Ficheros

---

## Índice

1. [Introducción](#1-introducción)
2. [Concepto de fichero. Tipos](#2-concepto-de-fichero-tipos)
3. [Formas de acceder a un fichero](#4-formas-de-acceder-a-un-fichero)
4. [Acceso secuencial con bytes](#5-acceso-secuencial-con-bytes)
5. [Acceso secuencial con caracteres](#6-acceso-secuencial-con-caracteres)
6. [Acceso aleatorio: RandomAccessFile](#7-acceso-aleatorio-randomaccessfile)
7. [Operaciones con buffer](#8-operaciones-con-buffer)

---

## 1. Introducción

Cada vez que una aplicación guarda una configuración, exporta un informe o procesa un archivo subido por un usuario, está haciendo lo mismo por debajo: **leer o escribir bytes en un fichero**. Antes de llegar a bases de datos, XML o JSON, un desarrollador Java necesita dominar esta capa más básica: la librería `java.io`.

> 💡 **Por qué importa este tema:** casi cualquier aplicación real necesita en algún momento leer un fichero de configuración, procesar un CSV, generar un log o manipular una imagen subida por el usuario. Todo eso se apoya en las clases que vemos aquí.

En este tema veremos cómo Java representa y gestiona los ficheros: desde la clase `File` (que gestiona el fichero como objeto del sistema) hasta las distintas formas de leer y escribir su contenido, byte a byte, carácter a carácter, o saltando directamente a una posición concreta.

---

## 2. Concepto de fichero. Tipos

Un **fichero** es una sucesión de bits almacenada en un dispositivo. Para ser identificado necesita:

- Un **nombre** — para reconocerlo.
- Una **extensión** — indica el tipo de contenido (`.txt`, `.jpg`, `.dat`...).

Según cómo esté codificada su información, distinguimos dos grandes familias:

```
FICHEROS
│
├── DE TEXTO (ASCII/UTF-8)
│    └── Legibles con cualquier editor de texto plano
│        Ej: .txt, .java, .html, .css, .svg
│
└── BINARIOS
     └── Requieren una aplicación específica para interpretarlos
         Ej: .jpg, .mp4, .exe, .dat, .bin
```

- **De texto (ASCII):** contienen líneas de texto legibles directamente. Ejemplos: `.txt`, código fuente (`.java`, `.html`, `.css`...), ficheros de configuración, o formatos de imagen vectorial basados en texto como `.svg`.
- **Binarios:** la información está representada directamente en binario, sin correspondencia directa con caracteres legibles. Para leerlos correctamente hay que conocer de antemano cómo fueron escritos (el mismo "orden" de escritura debe respetarse en la lectura). Ejemplos: `.bin`, `.dat`, y la inmensa mayoría de formatos de imagen (`.jpg`, `.png`) y vídeo (`.mp4`, `.avi`).


---

### 2.1. La clase File (java.io.File)

La clase `File`, del paquete `java.io`, **no lee ni escribe contenido**: sirve para gestionar el fichero como objeto del sistema de archivos — crearlo, moverlo, borrarlo, consultar sus propiedades. Es el equivalente a "manipular el icono del fichero", no su interior.

### Instanciación

```java
File fichero = new File("/carpeta/archivo");
```

### Crear un fichero

```java
File fichero = new File(".\\TEMA01\\Ejemplos\\crearFichero.txt");
if (fichero.createNewFile()) {
    System.out.println("Fichero creado: " + fichero.getName());
} else {
    System.out.println("El fichero ya existe.");
}
```

### Eliminar un fichero

```java
fichero.delete();
```

### Mover un fichero

Para mover un fichero con `renameTo()`, el fichero origen debe existir y la ruta destino debe ser una ruta operativa (el directorio destino tiene que existir; si no, se puede crear antes con `mkdirs()`).

```java
File ficheroOrigen = new File("C:\\temp\\pruebas1.txt");
File ficheroDestino = new File("C:\\temp\\pruebas\\pruebas2.txt");

try {
    if (ficheroOrigen.renameTo(ficheroDestino)) {
        System.out.println("El fichero se movió correctamente");
    } else {
        System.out.println("El fichero no pudo moverse");
    }
} catch (Exception e) {
    e.printStackTrace();
}
```

### Métodos más usados de `File`

| Método | Descripción |
|---|---|
| `createNewFile()` | Genera el fichero indicado |
| `delete()` | Borra el fichero |
| `mkdirs()` | Crea el/los directorio(s) indicado(s) |
| `getName()` | Devuelve el nombre del fichero |
| `getPath()` / `getAbsolutePath()` | Ruta relativa / ruta absoluta |
| `getParent()` | Devuelve el directorio superior |
| `renameTo(File destino)` | Mueve/renombra el fichero a la ruta indicada |
| `exists()` | Comprueba si el fichero existe |
| `canRead()` / `canWrite()` | Comprueban si puede leerse / escribirse |
| `listFiles()` | Devuelve un array con los ficheros del directorio |
| `lastModified()` | Devuelve última modificación |

---

## 3. Formas de acceder a un fichero

Antes de leer o escribir el **contenido** de un fichero (algo que `File` no hace) hay que decidir el criterio de acceso:

```
ACCESO SECUENCIAL                    ACCESO ALEATORIO (DIRECTO)
──────────────────                   ───────────────────────────
[B1]→[B2]→[B3]→[B4]→[B5]             [B1] [B2] [B3] [B4] [B5]
 └────────────┘                                  ▲
 Para leer B4 hay que                            │
 pasar por B1, B2 y B3          El puntero salta directamente
                                 a la posición que le indiques
```

- **Acceso secuencial:** para llegar a un byte o carácter concreto hay que haber pasado antes por todos los anteriores. Es el modo natural cuando se necesita recorrer el fichero de principio a fin.
- **Acceso aleatorio (o directo):** se establece un **puntero en bytes** que indica la posición exacta de lectura/escritura, a la que se puede saltar directamente sin recorrer lo anterior. Es el modo adecuado cuando se necesita acceder a un registro o posición concreta.

| | Acceso basado en caracteres | Acceso basado en bytes |
|---|---|---|
| **Entrada** | `FileReader` | `FileInputStream` |
| **Salida** | `FileWriter` | `FileOutputStream` |
| **Lectura/escritura aleatoria** | — | `RandomAccessFile` |

---

## 4. Acceso secuencial con bytes

`FileInputStream` y `FileOutputStream` leen/escriben un fichero como un flujo ("stream") de bytes "en bruto": son la opción adecuada para imágenes, ficheros binarios, etc.

### Lectura — `FileInputStream`

```java
FileInputStream entrada = new FileInputStream("C:\\temp\\pruebas\\pruebas2.txt");
int data = entrada.read();   // devuelve el primer byte (como entero)
entrada.close();
```

El método `read()` devuelve el byte leído como `int`. Cuando llega al final del fichero devuelve `-1`.

### Escritura — `FileOutputStream`

```java
String cadena = "Esto es una prueba de escritura";
byte[] arrayBytes = cadena.getBytes();
//getBytes() solo hace falta cuando el ORIGEN de los datos es un String en memoria.
FileOutputStream output = new FileOutputStream("C:\\temp\\pruebas\\pruebas2.txt");
output.write(arrayBytes);
output.close();
```

> ⚠️ En los ejemplos se omite a veces el manejo explícito de excepciones por claridad, pero `FileInputStream`/`FileOutputStream` lanzan `FileNotFoundException` e `IOException`, que deben tratarse (lo veremos en detalle en el Tema 3).

---

## 5. Acceso secuencial con caracteres

`FileReader` y `FileWriter` tienen la misma lógica que las clases de bytes, pero están pensadas para trabajar con **flujos de caracteres** en vez de bytes en bruto.

### Lectura — `FileReader`

```java
FileReader lector = new FileReader("C:\\temp\\pruebas\\pruebas2.txt");
int data = lector.read();
System.out.println((char) data);
lector.close();
```

Como `read()` devuelve un `int`, hay que hacer *casting* a `char` para visualizar el carácter.

### Escritura — `FileWriter`

```java
FileWriter escritorFicheros = new FileWriter("C:\\temp\\pruebas\\pruebas2.txt");
escritorFicheros.write("Esto es un ejemplo de escritura");
escritorFicheros.close();
```

Si el fichero no existe, `FileWriter` lo crea automáticamente. Si existe, **lo sobrescribe**.

```java
FileWriter w = new FileWriter("datos.txt", true); // el "true" indica modo append
w.write("Esto se añade al final, sin borrar lo que ya había");
w.close();
```

---

## 6. Acceso aleatorio: RandomAccessFile

`RandomAccessFile` permite abrir un fichero en modo lectura o en modo lectura-escritura, y acceder a una posición concreta del fichero para leer o escribir en ella — es la única clase de este tema que combina lectura **y** escritura sobre el mismo objeto.

### Modos de apertura

| Modo | Significado |
|---|---|
| `"r"` | Solo lectura. Lanza `IOException` si se intenta escribir. |
| `"rw"` | Lectura y escritura. |
| `"rwd"` | Lectura y escritura síncrona: se escriben todas las actualizaciones del **contenido** del fichero. |
| `"rws"` | Igual que `"rwd"`, pero además se escriben los **metadatos**. |

### Instanciación

```java
RandomAccessFile file = new RandomAccessFile("C:\\temp\\pruebas\\pruebas3.txt", "rw");
```

El primer parámetro puede ser un `String` con la ruta o un objeto `File`; el segundo es siempre el modo de acceso.

### Posicionar el puntero

```
seek(8)
 0   1   2   3   4   5   6   7   8   9  ...
[ ] [ ] [ ] [ ] [ ] [ ] [ ] [ ]  ▲
                                 └── el puntero salta aquí
```

```java
file.seek(8);                          // Nos posicionamos en el byte 8
long filePointer = file.getFilePointer(); // Posición actual del puntero (en bytes)
```

### Lectura y escritura byte a byte

```java
int unByte = file.read();   // Lee un byte a partir de la posición del puntero
file.write(68);             // Escribe el byte 68 (código ASCII de 'D') en la posición actual
```

Tras cada `read()` o `write()`, el puntero avanza automáticamente una posición.

### Lectura de un bloque (array de bytes)

```java
byte[] arrayBytes = new byte[1024];
int inicioPuntero = 0;
int size = 1024;
int bytesLeidos = file.read(arrayBytes, inicioPuntero, size);
```

`read(array, posicionInicial, tamaño)` llena el array indicado y devuelve cuántos bytes se han leído realmente.

---

## 7. Operaciones con buffer

Las clases con sufijo **Buffered** (`BufferedInputStream`, `BufferedOutputStream`, `BufferedReader`, `BufferedWriter`) mejoran el rendimiento frente a `FileInputStream`/`FileOutputStream`: en lugar de acceder a disco byte a byte, cargan **bloques completos** en memoria (el *buffer*) y solo vuelven a disco cuando ese bloque se agota.

```
SIN BUFFER                          CON BUFFER
──────────                          ──────────
Disco → 1 byte → Programa           Disco → [bloque de 4KB] → Memoria (buffer)
Disco → 1 byte → Programa                                       │
Disco → 1 byte → Programa                          Programa lee de memoria
     ...cientos de viajes a disco       (mucho más rápido que ir a disco cada vez)
```

> 💡 El acceso a memoria es muchísimo más rápido que a disco. Por eso, cuantos menos "viajes" a disco haga el programa, mejor rendimiento.

```java
int bufferSize = 4 * 1024;
BufferedInputStream bufferedInputStream = new BufferedInputStream(
        new FileInputStream("C:\\temp\\pruebas\\pruebas4.txt"),
        bufferSize);

int info = bufferedInputStream.read();
while (info != -1) {
    info = bufferedInputStream.read();
}
bufferedInputStream.close();
```

Un `BufferedInputStream` **envuelve** a otro stream (aquí, un `FileInputStream`); esto es un patrón habitual en `java.io` que reaparecerá en el Tema 2.

---

## Resumen visual del tema

```
TEMA 1 — MANEJO DE FICHEROS

  FICHERO
   │
   ├── Tipos: texto (ASCII/UTF-8) vs. binario
   │
   ├── Clase File → gestión del fichero como objeto
   │    (crear, mover, borrar, consultar; NO lee/escribe contenido)
   │
   └── Contenido: lectura / escritura
        │
        ├── ACCESO SECUENCIAL
        │    │  (hay que pasar por todo lo anterior)
        │    ├── Bytes      → FileInputStream / FileOutputStream
        │    └── Caracteres → FileReader / FileWriter
        │
        ├── ACCESO ALEATORIO
        │    │  (puntero en bytes, salto directo)
        │    └── RandomAccessFile
        │         modos "r" / "rw" / "rwd" / "rws"
        │         seek() · getFilePointer() · read() · write()
        │
        └── BUFFER (mejora de rendimiento)
             BufferedInputStream / BufferedOutputStream
             BufferedReader / BufferedWriter
             → bloques completos en memoria en vez de disco byte a byte
```

---

## Recursos

- 📖 Documentación oficial: [Java IO (java.io)](https://docs.oracle.com/javase/8/docs/api/?java/io)
