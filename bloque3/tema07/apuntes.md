# Tema 7 · El mapeo objeto-relacional

## 1. Introducción

Java trabaja con **objetos**; una base de datos relacional trabaja con **tablas, filas y columnas**. Cuando queremos guardar un objeto en base de datos aparece un problema de fondo, conocido como el *desajuste de impedancia objeto-relacional* (object-relational impedance mismatch): los dos mundos no encajan de forma natural (herencia, colecciones, identidad de objeto vs. clave primaria, etc.).

Un **ORM** (*Object Relational Mapping*) es un framework que resuelve ese desajuste: traduce automáticamente objetos Java a filas de una tabla, y filas de una tabla a objetos Java.

```
Aplicación Java → Objeto → ORM → Base de datos
```

Un objeto "plano" (POJO) equivale, a grandes rasgos, a una fila; sus atributos equivalen a columnas. El ORM es el que hace ese "espejo" en ambos sentidos.

## 2. Ventajas e inconvenientes del ORM

| ✅ Ventajas | ⚠️ Inconvenientes |
|---|---|
| Mejora la eficiencia del desarrollo | El mapeado automático consume recursos de sistema |
| Desarrollo más orientado a objetos (abstracción, herencia, composición, identidad) | La sintaxis del ORM puede complicarse en consultas muy avanzadas (varios cruces de tablas, condiciones complejas) |
| Mayor manejabilidad de los objetos del aplicativo | Curva de aprendizaje inicial del framework |
| Facilidad para incorporar funciones extra (caché, por ejemplo) | Logs y trazas más extensos de depurar |

Con JDBC "a pelo" (Bloque 2) escribíamos SQL nativo y procesábamos el `ResultSet` a mano: el desarrollador tenía que conocer a fondo la base de datos (tablas, columnas, *constraints*...). Con un ORM, el punto de vista cambia: trabajamos contra objetos Java y es el framework quien traduce eso a SQL.

## 3. Fases del mapeo objeto-relacional

La arquitectura funcional de un ORM se puede resumir en tres fases:

- **Fase 1 — Objetos.** Los POJOs (*Plain Old Java Object*), las clases de implementación, la capa de servicio y las clases DAO (`crearObjeto()`, `encontrarObjeto()`, `borrarObjeto()`...).
- **Fase 2 — Persistencia / mapeo.** Aquí vive el propio ORM: el proveedor JPA, el archivo de asignación (mapeo clase ↔ tabla), el cargador (una especie de caché) y la "reja de objetos" (*object grid*), un espacio temporal por el que pasan las consultas antes de llegar a la base de datos.
- **Fase 3 — Datos relacionales.** La base de datos en sí, donde finalmente se persiste la información.

```
FASE 1                FASE 2                    FASE 3
Capa servicio          Object grid                BBDD
POJO / Entidades       JPA loader
DAO                    Archivo asignación
                        JPA provider
```

## 4. Herramientas ORM en Java

| ORM | Notas |
|---|---|
| **EBean** | Soporta múltiples BBDD (H2, MySQL, Postgres, Oracle...). Mezcla consultas ORM con SQL y DTO. Evita automáticamente el problema N+1, caché L2/L3. |
| **MyBatis** (antes iBatis, Apache) | Separa capa de abstracción, capa de framework y capa de driver. Da mucho control manual sobre el SQL. |
| **Hibernate** | El más extendido en Java (y su versión .NET, NHibernate). Mapea las clases mediante ficheros `.hbm.xml` **o mediante anotaciones**. Ofrece su propio lenguaje de consultas, **HQL**. |

Nos centramos en **Hibernate**, por ser el estándar de facto y la base de **JPA** (*Jakarta Persistence API*, la especificación que Hibernate implementa).

> 📌 **Nota histórica — `.hbm.xml` vs. anotaciones.** Hibernate nació apoyándose en ficheros XML de mapeo (`.hbm.xml`), y así es como lo vas a encontrar descrito en buena parte de la documentación clásica y en proyectos antiguos. Hoy en día, en cualquier proyecto nuevo (y especialmente con Spring Boot), **el mapeo se hace con anotaciones JPA directamente sobre la clase** (`@Entity`, `@Id`, `@Column`...), sin necesidad de un fichero XML aparte. En este bloque veremos las dos formas para que sepas reconocer código heredado en `.hbm.xml`, pero **la que usaremos en la práctica y en los ejercicios es la de anotaciones**.

## 5. Arquitectura de Hibernate

La arquitectura de Hibernate gira en torno a los **objetos de persistencia, las sesiones y las transacciones**. Hibernate usa **reflexión** de Java para analizar y modificar en tiempo de ejecución los atributos de las clases mapeadas.

```
APLICACIÓN
    │
    ▼
OBJETOS DE PERSISTENCIA  ◄──  application.properties / hibernate.cfg.xml
    │
    ▼
   HIBERNATE
    │
    ▼
BASE DE DATOS
```

La capa **Aplicación** y la capa **Hibernate** están unidas por los objetos de persistencia (las entidades). Es Hibernate quien gestiona la conexión con el driver JDBC, carga la configuración y usa las entidades diseñadas para traducir Java ↔ SQL.

### Componentes principales

| Componente | Función |
|---|---|
| **SessionFactory** | Objeto "caro" de crear, pesado, pensado para vivir una vez por base de datos y aplicación. Fabrica objetos `Session`. |
| **Session** | Conexión ligera y de corta duración con la base de datos. No debe mantenerse abierta mucho tiempo. |
| **Transaction** | Objeto opcional que gestiona operaciones atómicas (`commit` / `rollback`). |
| **Query** | Consultas HQL o SQL nativas, con parámetros, límites de resultados, etc. |
| **Criteria** | API de consultas 100% orientada a objetos Java (sin escribir SQL/HQL a mano), traducida internamente a SQL. |

## 6. Instalación de Hibernate (con Spring Boot)

Igual que en el Bloque 2 partíamos de [start.spring.io](https://start.spring.io/) para generar el arquetipo del proyecto, aquí hacemos lo mismo añadiendo las dependencias de persistencia:

- **Project:** Maven · **Language:** Java · **Java:** 17+
- Dependencias:
  - `Spring Web`
  - `Spring Data JPA` (incluye Hibernate como implementación de JPA)
  - `MySQL Driver`

Esto añade a `pom.xml`:

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
</dependencies>
```

Si no usáramos Spring Boot, la dependencia se agregaría directamente desde el repositorio de Maven ([mvnrepository.com](https://mvnrepository.com/)), buscando la última versión `Final`:

```xml
<dependency>
    <groupId>org.hibernate</groupId>
    <artifactId>hibernate-core</artifactId>
    <version>5.4.18.Final</version>
</dependency>
```

## 7. Configuración de Hibernate: dos vías

### ✅ Opción 1 — Spring Boot + `application.properties` (la que usaremos)

📁 `src/main/resources/application.properties`

```properties
# Conexión a MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/empresa_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=tu_contraseña
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Dialecto y creación automática del esquema
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

# Trazas SQL
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Logs de Hibernate
logging.level.org.hibernate.SQL=debug
logging.level.org.hibernate.type.descriptor.sql=trace
```

- `spring.jpa.hibernate.ddl-auto` acepta `none`, `validate`, `update`, `create`, `create-drop` — controla si Hibernate crea/actualiza las tablas a partir de las entidades.
- Spring Boot detecta automáticamente las clases anotadas con `@Entity`: **no hace falta ningún fichero de mapeo XML ni instanciar `SessionFactory` a mano**.
- La clase principal lleva `@SpringBootApplication`:

```java
@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
```

Con esta opción, una entidad se define así, con **anotaciones JPA** en lugar de fichero XML:

```java
import jakarta.persistence.*;

@Entity
@Table(name = "CUSTOMER")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "customer_number")
    private int customerNumber;

    public Customer() {}

    public Customer(String firstName, String lastName, int customerNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.customerNumber = customerNumber;
    }

    // Getters y setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public int getCustomerNumber() { return customerNumber; }
    public void setCustomerNumber(int customerNumber) { this.customerNumber = customerNumber; }
}
```

| Anotación | Equivale a (en `.hbm.xml`) |
|---|---|
| `@Entity` | `<class>` |
| `@Table(name=...)` | atributo `table` de `<class>` |
| `@Id` | `<id>` |
| `@GeneratedValue` | `<generator>` |
| `@Column(name=...)` | `<property column=...>` |

### 🗄️ Opción 2 — Hibernate "clásico" sin Spring Boot (`.hbm.xml`)

Si no dispones de Spring Boot (por ejemplo, un proyecto Java a pelo o heredado), Hibernate se configura con un fichero `hibernate.cfg.xml`:

📁 `src/main/resources/hibernate.cfg.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<hibernate-configuration>
    <session-factory>

        <property name="hibernate.connection.driver_class">com.mysql.cj.jdbc.Driver</property>
        <property name="hibernate.connection.url">jdbc:mysql://localhost:3306/empresa_db</property>
        <property name="hibernate.connection.username">root</property>
        <property name="hibernate.connection.password">tu_contraseña</property>

        <property name="hibernate.dialect">org.hibernate.dialect.MySQL8Dialect</property>
        <property name="hibernate.hbm2ddl.auto">update</property>

        <property name="hibernate.show_sql">true</property>
        <property name="hibernate.format_sql">true</property>

        <!-- Ficheros de mapeo XML -->
        <mapping resource="Customer.hbm.xml"/>

    </session-factory>
</hibernate-configuration>
```

Y aquí **tú** instancias Hibernate manualmente (no hay `@SpringBootApplication`):

```java
public class Main {
    public static void main(String[] args) {
        SessionFactory sessionFactory =
                new Configuration().configure().buildSessionFactory();

        Session session = sessionFactory.openSession();
        session.beginTransaction();

        // ... operaciones con session ...

        session.getTransaction().commit();
        session.close();
        sessionFactory.close();
    }
}
```

## 8. Resumen

| Concepto | Idea clave |
|---|---|
| **ORM** | Traduce objetos Java ↔ filas de base de datos, evitando SQL manual |
| **Hibernate** | ORM más usado en Java; implementación de referencia de JPA |
| **`.hbm.xml`** | Forma clásica de mapeo (fichero XML aparte); hoy es legado |
| **Anotaciones (`@Entity`...)** | Forma moderna y estándar de mapeo, integrada en la propia clase |
| **`SessionFactory` / `Session`** | Fábrica de conexiones / conexión ligera con la BBDD |
| **Spring Boot + Spring Data JPA** | Forma habitual de trabajar con Hibernate hoy en día, sin gestión manual de `SessionFactory` |

## 🔗 Webgrafía

- <https://hibernate.org/>
- <https://start.spring.io/>
- <https://mvnrepository.com/>
- <https://docs.spring.io/spring-boot/docs/current/reference/html/data.html>
- <https://dev.mysql.com/doc/connector-j/en/>
