# Tema 4 — Manejo de Conectores I

---

## Índice

1. [Introducción](#1-introducción)
2. [Qué es un conector](#2-qué-es-un-conector)
3. [Protocolos de acceso: JDBC vs ODBC](#3-protocolos-de-acceso-jdbc-vs-odbc)
4. [Arquitectura y tipos de driver](#4-arquitectura-y-tipos-de-driver)
5. [Conexión en código: DriverManager](#5-conexión-en-código-drivermanager)

---

## 1. Introducción

Hasta ahora hemos trabajado con ficheros: texto, binarios, XML. En este bloque damos el salto a la fuente de datos más habitual en cualquier aplicación real: **una base de datos relacional**. Pero un programa Java no sabe comunicarse por sí solo con un gestor de bases de datos — necesita una pieza intermedia que abra la conexión, envíe las sentencias SQL y devuelva los resultados. Esa pieza es el **conector**.

> 💡 **La idea central de este tema:** un buen conector hace que tu aplicativo no tenga que preocuparse de los detalles internos de la base de datos concreta que hay detrás. Cambias de MySQL a Oracle, y en teoría solo cambias el driver y la URL — el resto del código apenas se toca (siempre que no uses sentencias propias de un gestor concreto).

---

## 2. Qué es un conector

Un **conector** (o **driver**) es una serie de clases y librerías que unen la capa de la aplicación con la capa de base de datos. La aplicación y el gestor de bases de datos son programas distintos que hablan protocolos distintos; el conector se encarga de **abrir la conexión, enviar las sentencias SQL y devolver los resultados a Java**. Sin él no podemos conectarnos ni lanzar consultas.

```
APLICATIVO JAVA  ───►  CONECTOR  ───►  BASE DE DATOS
(orientado a objetos)                  (relacional)
```

> 💡 El conector solo **transporta** los datos: un `SELECT` devuelve filas y columnas (`ResultSet`), y somos nosotros quienes construimos los objetos a mano. Esa diferencia entre objetos y tablas (el *desfase objeto-relacional*) la estudiaremos más adelante con Hibernate, que automatiza esa traducción.

---

## 3. Protocolos de acceso: JDBC vs ODBC

Un conector (o *driver*) es, en esencia, una API que facilita la conexión a una base de datos concreta. Existen dos protocolos históricos:

| Protocolo | Desarrollado por | Uso en este curso |
|---|---|---|
| **JDBC** (Java Database Connectivity) | Sun (ahora Oracle) | ✅ El que usaremos siempre |
| **ODBC** (Open DataBase Connectivity) | Microsoft | Solo como puente histórico (driver tipo 1) |

Nos centraremos en **JDBC** por ser el estándar nativo de Java y el más usado con diferencia en el desarrollo de software actual.

---

## 4. Arquitectura y tipos de driver

**Arquitecturas**

- **2 capas:** la aplicación, con su driver, se conecta directamente a la base de datos. La base de datos puede estar en otro equipo; lo que no hay es un servidor intermedio. Es lo que haremos siempre.
- **3 capas:** entre la aplicación y la base de datos hay un servidor intermedio (*middleware*) que traduce y reparte las peticiones. Se usa en aplicaciones con mucha carga (balanceo, caché).

**Tipos de driver JDBC**

Cada base de datos tiene su propio driver: un fichero `.jar` que se descarga de su web o se añade con Maven. Los dos que usaremos son:

| Base de datos | Driver (`.jar`) |
|---|---|
| MySQL | `mysql-connector-j` |
| H2 | `h2` |

Pasos:
1. Descargar el `.jar` (o añadir la dependencia en Maven).
2. Añadirlo al proyecto (a las librerías o al `pom.xml`).
3. Conectarse con `DriverManager.getConnection(...)`, como veremos en el apartado 5.

No hay que instalar nada más: son drivers 100 % Java que hablan directamente con el servidor. *(Existen otros tipos de driver más antiguos, como el puente JDBC-ODBC, que ya no se usan.)*

---

## 5. Conexión en código: DriverManager

El objeto central para establecer una conexión "a mano" con JDBC es `java.sql.DriverManager`.

### Paso 1 — Definir la URL de conexión (y, opcionalmente, el driver)

```java
private static final String URL_CONEXION = "jdbc:mysql://localhost:3306/Pruebas";
// private static final String DRIVER = "com.mysql.cj.jdbc.Driver";  // solo hace falta en proyectos antiguos
```

### Paso 2 — Abrir la conexión y consultar

```java
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Conexion {

    private static final String URL      = "jdbc:mysql://localhost:3306/tienda";
    private static final String USUARIO  = "root";
    private static final String PASSWORD = "tucontraseña";

    public static void main(String[] args) {

        String sql = "SELECT id_cliente, nombre, ciudad FROM cliente";

        try (Connection con = DriverManager.getConnection(URL, USUARIO, PASSWORD);
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                System.out.println(rs.getInt("id_cliente") + " - "
                        + rs.getString("nombre") + " (" + rs.getString("ciudad") + ")");
            }

        } catch (SQLException e) {
            System.out.println("Error SQL " + e.getErrorCode() + ": " + e.getMessage());
        }
    }
}
```

El `try (...)` con recursos (*try-with-resources*) **cierra solo** `ResultSet`, `Statement` y `Connection`, hayamos tenido excepción o no.

### Las piezas clave

- **`Connection`** es una **interfaz** (no una clase concreta), porque puede tener implementaciones distintas según el driver.
- **`DriverManager.getConnection(url, usuario, password)`** busca el driver adecuado para esa URL y devuelve el objeto `Connection` ya conectado.
- **`SQLException`**: algo falla en la propia base de datos o en la conexión (sintaxis, permisos, servidor apagado...). Tiene `getMessage()`, `getSQLState()` y `getErrorCode()`.
- Si el driver (`.jar`) no está en el classpath, `getConnection` lanza `SQLException` con el mensaje *"No suitable driver found"*.

### Cómo se escribe de forma clásica

```java
Connection dbConnection = null;
Statement statement = null;
try {
    Class.forName(DRIVER);  // registra el driver
    dbConnection = DriverManager.getConnection(URL_CONEXION, usuario, password);
    statement = dbConnection.createStatement();
    ResultSet rs = statement.executeQuery(sql);
    while (rs.next()) { /* ... */ }
} catch (SQLException e) {
    System.out.println(e.getMessage());
} catch (ClassNotFoundException e) {
    System.out.println(e.getMessage());   // el driver no está en el classpath
} finally {
    if (statement != null) statement.close();       // close() también lanza SQLException:
    if (dbConnection != null) dbConnection.close(); // hay que capturarla o declarar "throws SQLException"
}
```

> ⚠️ **Dos cosas que ya están anticuadas:**
> 1. `Class.forName(DRIVER)` **ya no hace falta** desde JDBC 4.0 (Java 6): si el `.jar` está en el classpath, `DriverManager` encuentra el driver solo. Por eso el PDF dice que los drivers deben estar registrados antes de usarse, y hoy no es cierto.

---

## 6. La interfaz Statement: CRUD con JDBC:

`Statement` es la interfaz que ejecuta las sentencias SQL desde Java y recoge los resultados. Se obtiene a partir de una `Connection`. 

Todo lo que hicimos en Workbench (INSERT, SELECT, UPDATE, DELETE) se puede lanzar desde Java: la sentencia SQL se escribe como un `String` y se la pasamos al `Statement`. Solo hay que elegir el método adecuado:

| Operación | Sentencia SQL | Método de `Statement` | Qué devuelve |
|---|---|---|---|
| **C**reate | `INSERT` | `executeUpdate(sql)` | `int`: filas afectadas |
| **R**ead | `SELECT` | `executeQuery(sql)` | `ResultSet` con las filas |
| **U**pdate | `UPDATE` | `executeUpdate(sql)` | `int`: filas afectadas |
| **D**elete | `DELETE` | `executeUpdate(sql)` | `int`: filas afectadas |
| Cualquiera | las anteriores | `execute(sql)` | `boolean`: `true` si hay `ResultSet`, `false` si no |

`execute` se usa cuando no sabes de antemano si la sentencia es un `SELECT` o no:

```java
boolean hayResultado = st.execute(sql);
if (hayResultado) {
    try (ResultSet rs = st.getResultSet()) {        // era un SELECT
        while (rs.next()) { System.out.println(rs.getString(1)); }
    }
} else {
    System.out.println("Filas afectadas: " + st.getUpdateCount());   // era INSERT, UPDATE, DELETE...
}
```

> Los ejemplos usan la base de datos `tienda` del Tema 6 y el driver `mysql-connector-j`. Cambia usuario y contraseña por los de tu MySQL.

```java
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CrudCliente {

    private static final String URL      = "jdbc:mysql://localhost:3306/tienda";
    private static final String USUARIO  = "root";
    private static final String PASSWORD = "root";

    public static void main(String[] args) {
        try (Connection con = DriverManager.getConnection(URL, USUARIO, PASSWORD);
             Statement st = con.createStatement()) {

            // CREATE -> INSERT
            int n = st.executeUpdate("INSERT INTO cliente (nombre, direccion, ciudad, email) "
                    + "VALUES ('Ana Ruiz', 'C/ Sol 7', 'Malaga', 'ana.ruiz@mail.com')");
            System.out.println("INSERT: " + n + " fila(s)");

            // READ -> SELECT
            try (ResultSet rs = st.executeQuery(
                    "SELECT id_cliente, nombre, ciudad FROM cliente WHERE email = 'ana.ruiz@mail.com'")) {
                while (rs.next()) {
                    System.out.println(rs.getInt("id_cliente") + " - " + rs.getString("nombre")
                            + " (" + rs.getString("ciudad") + ")");
                }
            }

            // UPDATE
            n = st.executeUpdate("UPDATE cliente SET ciudad = 'Granada' WHERE email = 'ana.ruiz@mail.com'");
            System.out.println("UPDATE: " + n + " fila(s)");

            // DELETE
            n = st.executeUpdate("DELETE FROM cliente WHERE email = 'ana.ruiz@mail.com'");
            System.out.println("DELETE: " + n + " fila(s)");

            

        } catch (SQLException e) {
            System.out.println("Error SQL " + e.getErrorCode() + ": " + e.getMessage());
        }
    }
}
```

Salida (el id puede ser otro en tu base de datos):

```
INSERT: 1 fila(s)
12 - Ana Ruiz (Malaga)
UPDATE: 1 fila(s)
DELETE: 1 fila(s)
```

### Cosas que hay que saber

- **`executeUpdate` devuelve las filas afectadas.** Si un `UPDATE` o un `DELETE` no encuentra ninguna fila, devuelve `0`: **no es un error**, simplemente no coincidió el `WHERE`.
- **Los errores de MySQL llegan como `SQLException`.** Es la misma que veíamos en Workbench: con `e.getErrorCode()` obtienes el número (por ejemplo, `1451` si intentas borrar un cliente que tiene ventas, por la clave foránea) y con `e.getMessage()` el texto.
- **Un `ResultSet` empieza antes de la primera fila.** `rs.next()` avanza a la siguiente y devuelve `false` cuando no quedan más. Los campos se leen por nombre de columna (`rs.getString("nombre")`) o por posición, que **empieza en 1**.
- **Cada `Statement` se cierra solo** gracias al `try (...)`; no hace falta escribir `close()`.

### Cuando las sentencias llevan variables: `PreparedStatement`

Si los datos vienen de variables (por ejemplo, de lo que escribe el usuario), **no se concatenan** dentro del texto SQL: es la forma de que alguien meta comandos maliciosos (*inyección SQL*). Se usan marcadores `?` y se rellenan con `setString`, `setInt`...:

```java
String nombre = "Ana Ruiz";
String sql = "INSERT INTO cliente (nombre, direccion, ciudad, email) VALUES (?, ?, ?, ?)";

try (Connection con = DriverManager.getConnection(URL, USUARIO, PASSWORD);
     PreparedStatement ps = con.prepareStatement(sql)) {

    ps.setString(1, nombre);              // el 1 es el primer ?
    ps.setString(2, "C/ Sol 7");
    ps.setString(3, "Malaga");
    ps.setString(4, "ana.ruiz@mail.com");
    System.out.println("INSERT: " + ps.executeUpdate() + " fila(s)");

} catch (SQLException e) {
    System.out.println("Error SQL " + e.getErrorCode() + ": " + e.getMessage());
}
```

(Hay que importar también `java.sql.PreparedStatement`.) Con `PreparedStatement` se usa `executeUpdate()` y `executeQuery()` **sin** pasar el SQL, porque ya se dio al crearlo.

### Ejercicio propuesto (rápido)

Sobre la tabla `producto` de la tienda, escribe un programa que:
1. Inserte un producto nuevo con tu nombre en el campo `nombre`.
2. Muestre sus datos con un `SELECT`.
3. Le suba el precio un 10 % con un `UPDATE` y muestre las filas afectadas.
4. Lo borre y muestre las filas afectadas.
5. Intente borrar un producto que tenga ventas y muestre el código de error.

> Los ejercicios de la unidad están en `ejercicios_tema4.md`: un ejemplo guiado con consultas y una práctica con un gestor de productos con menú.

---


## Resumen visual del tema

```
TEMA 4 — CONECTORES

  APLICATIVO JAVA ──CONECTOR (driver)──► BDD RELACIONAL
       └── el conector abre la conexión, envía SQL y devuelve resultados

  PROTOCOLOS
   ├── JDBC (Sun/Oracle) → el que usamos siempre
   └── ODBC (Microsoft)  → histórico, vía puente

  ARQUITECTURA
   ├── 2 capas → app + driver, directo a la BDD (la que usamos)
   └── 3 capas → app → middleware → BDD

  TIPOS DE DRIVER
   `mysql-connector-j` - Protocolo nativo (100 % Java, el que usamos)

  EN CÓDIGO
   DriverManager.getConnection(url, user, pass) → devuelve Connection
   try (Connection, Statement, ResultSet) { ... } catch (SQLException)
   (Class.forName ya no hace falta desde JDBC 4.0)

  CRUD CON STATEMENT
   INSERT / UPDATE / DELETE → executeUpdate(sql)  → int (filas afectadas)
   SELECT                   → executeQuery(sql)   → ResultSet (while rs.next())
   Con variables → PreparedStatement con ?  (nunca concatenar)
```


