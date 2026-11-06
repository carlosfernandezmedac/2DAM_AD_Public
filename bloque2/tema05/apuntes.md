# Tema 5 — Manejo de Conectores II

---

## Índice

1. [Introducción](#1-introducción)
2. [Bases de datos en memoria, embebidas e independientes](#2-bases-de-datos-en-memoria-embebidas-e-independientes)
3. [Gestores embebidos: HyperSQL, ObjectDB, Derby/JavaDB, H2](#3-gestores-embebidos-hypersql-objectdb-derbyjavadb-h2)
4. [Comparativa de gestores embebidos](#4-comparativa-de-gestores-embebidos)
5. [H2 + Spring Boot: configuración paso a paso](#5-h2--spring-boot-configuración-paso-a-paso)
6. [Gestores de bases de datos independientes](#6-gestores-de-bases-de-datos-independientes)

---

## 1. Introducción

En el Tema 4 vimos **cómo** conectar Java con una base de datos (el conector). Ahora toca decidir **con qué motor de base de datos** conectamos. No es una decisión trivial: montar un servidor Oracle completo para una app de pruebas con 3 tablas es tan desproporcionado como intentar servir a miles de usuarios concurrentes con una base de datos embebida.

> 💡 **La pregunta que resuelve este tema:** ¿necesito un servidor de base de datos aparte, o me basta con algo que viva dentro de mi propia aplicación?

---

## 2. Bases de datos en memoria, embebidas e independientes

Son tres conceptos que se confunden con facilidad:

```
EN MEMORIA               EMBEBIDA                  INDEPENDIENTE
──────────                ────────                  ─────────────
Toda la info vive         Es PARTE de la            Gestor/SGBD aparte,
en RAM. Volátil:           aplicación. Corre         normalmente en OTRA
se pierde al cerrar        en la misma JVM que       máquina. Cliente/
la aplicación.             la app (mismo proceso).   servidor.
                           Puede guardar en
                           disco o en memoria.
```

- **Base de datos en memoria:** almacena toda la información en RAM. Es volátil — al cerrar la aplicación, se pierde. No confundir con "embebida": una base de datos embebida *puede* funcionar en modo memoria, pero también puede persistir en disco.
- **Base de datos embebida:** es parte de la propia aplicación. Se accede vía JDBC, y el motor corre en la misma JVM que la aplicación mientras esta se ejecuta. Encaja perfectamente cuando se necesita un repositorio simple para persistir datos sin intervención del usuario, sin montar infraestructura aparte. Su punto débil es la dificultad de mantenimiento si el proyecto crece mucho.
- **Base de datos independiente:** aquí sí hay un SGBD dedicado, normalmente en otra máquina, pensado para resolver problemas de mantenimiento, backups, escalado, etc. Es más pesada (no por lenta, sino por la cantidad de procesos adicionales que gestiona) y potente.

---

## 3. Gestores embebidos: HyperSQL, ObjectDB, Derby/JavaDB, H2

| Gestor | Características clave |
|---|---|
| **HyperSQL (HSQLDB)** | Cumple SQL 2011 y JDBC 4. Modo embebido o servidor. Muy estable (lo usan OpenOffice/LibreOffice). Soporta MVCC desde la 2.3.x. |
| **ObjectDB** | No es relacional — es **orientada a objetos**, con soporte JPA2. Al ser compatible con JPA de forma nativa, en teoría se podría prescindir de una capa ORM aparte. |
| **JavaDB / Apache Derby** | Java DB está construida sobre el motor Derby. 100% Java. Soporta modo embebido y cliente/servidor. Se añade con el fichero `derby.jar`. |
| **H2 Database** | Muy rápida, código abierto, API JDBC. Modo cliente/servidor, embebido y en memoria. Ideal para apps web. Muy ligera (`.jar` de ~2MB). Soporta MVCC y driver ODBC de Postgres. |

> 💡 **MVCC** (*Multi-Version Concurrency Control*) es un mecanismo que permite que varias transacciones lean y escriban de forma concurrente sin bloquearse constantemente entre sí, manteniendo varias "versiones" del dato mientras dura cada transacción.

---

## 4. Comparativa de gestores embebidos

| | H2 | Derby | HyperSQL | MySQL | Postgres |
|---|---|---|---|---|---|
| Java puro | Sí | Sí | Sí | No | No |
| Modo memoria | Sí | Sí | Sí | No | No |
| BBDD encriptada | Sí | Sí | Sí | No | No |
| Driver ODBC | Sí | No | No | Sí | Sí |
| Búsqueda texto completo | Sí | No | No | Sí | Sí |
| MVCC | Sí | Sí | Sí | Sí | Sí |
| Espacio (embebido) | ~2 MB | ~3 MB | ~1.5 MB | — | — |
| Espacio (cliente) | ~500 KB | ~600 KB | ~1.5 MB | ~1 MB | ~700 KB |

**H2 destaca** por ser ligera, rápida, compatible con ODBC y con soporte para los tres modos (memoria, embebido, cliente/servidor). Por eso es la opción por defecto que usaremos en las prácticas con Spring Boot.

---

## 5. H2 + Spring Boot: configuración paso a paso

**Spring Boot** es un módulo de Spring que simplifica dos de las partes más tediosas de un proyecto Java con Maven: **elegir dependencias** y **desplegar la aplicación**.

### Paso 1 — Generar el proyecto en start.spring.io

En [https://start.spring.io/](https://start.spring.io/) se configura:

- **Project:** Maven
- **Spring Boot:** la versión estable por defecto
- **Project Metadata:** Group, Artifact, Name, Description, Package name (libres)
- **Packaging:** `.jar`
- **Dependencies:** aquí se añade **H2 Database** (y `Spring Web`, si la app va a ser web)

Al pulsar "Generate" se descarga un `.zip` con el proyecto ya montado.

### Paso 2 — La dependencia H2 en el `pom.xml`

```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
```

### Paso 3 — Configurar `application.properties`

```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=password
```

- `jdbc:h2:mem:testdb` → base de datos **en memoria** (se pierde al parar la app).
- Para que persista en disco, se cambia la URL:

  ```properties
  spring.datasource.url=jdbc:h2:file:~/data/demo;
  ```

  - Para que podamos entrar a la consola web:
  ```properties
  spring.h2.console.enabled=true
  ```

  ```properties
  spring.sql.init.mode=always
  ```

Valores:

- `always` → ejecuta los scripts en **cada arranque**.
- `never` → no los ejecuta nunca.
- `embedded` (por defecto) → solo si la base es en memoria.

### Paso 4 — Inicializar datos con `data.sql`

En `src/main/resources/data.sql`, cualquier SQL que se escriba aquí **se ejecuta automáticamente al arrancar la aplicación** — útil para crear tablas de prueba o resetear datos:

```sql
DROP TABLE IF EXISTS RESERVATION;

CREATE TABLE RESERVATION (
    id INT AUTO_INCREMENT PRIMARY KEY,
    reservation_date TIMESTAMP NULL DEFAULT NULL,
    name VARCHAR(250) NULL,
    status VARCHAR(25) NOT NULL DEFAULT 'FREE'
);
```

### Paso 5 — Consola web de H2

```properties
spring.h2.console.enabled=true
```

Con esto, al arrancar la aplicación se puede acceder a un panel visual de la base de datos en `http://localhost:8080/h2-console`, introduciendo el Driver Class (`org.h2.Driver`), la JDBC URL, usuario y contraseña configurados.

> ⚠️ Como recomienda el propio libro, para una aplicación real conviene montar una **capa DAO** de acceso a datos en vez de escribir SQL suelto por todas partes — pero para practicar y hacer pruebas rápidas, `data.sql` + la consola H2 son más que suficientes.

---

## 6. Gestores de bases de datos independientes

A diferencia de las embebidas, una base de datos independiente **no corre en la misma JVM** que la aplicación. Consume más recursos, pero precisamente por estar aislada en su propia máquina puede aprovecharlos mejor y ofrecer todo su potencial (procedimientos almacenados, replicación, backups avanzados...).

Las más conocidas a nivel comercial:

- **SQL Server** (Microsoft)
- **Oracle**
- **PostgreSQL**
- **MySQL**

---

## Resumen visual del tema

```
TEMA 5 — ELEGIR EL MOTOR DE BASE DE DATOS

  ¿En memoria, embebida o independiente?
   ├── En memoria     → volátil, se pierde al cerrar la app
   ├── Embebida       → vive en la misma JVM que la app (H2, Derby, HyperSQL...)
   └── Independiente  → SGBD aparte, normalmente otra máquina (Oracle, MySQL...)

  ¿Cuándo cada una?
   Pocas tablas, poca carga, sin admin dedicado  → EMBEBIDA (ej. H2)
   Mucha carga, muchas consultas, necesita admin → INDEPENDIENTE

  H2 + SPRING BOOT
   1. start.spring.io → añadir dependencia H2
   2. application.properties → spring.datasource.url (mem: o file:)
   3. data.sql → SQL que se ejecuta al arrancar
   4. spring.h2.console.enabled=true → panel visual en /h2-console
```

---

## Recursos

- 📖 Webgrafía: [https://start.spring.io/](https://start.spring.io/)
