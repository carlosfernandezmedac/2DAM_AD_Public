# Práctica guiada — Provocar excepciones y entregar el código con su salida

---

# PARTE A — Ficheros

## Paso 0 — Preparación

```text
practica_excepciones/
 ├── mis_datos.txt        ← dos líneas de texto con tu nombre y DNI
 ├── iniciales.txt           ← solo dos caracteres, por ejemplo: ab
```

---

## Paso 1 — El fichero no existe, o no es un fichero

```java
import java.io.FileReader;
import java.io.IOException;

public class Paso1 {
    public static void main(String[] args) {
        String ruta = "mis_datos.txt";
        // PRUEBA 1: cambia la ruta por "no_existe.txt"
        // PRUEBA 2: deniega al fichero mis_datos.txt el permiso de lectura desde Windows

        try (FileReader file = new FileReader(ruta)) {
            int c;
            while ((c = file.read()) != -1) {
                System.out.print((char) c);
            }
        } catch (IOException e) {
            System.out.println("Error de lectura: " + e.toString());
        }
    }
}
```
Para quitar los permisos de lectura a tu fichero en **Windows:** clic derecho → Propiedades → Seguridad → denegar "Lectura".

| Prueba | Salida |
|---|---|
| Sin cambios | el contenido del fichero |
| 1 — `"no_existe.txt"` |  |
| 2 - Leer sin permiso de lectura |  |

> 💡 **Misma excepción, dos causas distintas**: el **texto entre paréntesis** dice cuál. `FileNotFoundException` significa "**no se pudo abrir** el fichero".

> **Entrega:** tu código + la salida de las pruebas 1 y 2.
> **Pregunta:** ¿por qué el `catch` es de `IOException` y aun así captura `FileNotFoundException`?

---

## Paso 2 — No tienes permisos

Crea un archivo.txt y quita los permisos de escritura a tu fichero. **Windows:** clic derecho → Propiedades → Seguridad → denegar "Escritura".

```java
import java.io.FileWriter;
import java.io.IOException;

public class Paso2 {
    public static void main(String[] args) {
        try (FileWriter fw = new FileWriter("archivo.txt")) {      // sin permiso de escritura
            fw.write("hola");
        } catch (IOException e) {
            System.out.println("Error de escritura: " + e.toString());
        }
    }
}
```

| Intento | Salida |
|---|---|
| Escribir en un fichero sin permiso | |

> ⚠️ Hazlo con un usuario normal.
>
> **Entrega:** tu código + la salida. 
> **Pregunta:** ¿qué excepción sale, y qué parte del mensaje te dice que es por permisos?

---


## Paso 3 — Una clase llama a otra con `throws`

El fallo ocurre **dentro de otra clase**. Crea `LectorFicheros.java` (con **tu** nombre de clase): este método **no trata** el error, lo declara con `throws` y lo **pasa a quien lo llame**.

```java
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class LectorFicheros1 {

    public String leerPrimeraLinea(String ruta) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(ruta))) {
            return br.readLine();
        }
    }
}
```

### 3a — La clase principal lo trata

```java
import java.io.IOException;

public class Principal1 {
    public static void main(String[] args) {
        LectorFicheros1 lector = new LectorFicheros1();
        try {
            System.out.println(lector.leerPrimeraLinea("mis_datos.txt"));
            System.out.println(lector.leerPrimeraLinea("no_existe.txt"));
            System.out.println("Esta línea no se ejecuta");
        } catch (IOException e) {
            System.out.println("Tratado en main -> " + e.toString());
            e.printStackTrace(System.out);
        }
        System.out.println("El programa sigue");
    }
}
```

```text
Antonio Canales
Tratado en main -> java.io.FileNotFoundException: no_existe.txt (No such file or directory)
java.io.FileNotFoundException: no_existe.txt (No such file or directory)
	at java.base/java.io.FileInputStream.open0(Native Method)
	...
	at java.base/java.io.FileReader.<init>(FileReader.java:60)
	at LectorFicheros.leerPrimeraLinea(LectorFicheros.java:7)
	at Principal1.main(Principal1.java:8)
El programa sigue
```

> 💡 El error **nace en `LectorFicheros`** pero lo **trata `main`**. La pila es **el camino del error**: dentro de Java → `LectorFicheros.leerPrimeraLinea` (línea 7) → `Principal1.main` (línea 8). `"Esta línea no se ejecuta"` no sale, pero **el programa sigue** después.


> **Entrega:** el código y la salida 
> **Pregunta:** ¿qué pasa si quitas el `throws IOException` de `leerPrimeraLinea`? 

---

## Paso 4 — Errores de `IOException` que **no** son "fichero no encontrado"

Hasta aquí, **todo ha sido `FileNotFoundException`**: errores al **abrir**. `IOException` también cubre los fallos **al leer, escribir o cerrar**, y estos **no** son `FileNotFoundException`.

Crea una segunda clase con cinco métodos (todos con `throws IOException`):

```java
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;

public class LectorFicheros2 {

    // 1. Abrir y leer: puede fallar al ABRIR (FileNotFoundException)
    public int leerPrimerCaracter(String ruta) throws IOException {
        try (FileReader fr = new FileReader(ruta)) {
            return fr.read();
        }
    }

    // 2. Leer un fichero que ya se ha cerrado
    public int leerTrasCerrar(String ruta) throws IOException {
        FileReader fr = new FileReader(ruta);
        fr.close();
        return fr.read();
    }

    // 3. Pedir un int (4 bytes) a un fichero que tiene menos
    public int leerEntero(String ruta) throws IOException {
        try (DataInputStream in = new DataInputStream(new FileInputStream(ruta))) {
            return in.readInt();
        }
    }

    // 4. Posicionarse en una posición negativa
    public void irAPosicionNegativa(String ruta) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(ruta, "r")) {
            raf.seek(-1);
        }
    }

    // 5. Escribir en un fichero abierto solo para leer
    public void escribirEnModoLectura(String ruta) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(ruta, "r")) {
            raf.write('X');
        }
    }
}
```

Y una clase principal que llama a cada método con **dos `catch`**: primero `FileNotFoundException`, después `IOException`. Así se ve **por cuál entra** cada caso:

```java
import java.io.FileNotFoundException;
import java.io.IOException;

public class Principal2 {
    public static void main(String[] args) {
        LectorFicheros2 lector = new LectorFicheros2();

        // CASO 1: cambia "iniciales.txt" por "no_existe.txt"
        try {
            System.out.println("Caso 1 -> " + lector.leerPrimerCaracter("iniciales.txt"));
        } catch (FileNotFoundException e) {
            System.out.println("Caso 1 -> catch FileNotFoundException: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Caso 1 -> catch IOException: " + e.toString());
        }

        // CASO 2: leer después de cerrar
        try {
            System.out.println("Caso 2 -> " + lector.leerTrasCerrar("iniciales.txt"));
        } catch (FileNotFoundException e) {
            System.out.println("Caso 2 -> catch FileNotFoundException: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Caso 2 -> catch IOException: " + e.toString());
        }

        // CASO 3: readInt() sobre un fichero de solo 2 bytes
        try {
            System.out.println("Caso 3 -> " + lector.leerEntero("iniciales.txt"));
        } catch (FileNotFoundException e) {
            System.out.println("Caso 3 -> catch FileNotFoundException: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Caso 3 -> catch IOException: " + e.toString());
        }

        // CASO 4: seek con posición negativa
        try {
            lector.irAPosicionNegativa("iniciales.txt");
            System.out.println("Caso 4 -> sin error");
        } catch (FileNotFoundException e) {
            System.out.println("Caso 4 -> catch FileNotFoundException: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Caso 4 -> catch IOException: " + e.toString());
        }

        // CASO 5: escribir con el fichero abierto en modo "r"
        try {
            lector.escribirEnModoLectura("iniciales.txt");
            System.out.println("Caso 5 -> sin error");
        } catch (FileNotFoundException e) {
            System.out.println("Caso 5 -> catch FileNotFoundException: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Caso 5 -> catch IOException: " + e.toString());
        }
    }
}
```

**Salida con los ficheros correctos** (`iniciales.txt` con texto corto):

```text
Caso 1 -> 72
Caso 2 -> catch IOException (IOException): Stream closed
Caso 3 -> catch IOException (EOFException): 
Caso 4 -> catch IOException (IOException): Negative seek offset
Caso 5 -> catch IOException (IOException): Acceso denegado
```

**Si en el caso 1 cambias a `"no_existe.txt"`:**

```text
Caso 1 -> catch FileNotFoundException: no_existe.txt (No such file or directory)
```

> 💡 **Qué ver:**
> - **`FileNotFoundException`** = falla al **abrir**. **`IOException`** (y sus otras hijas) = falla **después de abrir**: al leer, escribir o cerrar.
> - El caso 3 es `EOFException`, otra **hija** de `IOException`: por eso entra por el segundo `catch`. Su mensaje es **`null`**: la clase es lo que te dice qué ha pasado.
> - Los métodos **no tratan nada**: lo hace `Principal`, que es quien llama (igual que en el paso 3).
>
> **Entrega:** el código y la salida de cada **caso**.
> **Pregunta:** ¿por qué los casos 2 a 5 no entran por `catch (FileNotFoundException)`?

---

## Paso 5 — Los métodos de la excepción

Sobre el mismo fallo (fichero que no existe), llama a **todos los métodos**:

```java
import java.io.FileReader;
import java.io.IOException;

public class Paso5 {
    public static void main(String[] args) {
        try (FileReader file = new FileReader("no_existe.txt")) {
            file.read();
        } catch (IOException e) {
            System.out.println("getMessage():  " + e.getMessage());
            System.out.println("getCause():    " + e.getCause());
            System.out.println("toString():    " + e);
            System.out.println("getStackTrace()[0]: " + e.getStackTrace()[0]);
            System.out.println("printStackTrace():");
            e.printStackTrace(System.out);
        }
    }
}
```

```text
getMessage():  no_existe.txt (No such file or directory)
getCause():    null
toString():    java.io.FileNotFoundException: no_existe.txt (No such file or directory)
getStackTrace()[0]: java.base/java.io.FileInputStream.open0(Native Method)
printStackTrace():
java.io.FileNotFoundException: no_existe.txt (No such file or directory)
	at java.base/java.io.FileInputStream.open0(Native Method)
	...
	at Paso5.main(Paso5.java:5)
```

| Método | Qué devuelve | Excepción capturada |
|---|---|---|
| `getMessage()` |  |  |
| `getCause()` | | |
| `toString()` |  | |
| `printStackTrace()` | |  |
| `getStackTrace()` | |  |

> **Entrega:** código + salida



---

# PARTE B — XML con DOM

Partes de **tu programa del Ejercicio 2** (el que recorre tu XML propio). Aquí se usa un XML de **equipos de fútbol**; tú usarás **tu temática y tus etiquetas**.

## Paso 6 — Provocar las excepciones del temario con DOM

Reescribe tu programa con **un `catch` por cada excepción del Tema 3** y con las cuatro constantes del principio, que son las que cambiarás en cada prueba (usa **tus** etiquetas, atributo y ruta):

```java
import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class DomExcepciones {

    public static void main(String[] args) {

        String fichero   = "equipos.xml";        // PRUEBAS 1, 2 y 7
        String etiqueta  = "nombre";             // PRUEBA 3
        String atributo  = "ciudad";             // PRUEBA 4
        String expresion = "/equipos/equipo";    // PRUEBAS 5 y 6

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new File(fichero));
            doc.getDocumentElement().normalize();

            XPath xPath = XPathFactory.newInstance().newXPath();
            NodeList equipos = (NodeList) xPath.compile(expresion).evaluate(doc, XPathConstants.NODESET);

            for (int i = 0; i < equipos.getLength(); i++) {
                Element equipo = (Element) equipos.item(i);
                String nombre = equipo.getElementsByTagName(etiqueta).item(0).getTextContent();
                String ciudad = equipo.getAttribute(atributo);
                System.out.println("Equipo: " + nombre + " (ciudad: " + ciudad + ")");
            }

        } catch (ParserConfigurationException e) {
            System.out.println("Error de configuración del analizador: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Error de lectura del fichero XML (" + e.getClass().getSimpleName() + "): " + e.getMessage());
        } catch (SAXException e) {
            System.out.println("Error SAX (" + e.getClass().getSimpleName() + "): " + e.getMessage());
        } catch (XPathExpressionException e) {
            System.out.println("Error en la expresión XPath: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Otro error (" + e.getClass().getSimpleName() + "): " + e.getMessage());
        }
    }
}
```

**Sin cambios:**

```text
Equipo: Real Betis (ciudad: Sevilla)
Equipo: Atlético de Madrid (ciudad: Madrid)
```

### Pruebas

| # | Qué haces | Qué sale | Excepción |
|---|---|---|---|
| 1 | `fichero = "no_hay.xml"` | |  |
| 2a | Cambia un `</nombre>` por `</nomre>` en una copia del XML y úsala |  |  |
| 2b | Borra la última línea (`</equipos>`) de una copia |  |  |
| 2c | Usa un fichero **vacío** |  |  |
| 3 | `etiqueta = "nombr"` (mal escrita) | |  |
| 4 | `atributo = "ciudd"` (mal escrito) |  | |
| 5 | `expresion = "/equipos/equipo["` |  |  |
| 6 | `expresion = "/equipos/club"` (ruta que no existe) |  |  |
| 7 | Quita los permisos al XML (como en el paso 2) |  |  |



> **Entrega:** tu código con tu XML y la salida de **las pruebas** con justificaciones

