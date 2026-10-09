# Tema 3 — Trabajo con Ficheros XML

---

## Índice

1. [Introducción](#1-introducción)
2. [Acceso a datos con DOM y SAX](#2-acceso-a-datos-con-dom-y-sax)
3. [Parser DOM en Java](#3-parser-dom-en-java)
4. [Parser SAX en Java](#4-parser-sax-en-java)
5. [Procesamiento de XML: XPath](#5-procesamiento-de-xml-xpath)
6. [Excepciones](#6-excepciones)
7. [Pruebas unitarias: JUnit (introducción)](#7-pruebas-unitarias-junit-introducción)

---

## 1. Introducción

En este tema damos un salto: en vez de leer ficheros "planos" byte a byte o carácter a carácter, vamos a leer ficheros **estructurados** (XML) e interpretarlos como un árbol de datos con significado. Es la antesala directa de trabajar con JSON, con APIs REST, o con cualquier formato de intercambio de datos.

> 💡 **Dónde poner el foco:** de este tema, lo verdaderamente importante es (1) saber elegir entre **DOM y SAX** según el caso, y (2) el **manejo de excepciones**, que no es exclusivo de XML — es una base que vas a usar en el resto del curso. JUnit se ve aquí solo como introducción; no hace falta profundizar todavía.

---

## 2. Acceso a datos con DOM y SAX

**DOM** y **SAX** son dos formas distintas ("parsers" o analizadores) de leer un fichero XML.

```
DOM                                    SAX
───                                    ───
Carga el fichero COMPLETO en           Va leyendo EVENTO a EVENTO
memoria, como un árbol                 (nodo a nodo), sin cargar
                                        el fichero completo

     <libraries>                       "empieza <libraries>"
      /        \                       "empieza <library>"
 <library>  <library>                  "empieza <name>"
     │                                 "texto: Biblioteca Pública"
   <name>                              "termina <name>"
                                        ...
Todo el árbol disponible               Solo tienes el evento actual
para navegar en cualquier dirección    en memoria en cada momento
```

| | SAX | DOM |
|---|---|---|
| Funcionamiento | Basado en eventos | Carga el fichero completo en memoria |
| Recorrido | Nodo por nodo, secuencial | Búsqueda hacia delante y hacia detrás |
| Memoria | Poca (no carga todo el fichero) | Mucha (árbol completo) |
| Velocidad | Rápido | Más lento |
| Edición | Solo lectura | Se pueden insertar/eliminar nodos |

**Regla práctica:**
- Usa **SAX** cuando solo necesitas **recorrer** el fichero de forma secuencial (por ejemplo, extraer datos concretos de un fichero muy grande) y no necesitas manipularlo en memoria.
- Usa **DOM** cuando necesitas tener **todo el árbol disponible** para navegar libremente, buscar hacia delante y hacia atrás, o modificar el documento.

---

## 3. Parser DOM en Java

Se trabaja con la paquetería `javax.xml.parsers`. El flujo típico es:

```java
DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

// Validar el documento e ignorar espacios en blanco "sueltos"
factory.setValidating(true);
factory.setIgnoringElementContentWhitespace(true);

DocumentBuilder builder = factory.newDocumentBuilder();

File file = new File(".\\TEMA03\\Ejemplos\\DOM\\fichero.xml");

// Se carga el fichero COMPLETO en memoria como un Document (árbol)
Document doc = builder.parse(file);
doc.getDocumentElement().normalize();

// A partir de aquí ya se puede navegar el árbol
Element root = doc.getDocumentElement();
System.out.println("Elemento raíz: " + root.getNodeName());
```

Una vez tienes el `Document`, puedes navegar con métodos como `getElementsByTagName("tag")`, `getAttribute("attr")`, `getTextContent()`, `getChildNodes()`, etc.

---

## 4. Parser SAX en Java

```java
SAXParserFactory factory = SAXParserFactory.newInstance();
factory.setValidating(true);

SAXParser saxParser = factory.newSAXParser();
File file = new File("test.xml");

// El segundo parámetro es un "Handler": ahí se define QUÉ hacer con cada evento
saxParser.parse(file, new DefaultHandler());
```

A diferencia de DOM, aquí no obtienes un árbol para navegar libremente: en el método `parse()` se le pasa un **Handler** (normalmente extendiendo `DefaultHandler`) que define qué código ejecutar en cada evento (inicio de documento, inicio/fin de un elemento, texto encontrado, etc.). Es en la definición de ese Handler donde va la lógica real del análisis.

---

## 5. Procesamiento de XML: XPath

**XPath** es una recomendación oficial del W3C para **consultar** información dentro de un documento XML, de forma parecida a como SQL consulta una base de datos.

### Pasos habituales al usar XPath

1. Cargar el fichero con `DocumentBuilder` (como en DOM).
2. Crear el objeto `XPath`.
3. Definir la expresión de búsqueda (un `String`, similar a una ruta de carpetas).
4. Compilar y evaluar la expresión con `compile()` + `evaluate()`.
5. Iterar sobre el resultado (normalmente un `NodeList`).

```java
XPath xPath = XPathFactory.newInstance().newXPath();

String expression = "/libraries/library";
NodeList nodeList = (NodeList) xPath.compile(expression)
        .evaluate(doc, XPathConstants.NODESET);

for (int i = 0; i < nodeList.getLength(); i++) {
    Node nNode = nodeList.item(i);
    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
        Element eElement = (Element) nNode;
        System.out.println(eElement.getElementsByTagName("name").item(0).getTextContent());
    }
}
```

> 💡 Las expresiones XPath se pueden anidar: en el ejemplo anterior, una vez dentro de cada `<library>`, se puede volver a compilar y evaluar otra expresión relativa (`"books/book"`) **sobre ese nodo concreto** (`nNode`) en vez de sobre todo el documento, para bajar un nivel más en el árbol.

---

## 6. Excepciones

Una **excepción** es un evento que interrumpe el flujo normal de un programa durante su ejecución: un fichero no encontrado, una división entre 0, un dato introducido incorrectamente, etc.

### 6.1 Las 3 categorías

```
EXCEPCIONES EN JAVA
│
├── CON CHEQUEO (checked)
│    El compilador las detecta en tiempo de COMPILACIÓN.
│    Obligan a tratarlas (try/catch o "throws").
│    Ej: IOException, FileNotFoundException, ParserConfigurationException
│
├── SIN CHEQUEO (unchecked / RuntimeException)
│    Ocurren en tiempo de EJECUCIÓN. El compilador NO obliga a tratarlas.
│    Suelen ser errores de programación o mal uso de una API.
│    Ej: ArithmeticException, NullPointerException, ArrayIndexOutOfBoundsException
│
└── ERRORES (Error)
     Escapan al control del programador. Rara vez se pueden solucionar en código.
     Ej: StackOverflowError, OutOfMemoryError
```

### 6.2 try / catch / finally

```java
try {
    // Código protegido: propenso a lanzar una excepción
} catch (TipoDeExcepcion e) {
    // Qué hacer si se lanza esa excepción concreta
} finally {
    // Se ejecuta SIEMPRE, se haya lanzado o no la excepción
    // Típico para liberar recursos (cerrar ficheros, conexiones...)
}
```

Se pueden encadenar varios `catch` para tratar distintos tipos de excepción de forma diferenciada, y el bloque `finally` es opcional pero recomendable cuando hay recursos que cerrar.

### 6.3 Métodos útiles de una excepción (`Throwable`)

| Método | Qué devuelve |
|---|---|
| `getMessage()` | Mensaje detallado de la excepción |
| `getCause()` | La causa de la excepción (otro `Throwable`), si la tiene |
| `toString()` | Nombre de la clase + `getMessage()` |
| `printStackTrace()` | Traza completa de la pila de llamadas hasta el error |
| `getStackTrace()` | Array con cada elemento de la pila (el `[0]` es el más alto) |

---


## Resumen visual del tema

```
TEMA 3 — FICHEROS XML

  LEER XML
   │
   ├── DOM  → carga TODO el árbol en memoria · navegación libre · más lento
   └── SAX  → EVENTOS, nodo a nodo · poca memoria · solo lectura · más rápido

  CONSULTAR XML
   └── XPath → expresiones tipo ruta ("/libraries/library") para localizar nodos

  EXCEPCIONES (importante para todo el curso)
   ├── Checked      → obligatorio tratarlas (try/catch o throws)  → IOException...
   ├── Unchecked     → RuntimeException, no obligan a tratarlas   → NullPointerException...
   └── Error         → casi nunca solucionable en código          → StackOverflowError...

   try { … } catch (Tipo e) { … } finally { … }
```



