# Casos Prácticos — 3. Trabajo con Ficheros XML

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

> 💡 Fíjate en que el primer libro de la biblioteca de Jaén ("El Gran Gatsby") aparece **repetido**: es útil para plantear en clase preguntas como "¿cuántos libros distintos hay realmente en Jaén?".

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

> 💡 `FileReader` lanza `FileNotFoundException`, que es subclase de `IOException`. Al capturar `IOException` se captura también cualquier `FileNotFoundException`. Es **checked**: si se quita el `try/catch`, el código no compila.

---

### Ejemplo 4a — Cerrar el fichero 

Ejemplo 4b — Cerrar el fichero

Mismo programa que el Ejemplo 4, pero leyendo el fichero y cerrándolo. 

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

### Ejemplo 4b — Cerrar el fichero:  con el fichero entre paréntesis después del try (no hace close())

```java
import java.io.FileReader;
import java.io.IOException;

public class Ejemplo4b {
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


