# Tema 2 — Flujos

---

## Índice

1. [Introducción](#1-introducción)
2. [Definición y tipos de Streams](#2-definición-y-tipos-de-streams)
3. [Clases de análisis de flujos](#3-clases-de-análisis-de-flujos)
4. [Clases para tratamiento de información: DataInputStream / DataOutputStream](#4-clases-para-tratamiento-de-información-datainputstream--dataoutputstream)

---

## 1. Introducción

En el Tema 1 vimos las clases más básicas de `java.io`: `File`, `FileInputStream`/`FileOutputStream`, `FileReader`/`FileWriter` y las variantes `Buffered`. En este tema ampliamos el mapa: el paquete `java.io` tiene más clases de flujos ("streams"), cada una pensada para un caso de uso concreto.

> 💡 **No todas pesan lo mismo en la práctica.** Este tema mezcla clases muy usadas en el día a día (`StreamTokenizer`, `LineNumberReader`, `DataInputStream`/`DataOutputStream`) con otras bastante específicas (tuberías, arrays) que rara vez aparecen fuera de casos muy concretos.

Un **Stream** es una secuencia ordenada de información con un origen (entrada) o un destino (salida) — nunca ambos a la vez: son **unidireccionales**.

La diferencia con el Tema 1: las clases del Tema 1 (FileReader, FileInputStream...) solo transportan datos en bruto — bytes o caracteres sueltos, sin entender nada de su significado. Estas tres clases del Tema 2 añaden una capa de interpretación
---

## 2. Definición y tipos de Streams

Según lo que traten, los Streams de `java.io` se agrupan así:

| | Tema 1 | Tema 2 (estas 3) |
|---|---|---|
| **Qué hacen** | Mover bytes/caracteres tal cual | Interpretar/estructurar esos datos |
| **StreamTokenizer** | — | Distingue palabras de números |
| **LineNumberReader** | — | Sabe en qué línea estás |
| **DataOutputStream** | — | Entiende tipos (`int`, `float`...), no solo bytes sueltos |


---


## 3. Clases de análisis de flujos

Este bloque sí es de uso frecuente: son clases pensadas para **analizar** el contenido de un flujo (parsing), no solo leerlo tal cual.


### 3.1. StreamTokenizer — dividir en "tokens"

Un **token** es un fragmento con significado propio (una palabra, un número...). `StreamTokenizer` recorre el flujo y lo va partiendo en tokens, indicando de qué tipo es cada uno.

```java
StreamTokenizer streamTokenizer = new StreamTokenizer(
        new StringReader("Hola mi edad es 45"));

while (streamTokenizer.nextToken() != StreamTokenizer.TT_EOF) {
    if (streamTokenizer.ttype == StreamTokenizer.TT_WORD) {
        System.out.println(streamTokenizer.sval);   // token de tipo palabra
    } else if (streamTokenizer.ttype == StreamTokenizer.TT_NUMBER) {
        System.out.println(streamTokenizer.nval);   // token de tipo número
    } else if (streamTokenizer.ttype == StreamTokenizer.TT_EOL) {
        System.out.println();                       // fin de línea
    }
}
```

Constantes clave:

| Constante | Significado |
|---|---|
| `TT_EOF` | Fin del fichero (End Of File) |
| `TT_EOL` | Fin de línea (End Of Line) — solo si se activa con `eolIsSignificant(true)` |
| `TT_WORD` | El token es una palabra |
| `TT_NUMBER` | El token es un número |

> ⚠️ Por defecto, `StreamTokenizer` **ignora** los saltos de línea (no genera `TT_EOL`). Si necesitas detectarlos, hay que activarlo explícitamente con `streamTokenizer.eolIsSignificant(true);`.


### 3.2 LineNumberReader — leer y contar líneas

Es un `BufferedReader` que además **cuenta las líneas** que va leyendo. Empieza en la línea 0 y suma 1 cada vez que encuentra un salto de línea.

| Método | Qué hace |
|---|---|
| `getLineNumber()` | Devuelve el número de línea en la que se está leyendo actualmente |
| `setLineNumber(int n)` | Fuerza el contador a la línea indicada (no mueve el puntero de lectura por sí solo) |
| `readLine()` | Devuelve el contenido completo de la siguiente línea (`String`), o `null` si no hay más |

```java
LineNumberReader lineNumberReader =
        new LineNumberReader(new FileReader("C:\\temp\\pruebas\\pruebas2.txt"));

String line = lineNumberReader.readLine();
while (line != null) {
    System.out.println("Contenido de la línea número: " + lineNumberReader.getLineNumber());
    System.out.println(line);
    line = lineNumberReader.readLine();
}
lineNumberReader.close();
```

> ⚠️ **Cuidado con `setLineNumber()`**: cambia el valor que devuelve `getLineNumber()`, pero **no reposiciona el puntero de lectura** al principio de esa línea. Si quieres saltar directamente a una línea concreta sin leer las anteriores, `LineNumberReader` no lo permite por sí solo — tendrás que combinarlo con lógica propia (ver el Ejercicio 2 del tema, con sus dos soluciones distintas).

---

## 4. Clases para tratamiento de información: DataInputStream / DataOutputStream

Cuando se necesita leer o escribir **tipos primitivos de Java** (`int`, `float`, `long`, `double`...) directamente, sin pasar por texto, se usan `DataOutputStream` (escritura) y `DataInputStream` (lectura). Son la base de muchos formatos de fichero binario.

```
ESCRITURA                          LECTURA
──────────                         ───────
DataOutputStream                   DataInputStream
  .writeInt(123)      ──┐      ┌──  .readInt()    → 123
  .writeFloat(123.45F)  ├─ fichero binario ─┤      .readFloat()  → 123.45
  .writeLong(789L)      ┘      └──  .readLong()   → 789
```

### Escritura — `DataOutputStream`

```java
DataOutputStream dataOutputStream = new DataOutputStream(
        new FileOutputStream("C:\\temp\\pruebas\\data.bin"));

dataOutputStream.writeInt(123);
dataOutputStream.writeFloat(123.45F);
dataOutputStream.writeLong(789);
dataOutputStream.close();
```

### Lectura — `DataInputStream`

```java
DataInputStream dataInputStream = new DataInputStream(
        new FileInputStream("C:\\temp\\pruebas\\data.bin"));

int entero = dataInputStream.readInt();
float numeroFloat = dataInputStream.readFloat();
long numeroLong = dataInputStream.readLong();
dataInputStream.close();
```

> ⚠️ **La regla de oro de `DataInputStream`: el orden importa.** Hay que leer **exactamente en el mismo orden y con los mismos tipos** con los que se escribió. Además, como los datos son binarios puros, no hay forma de "adivinar" dónde termina un dato y empieza otro, ni de distinguir un `-1` válido leído como número del `-1` que devuelve `read()` al llegar al final del fichero. Por eso es imprescindible que quien lee conozca de antemano la estructura exacta del fichero.

`DataInputStream` es subclase de `InputStream`, así que también hereda `read()` byte a byte si se necesita.

---

## Resumen visual del tema

```
TEMA 2 — FLUJOS

  STREAM = secuencia ORDENADA y UNIDIRECCIONAL de datos (o entrada, o salida)
   │
   │
   ├── ANÁLISIS (parsing de flujos)
   │    ├── StreamTokenizer            → divide en tokens: TT_WORD / TT_NUMBER / TT_EOL / TT_EOF
   │    └── LineNumberReader           → readLine() + getLineNumber()/setLineNumber()
   │
   └── INFORMACIÓN (tipos primitivos)
        DataOutputStream.writeInt()/writeFloat()/writeLong()...
        DataInputStream.readInt()/readFloat()/readLong()...
        ⚠️ mismo orden y mismos tipos al leer que al escribir
```

---

## Recursos

- 📖 Documentación oficial: [Java IO (java.io)](https://docs.oracle.com/javase/8/docs/api/?java/io)
