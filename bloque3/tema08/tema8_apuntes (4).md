# Tema 8 · Exploración del mapeo objeto-relacional

## 1. Introducción

En el Tema 7 vimos qué es un ORM, por qué usamos Hibernate y cómo configurarlo. En este tema entramos en el día a día del framework: cómo definir una **clase persistente**, cómo mapearla con anotaciones JPA (incluidas las relaciones entre tablas), cómo funciona el objeto **`Session`** y sus estados, cómo cargar/guardar/modificar objetos, y cómo consultar con **HQL**.

## 2. Clases persistentes

Una **clase persistente** es una clase Java cuyos objetos se pueden almacenar en base de datos. Para que Hibernate la trate con eficiencia, debería seguir el modelo **POJO** (*Plain Old Java Object*) y estas reglas (no son requisitos estrictos, pero sí buenas prácticas):

- Constructor **por defecto** (sin argumentos).
- Un atributo **`id`**, que se mapeará como clave primaria.
- Atributos **privados**, con sus correspondientes `get()`/`set()`.
- La clase **no debería ser `final`** (Hibernate genera proxies dinámicos por reflexión, y una clase `final` no se puede extender).

```java
   @Entity
   @Table(name = "PRODUCTO")
   public class Producto {

       @Id
       @GeneratedValue(strategy = GenerationType.IDENTITY)
       private int id;

       @Column(nullable = false)
       private String nombre;

       private double precio;

       private int stock;

       public Producto() {}

       public Producto(String nombre, double precio, int stock) {
           this.nombre = nombre;
           this.precio = precio;
           this.stock = stock;
       }

       // getters y setters
   }
```

Es la clase `Producto` que creamos en el Tema 7 (Caso 1).

## 3. Mapeo con anotaciones JPA

Para que la clase `Producto` sea una **entidad** (se guarde en una tabla) se anota directamente sobre la clase, sin ficheros aparte. Es lo que hicimos en el Tema 7 (Caso 1). La tabla que Hibernate crea es equivalente a:

```sql
CREATE TABLE PRODUCTO (
  id INT NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(255) NOT NULL,
  precio DOUBLE,
  stock INT,
  PRIMARY KEY (id)
);
```

| Anotación | Qué hace |
|---|---|
| `@Entity` | La clase es una entidad: se corresponde con una tabla |
| `@Table(name=...)` | Nombre de la tabla (si no se pone, se usa el de la clase) |
| `@Id` | Atributo que es la clave primaria |
| `@GeneratedValue(strategy = IDENTITY)` | La PK la genera la base de datos (`AUTO_INCREMENT`) |
| `@Column(name=..., nullable=false)` | Nombre de la columna y restricciones (si no se pone, se usa el nombre del atributo) |

Con `ddl-auto=update` o `create`, Hibernate crea la tabla a partir de estas anotaciones. Aquí se añade lo que falta: **las relaciones entre tablas**.

### 3.1 Relaciones entre entidades

En SQL una relación se expresa con una **clave ajena (FK)**. En Java se expresa con un **atributo que es el objeto relacionado** (no un `int` con el id).

| Relación | Ejemplo | En SQL | Anotación JPA |
|---|---|---|---|
| **1:N** | Una categoría tiene muchos productos | FK en la tabla del lado N (`producto`) | `@ManyToOne` en la clase del lado N |
| **1:1** | Un producto tiene una ficha técnica | FK con `UNIQUE` | `@OneToOne` |
| **N:M** | Un cliente compra varios productos y un producto lo compran varios clientes | Tabla intermedia con dos FK | `@ManyToMany` + `@JoinTable` |

**1:N — `@ManyToOne`.** La FK (`id_categoria`) está en la tabla `producto`, así que la anotación va en `Producto`:

```java
@Entity
@Table(name = "PRODUCTO")
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String nombre;
    private double precio;
    private int stock;

    @ManyToOne                          // muchos productos -> una categoría
    @JoinColumn(name = "id_categoria")  // columna de la clave ajena
    private Categoria categoria;        // el OBJETO, no un int
}
```

La clase del lado 1, `Categoria`, es una entidad normal, sin anotaciones de relación:

```java
@Entity
@Table(name = "CATEGORIA")
public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String nombre;
}
```

```sql
-- lo que crea Hibernate
create table categoria (id integer not null auto_increment, nombre varchar(255), primary key (id))
create table producto (..., id_categoria integer, primary key (id))
alter table producto add constraint ... foreign key (id_categoria) references categoria (id)
```

> 💡 **Regla:** el `@ManyToOne` va siempre en la clase que tiene la FK en su tabla (el lado N).

**1:1 — `@OneToOne`** (en `Producto`, hacia una entidad `FichaTecnica`):

```java
@OneToOne(cascade = CascadeType.ALL)    // al guardar el producto se guarda su ficha
@JoinColumn(name = "id_ficha")          // FK UNIQUE en la tabla producto
private FichaTecnica ficha;
```

**N:M — `@ManyToMany` + `@JoinTable`** (en `Cliente`, hacia `Producto`). Hibernate crea la tabla intermedia `compra`, con una FK a cada lado. La anotación se pone en una sola de las dos clases (aquí `Cliente`, porque "un cliente compra productos" se lee de forma natural); también funcionaría al revés:

```java
@Entity
@Table(name = "CLIENTE")
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String nombre;

    @ManyToMany
    @JoinTable(name = "compra",                                     // tabla intermedia
               joinColumns = @JoinColumn(name = "id_cliente"),      // FK hacia Cliente
               inverseJoinColumns = @JoinColumn(name = "id_producto")) // FK hacia Producto
    private Set<Producto> productos = new HashSet<>();
}
```

```sql
-- tabla que crea Hibernate
create table compra (id_cliente integer not null, id_producto integer not null,
                     primary key (id_cliente, id_producto))
```

En el **Caso 5** se crean estas entidades (`Categoria`, `Producto` y `Cliente`) y se comprueban las tablas y las claves ajenas en MySQL.

## 4. Sesiones y objetos Hibernate — estados

Hibernate usa el objeto **`Session`** para interactuar con la base de datos. Una instancia de una clase persistente puede estar en uno de estos tres estados:

| Estado | Descripción |
|---|---|
| **Transient** | Objeto recién creado con `new`. No está asociado a ninguna `Session` ni tiene representación en base de datos. |
| **Persistent** | El objeto se asocia a una `Session` (p. ej. tras un `save()`/`persist()`, o al recuperarlo con `get()`). Tiene identificador y fila asociada en BBDD; Hibernate sincroniza sus cambios. |
| **Detached** | La `Session` que lo gestionaba se ha cerrado. El objeto sigue teniendo los datos, pero ya no está sincronizado automáticamente con la base de datos. |

```
new Producto()  →  [TRANSIENT]
     │  session.save(p)
     ▼
 [PERSISTENT]  ←── vive mientras la Session está abierta
     │  session.close()
     ▼
 [DETACHED]
```

El objeto `Session` **no debe mantenerse abierto mucho tiempo** (es ligero, pensado para crearse y destruirse en cada interacción). Patrón típico de uso con `Transaction`:

```java
SessionFactory sessionFactory = ...; // obtenido de Spring o de Configuration().buildSessionFactory()
Session session = sessionFactory.openSession();
Transaction transaction = null;

try {
    transaction = session.beginTransaction();
    // ... operaciones de persistencia ...
    transaction.commit();
} catch (Exception e) {
    if (transaction != null) transaction.rollback();
    e.printStackTrace();
} finally {
    session.close();
}
```

## 5. Métodos principales de `Session`

| Método | Descripción |
|---|---|
| `beginTransaction()` | Inicia una unidad de trabajo y devuelve el `Transaction` asociado. |
| `close()` | Cierra la sesión, liberando la conexión JDBC. |
| `clear()` | Vacía por completo la caché de la sesión. |
| `createQuery(String hql)` | Crea una consulta HQL. |
| `createSQLQuery(String sql)` | Crea una consulta SQL nativa. |
| `createCriteria(Class c)` | Crea una instancia `Criteria` para consultas 100% orientadas a objetos. |
| `get(Class, id)` | Recupera un objeto persistente; devuelve `null` si no existe. |
| `load(Class, id)` | Recupera un objeto persistente; lanza excepción si no existe. |
| `getIdentifier(Object)` | Devuelve el identificador de una entidad asociada a la sesión. |
| `isOpen()` / `isConnected()` | Comprueban el estado de la sesión. |

> ⚠️ **Nota sobre versiones.** Esta tabla (y el libro) corresponde a Hibernate 5. En **Hibernate 6**, que es el que trae Spring Boot 3, `createSQLQuery` se sustituye por `createNativeQuery` y `createCriteria` ya no existe (se usa la Criteria API de JPA). Además, `save()`, `update()`, `saveOrUpdate()` y `delete()` están en desuso en favor de los métodos estándar de JPA: `persist()`, `merge()` y `remove()`. Como concepto siguen valiendo, y es lo que encontrarás en código heredado, pero en proyectos nuevos se usan los repositorios de Spring Data JPA (Caso 3 y Caso 4).

## 6. Carga, almacenamiento y modificación de objetos

**Carga:**
```java
Producto p = session.get(Producto.class, 1);
```

**Guardar (`persist` vs. `save`):**
```java
session.persist(new Producto("Teclado", 25.90, 40)); // void, no devuelve nada
Serializable id = session.save(new Producto("Ratón", 12.50, 60)); // devuelve el id generado
```

**Actualizar (`update` vs. `merge`):**
```java
p.setPrecio(19.90);
session.update(p);  // necesita que no haya ya otra instancia de la misma fila en sesión, o lanza excepción
session.merge(p);   // hace lo mismo, pero gestiona ese conflicto internamente
```

**Eliminar:**
```java
session.delete(p);
```

**Guardar o actualizar automáticamente:**
```java
session.saveOrUpdate(p); // insert si no existe en BBDD, update si ya existe
```

## 7. Consultas: HQL y SQL nativo

**HQL** (*Hibernate Query Language*) es un lenguaje orientado a objetos muy parecido a SQL, pero en vez de operar sobre tablas/columnas, opera sobre **entidades y sus propiedades**. Hibernate traduce internamente cada consulta HQL a SQL antes de ejecutarla.

```java
String hql = "FROM Producto";
Query consulta = session.createQuery(hql);
consulta.setFirstResult(0);
consulta.setMaxResults(40);
List<Producto> results = consulta.list();
```

Se recomienda HQL frente a SQL nativo porque es independiente del motor de base de datos concreto (portabilidad) y aprovecha la caché de Hibernate.

**Update con parámetros con nombre:**
```java
Query q = session.createQuery(
    "update Producto set stock = :num where id = :id and nombre = :name");
q.setParameter("num", 25);
q.setParameter("id", 105);
q.setParameter("name", "Teclado");

int status = q.executeUpdate();
transaction.commit();
```

**SQL nativo** (cuando necesitamos algo específico del motor, como un procedimiento almacenado):
```java
Query q = session.createNativeQuery("SELECT * FROM PRODUCTO WHERE stock > :num"); // en Hibernate 5: createSQLQuery
q.setParameter("num", 100);
```

## 8. Gestión de transacciones

Una **transacción** es una unidad de trabajo atómica: si un paso falla, se revierte todo el bloque. Se rige por las propiedades **ACID** (Atomicidad, Consistencia, Aislamiento, Durabilidad), ya vistas en el Bloque 2 con JDBC.

| Método de `Transaction` | Descripción |
|---|---|
| `begin()` | Inicia la transacción. |
| `commit()` | Confirma y persiste todos los cambios del bloque. |
| `rollback()` | Cancela y revierte todos los cambios del bloque. |
| `isActive()` | Comprueba si la transacción sigue en curso. |
| `setTimeout(int segundos)` | Define un tiempo máximo de ejecución. |
| `wasCommitted()` / `wasRolledBack()` | Comprueban cómo terminó la transacción. |

## 9. Resumen

| Concepto | Idea clave |
|---|---|
| **Clase persistente** | POJO con `id`, constructor vacío y getters/setters |
| **Anotaciones (`@Entity`, `@Id`...)** | Mapeo actual, integrado en la clase |
| **Relaciones** | `@ManyToOne` (1:N, en el lado de la FK), `@OneToOne`, `@ManyToMany` + `@JoinTable` |
| **`Session`** | Conexión ligera; estados transient / persistent / detached |
| **`Transaction`** | Unidad de trabajo atómica (ACID) |
| **HQL** | Consultas orientadas a objetos, portables entre motores de BBDD |

## 🔗 Webgrafía

- <https://hibernate.org/>
- <https://docs.spring.io/spring-boot/docs/current/reference/html/data.html>
