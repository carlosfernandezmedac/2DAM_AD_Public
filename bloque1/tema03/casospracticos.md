# Casos Prácticos — 3. Trabajo con Ficheros XML y Excepciones

Todos los ejemplos de DOM/SAX/XPath usan este `fichero.xml` de partida:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE libraries [
    <!ELEMENT libraries (library+)>
    <!ELEMENT library (name, books)>
    <!ATTLIST library location CDATA #REQUIRED>
    <!ELEMENT name (#PCDATA)>
    <!ELEMENT books (book+)>
    <!ELEMENT book (title, author, genre, year)>
    <!ELEMENT title (#PCDATA)>
    <!ELEMENT author (#PCDATA)>
    <!ELEMENT genre (#PCDATA)>
    <!ELEMENT year (#PCDATA)>
]>

<libraries>
    <library location="Jaén">
        <name>Biblioteca Pública</name>
        <books>
            <book>
                <title>El Gran Gatsby</title>
                <author>F. Scott Fitzgerald</author>
                <genre>Ficción</genre>
                <year>1925</year>
            </book>
            <book>
                <title>Cien años de soledad</title>
                <author>Gabriel García Márquez</author>
                <genre>Realismo mágico</genre>
                <year>1967</year>
            </book>
        </books>
    </library>
    <library location="Málaga">
        <name>BIBLIOTECA JARDIN DE MALAGA</name>
        <books>
            <book>
                <title>1984</title>
                <author>George Orwell</author>
                <genre>Distopía</genre>
                <year>1949</year>
            </book>
            <book>
                <title>Don Quijote de la Mancha</title>
                <author>Miguel de Cervantes</author>
                <genre>Novela</genre>
                <year>1605</year>
            </book>
        </books>
    </library>
</libraries>
```

---

## Ejemplos guiados

### Ejemplo 1 — Parser DOM básico

```java
import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class parserDOM {
    public static void main(String[] args) {

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // Validar el documento e ignorar espacios en blanco "sueltos"
            factory.setValidating(true);
            factory.setIgnoringElementContentWhitespace(true);

            DocumentBuilder builder = factory.newDocumentBuilder();

            File file = new File(".\\TEMA03\libreria.xml");

            Document doc = builder.parse(file);
            doc.getDocumentElement().normalize();

            Element root = doc.getDocumentElement();
            System.out.println("Elemento raíz: " + root.getNodeName());

        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

> 💡 Este ejemplo solo llega hasta el nodo raíz (`libraries`). Es el punto de partida antes de navegar hacia dentro del árbol — justo lo que se pide en el Ejercicio 1.

---

### Ejemplo 2 — Consultas con XPath sobre DOM

```java
package DOM;
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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XPathDOM {
    public static void main(String[] args)  {

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(true);
            factory.setIgnoringElementContentWhitespace(true);

            DocumentBuilder builder = factory.newDocumentBuilder();
            File file = new File(".\\libreria.xml");
            Document doc = builder.parse(file);
            doc.getDocumentElement().normalize();

            XPath xPath = XPathFactory.newInstance().newXPath();
            String expression = "/libraries/library";
            NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);

            for(int i=0;i<nodeList.getLength();i++){
                Node nNode = nodeList.item(i);
                System.out.println("\nElemento Actual:" + nNode.getNodeName());
                System.out.println("\nElemento Padre:" + nNode.getParentNode());

                if(nNode.getNodeType()== Node.ELEMENT_NODE){
                    Element eElement = (Element) nNode;
                    System.out.println("Nombre :" +eElement.getElementsByTagName("name").item(0).getTextContent());
                    System.out.println("Ciudad :" +eElement.getAttribute("location"));
                    System.out.println("");

                    String bookExpression = "books/book";
                    NodeList bookNodeList = (NodeList) xPath.compile(bookExpression).evaluate(nNode, XPathConstants.NODESET);

                    for (int j = 0; j < bookNodeList.getLength(); j++) {
                        Node bookNode = bookNodeList.item(j);
                        if (bookNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element bookElement = (Element) bookNode;
                            System.out.println("Título del Libro: " + bookElement.getElementsByTagName("title").item(0).getTextContent());
                            System.out.println("Autor del Libro: " + bookElement.getElementsByTagName("author").item(0).getTextContent());
                            System.out.println("Género del Libro: " + bookElement.getElementsByTagName("genre").item(0).getTextContent());
                            System.out.println("Año del Libro: " + bookElement.getElementsByTagName("year").item(0).getTextContent());
                            System.out.println("-----");
                        }
                    }
                    System.out.println("==========");
                }
            }

        } catch (ParserConfigurationException e) {
      System.err.println("Error de configuración del analizador XML: PARSER CONFIGURATION EXCEPTION " + e.getMessage());
    } catch (IOException e) {
      System.err.println("Error de lectura del archivo XML: " + e.getMessage());
    } catch (XPathExpressionException e) {
      System.err.println("Error en la expresión XPath: " + e.getMessage());
    } catch (SAXException e) {
      System.err.println(
        "Error SAX durante el análisis XML: " + e.getMessage());
    }
    }
}
```

> 💡 Fíjate en el patrón de **XPath anidado**: primero se consulta `/libraries/library` sobre todo el documento (`doc`), y luego, dentro de cada `library`, se vuelve a consultar `books/book` pero esta vez sobre ese nodo concreto (`nNode`) en vez de sobre `doc`. Así se navega nivel a nivel.
>
> También es un buen ejemplo de los 4 tipos de excepción **checked** que puede lanzar este tipo de operaciones, cada una tratada por separado.

---

### Ejemplo 3 — Parser SAX básico

```java
package SAX;
import java.io.File;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.helpers.DefaultHandler;

public class parserSAX {
    public static void main(String[] args) {

        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(true);
            SAXParser saxParser = factory.newSAXParser();
            File file = new File(".\\TEMA03\\Ejemplos\\SAX\\fichero.xml");
            saxParser.parse(file, new DefaultHandler());

        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

> ⚠️ Este ejemplo, tal cual, **no imprime nada**: usa `new DefaultHandler()` sin sobrescribir ningún método, así que valida el XML pero no hace nada con los eventos. Para que SAX sea útil de verdad hay que crear una clase propia que **extienda `DefaultHandler`** y sobrescriba métodos como `startElement()`, `endElement()` o `characters()`. Buen contraste con DOM: en DOM tienes el árbol ya construido; en SAX tienes que "programar" qué hacer en cada evento tú mismo.

---

## Ejemplo 4 — Excepción checked (`IOException`)

```java
package Excepciones;
import java.io.FileReader;
import java.io.IOException;

public class Ejemplo1checkedExceptions {
    public static void main(String[] args) {
         try {
            FileReader file = new FileReader("archivo.txt");
            // Realizar operaciones de lectura en el archivo

        } catch (IOException e) {
            System.out.println("Error de lectura: " + e.getMessage());
        }
    }
}
```


Mismo programa, pero leyendo el fichero y cerrándolo. 

```java
import java.io.FileReader;
import java.io.IOException;

public class Ejemplo4a {
    public static void main(String[] args) {
        try {
            FileReader file = new FileReader("archivo.txt");
            int c;
            while ((c = file.read()) != -1) {
                System.out.print((char) c);
            }
            file.close();                               
        } catch (IOException e) {
            System.out.println("Error de lectura: " + e.getMessage());
        }
    }
}
```

> 💡 `FileReader` lanza `FileNotFoundException`, que es subclase de `IOException`. Al capturar `IOException` se captura también cualquier `FileNotFoundException`. Es **checked**: si se quita el `try/catch`, el código no compila.

---

## Ejemplo 4a — Los arreglos rápidos de VS Code: ¿cuál conviene?

**Planteamiento:** Estás escribiendo en VS Code esta línea para abrir un fichero:

```java
FileReader file = new FileReader("archivo.txt");
```

La línea sale **subrayada en rojo**. Al pasar el ratón por encima aparece la bombilla 💡 con una **Corrección rápida** que ofrece, entre otras, estas dos opciones:

- **Add throws declaration**
- **Surround with try/catch**

**Nudo:** Las dos hacen que el programa compile, pero **no hacen lo mismo**. ¿Qué genera cada una? ¿Cierra el fichero alguna de ellas? ¿Cuál es la forma correcta?

Fichero de entrada `archivo.txt`:
```
Hola mundo
```

---

### Opción 1 — "Surround with try/catch"

VS Code envuelve la línea así:

```java
import java.io.FileNotFoundException;
import java.io.FileReader;

public class Opcion1 {
    public static void main(String[] args) {
        try {
            FileReader file = new FileReader("archivo.txt");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
```

> ⚠️ El error **se trata**, pero el fichero **no va entre paréntesis**: **no se cierra solo**. Habría que cerrarlo tú con `close()`, y ahí aparece el problema de qué pasa si algo falla antes de llegar a él.
>
> Además, VS Code pone `FileNotFoundException` porque mira **solo esa línea**. En cuanto añadas `file.read()` dentro del `try`, el compilador pedirá `IOException`.

---

### Opción 2 — "Add throws declaration"

VS Code **no trata el error**: lo declara en la cabecera de `main`.

```java
import java.io.FileReader;
import java.io.IOException;

public class Opcion2 {
    public static void main(String[] args) throws IOException {
        FileReader file = new FileReader("archivo.txt");
    }
}
```

> ⚠️ `throws` no trata nada ni cierra nada: **lo pasa a quien llamó al método**. 
>
> Si el `throws` estuviera en otro método (por ejemplo `leerFichero()`), el error **volvería a quien lo llamó**, que puede ser `main`, y ahí sí se podría tratar con `try/catch`.

Aquí es donde `throws` tiene sentido: un método **avisa** de que puede fallar y **quien lo llama** decide qué hacer. Ahora `leerFichero` lleva el `throws` y es `main` quien trata el error:

```java
import java.io.FileReader;
import java.io.IOException;

public class Opcion2b {

    // Este método NO trata el error: lo pasa a quien lo llame
    static void leerFichero(String ruta) throws IOException {
        FileReader file = new FileReader(ruta);
        System.out.println("Fichero abierto: " + ruta);
        file.close();
    }

    public static void main(String[] args) {
        try {
            leerFichero("archivo.txt");
            leerFichero("no_existe.txt");
            System.out.println("Esta línea no se ejecuta");
        } catch (IOException e) {
            System.out.println("Error tratado en main: " + e.getMessage());
        }
        System.out.println("El programa sigue");
    }
}
```

**Salida:**
```text
Fichero abierto: archivo.txt
Error tratado en main: no_existe.txt (No such file or directory)
El programa sigue
```
---

### Opción 3 — La forma correcta: el fichero entre paréntesis

**Esta no la genera VS Code**: la escribes tú, moviendo la declaración al paréntesis que va justo después del `try`.

```java
import java.io.FileReader;
import java.io.IOException;

public class Opcion3 {
    public static void main(String[] args) {
        try (FileReader file = new FileReader("archivo.txt")) {
            int c;
            while ((c = file.read()) != -1) {
                System.out.print((char) c);
            }
        } catch (IOException e) {
            System.out.println("Error de lectura: " + e.getMessage());
        }
    }
}
```


> 💡 El fichero se cierra **solo** al terminar el `try`, haya error o no. No hace falta `close()` ni `finally`. Y el `catch` es `IOException`, que cubre tanto el fallo al abrir como los de lectura.

---

### Comparación

| | Surround with try/catch | Add throws declaration | Fichero entre paréntesis |
|---|---|---|---|
| ¿Trata el error? | ✅ | ❌ lo pasa hacia arriba | ✅ |
| ¿Cierra el fichero? | ❌ | ❌ | ✅ |
| ¿Si falla el programa sigue? | ✅ | ❌ se para | ✅ |
| ¿Lo genera VS Code? | ✅ | ✅ | ❌ lo escribes tú |


--- 

### Ejemplo 5 — Excepción unchecked (`ArrayIndexOutOfBoundsException`)

```java
package Excepciones;

public class Ejemplo2UncheckedException1 {
    public static void main(String[] args) {
        int[] numbers = {1, 2, 3};
        System.out.println(numbers[5]); // Esto lanza un ArrayIndexOutOfBoundsException
        System.out.println("Ocurrió una ArrayIndexOutOfBoundsException: Índice fuera de rango.");
    }
}
```

> ⚠️ Este código **compila perfectamente** (a diferencia del Ejemplo 4), porque `ArrayIndexOutOfBoundsException` es *unchecked*. Pero **falla en tiempo de ejecución**: el `System.out.println("Ocurrió...")` nunca llega a ejecutarse, porque el programa se detiene en cuanto se lanza la excepción no capturada. 

---

### Ejemplo 6 — Excepción unchecked (`NullPointerException`)

```java
package Excepciones;

public class Ejemplo2uncheckedexceptions2 {
    public static void main(String[] args) {
        String texto = null;
        int longitud = texto.length();
        System.out.println(longitud);

        System.out.println("El programa no continúa su ejecución normalmente.");
    }
}
```

> 💡 Mismo patrón que el ejemplo anterior: llamar a un método (`.length()`) sobre una referencia `null` lanza `NullPointerException` en tiempo de ejecución, sin avisar en compilación.

---

### Ejemplo 7 — try/catch/finally y métodos de la excepción (`ArithmeticException`)

```java
package Excepciones;

public class Ejemplo4ArithmeticException  {
    public static void main(String[] args) {

        int dividendo = 10;
        int divisor = 0;

        try {
            int resultado = dividendo / divisor;
            System.out.println(resultado);
        } catch (ArithmeticException e) {
            System.out.println("Error aritmético: " + e.getMessage());
            System.out.println("No se ejecuta");
       } finally{
        System.out.println("Bloque finally ejecutado (cierro recursos).");
       }
    }
}
```

> 💡 Buen ejemplo para practicar en clase, descomentando por turnos las llamadas a `e.getCause()`, `e.toString()`, `e.printStackTrace()` y `e.getStackTrace()` que aparecen comentadas en el original, y comprobar qué imprime cada una.
>
> Fíjate también en que el `finally` se ejecuta **siempre**, tanto si hay excepción como si no — es el bloque adecuado para cerrar ficheros, conexiones, etc.

---

### Ejemplo 7b — Ver qué devuelve cada método de la excepción

```java
public class ThrowableExample {
    public static void main(String[] args) {
        try {
            int resultado = dividir(10, 0);
            System.out.println("Resultado: " + resultado);       // no se ejecuta
        } catch (ArithmeticException e) {
            System.out.println("Error: " + e.getMessage());
            System.out.println("Información de la excepción: " + e.toString());
            System.out.println("Pila de llamadas:");
            e.printStackTrace();
            for (StackTraceElement element : e.getStackTrace()) {
                System.out.println("Elemento de la pila: " + element);
            }
        }
    }

    public static int dividir(int dividendo, int divisor) {
        return dividendo / divisor;
    }
}

```

### Ejemplo 8 — Error (`StackOverflowError`)

```java
package Excepciones;

public class Ejemplo3StackOverflowError {
    public static void main(String[] args) {
            recursiveFunction();
    }
            public static void recursiveFunction() {
            recursiveFunction();
        }
    }
```

> ⚠️ **No es una excepción, es un `Error`.** La recursión sin caso base agota la pila de llamadas. A diferencia de las excepciones, los `Error` normalmente **no se intentan capturar**, porque casi nunca hay nada razonable que hacer en el `catch` — la solución real es corregir el código (aquí, añadir una condición de parada a la recursión).
