# Tema 8 · Exploración del mapeo objeto-relacional

## 1. Introducción

En el Tema 7 vimos qué es un ORM, por qué usamos Hibernate y cómo configurarlo. En este tema entramos en el día a día del framework: cómo definir una **clase persistente**, cómo mapearla con anotaciones JPA (incluidas las relaciones entre tablas), los **estados** de un objeto, cómo guardar, consultar, modificar y borrar con **repositorios** de Spring Data JPA, cómo hacer consultas propias (**JPQL**) y cómo funcionan las **transacciones**.

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

En los **Casos 1 y 2** se practica la relación 1:N (`Categoria` – `Producto`): se crean las tablas, se hace un CRUD y se comprueban las tablas y la clave ajena en MySQL.

## 4. Estados de un objeto

Hibernate sigue la pista de los objetos que guarda. Un objeto de una clase persistente puede estar en uno de estos tres estados:

| Estado | Descripción |
|---|---|
| **Transient** | Objeto recién creado con `new`. Todavía no se ha guardado: no tiene fila en la base de datos. |
| **Persistent** | El objeto está gestionado por Hibernate (tras `save()` o al leerlo, **dentro de una transacción**). Tiene `id` y fila en la BBDD, y Hibernate detecta sus cambios. |
| **Detached** | La transacción ya terminó. El objeto conserva sus datos y su `id`, pero Hibernate ya no vigila sus cambios. |

```
new Producto(...)  →  [TRANSIENT]
        │  repositorio.save(p)
        ▼
   [PERSISTENT]   ←── mientras dura la transacción
        │  termina la transacción
        ▼
    [DETACHED]
```

> 💡 **Qué implica en la práctica:** si cambias un atributo de un objeto *detached* (por ejemplo, `p.setPrecio(14.90)` en el `main`), la base de datos **no** se entera hasta que llames de nuevo a `save(p)`. Como `p` ya tiene `id`, `save()` hace un `UPDATE` en lugar de un `INSERT`.

## 5. Repositorios: guardar, consultar, modificar y borrar

Con Spring Data JPA no se escribe código de acceso a datos. Se crea una **interfaz vacía** por entidad y Spring genera la implementación:

```java
public interface ProductoRepository extends JpaRepository<Producto, Integer> { }
//                                                         entidad ^   ^ tipo del id
```

Con eso ya se dispone de las operaciones básicas (CRUD):

| Método | Qué hace | SQL equivalente |
|---|---|---|
| `save(p)` | Inserta si el objeto es nuevo; modifica si ya tiene `id` | `INSERT` / `UPDATE` |
| `findAll()` | Devuelve todos los objetos | `SELECT * FROM producto` |
| `findById(id)` | Busca por clave; devuelve un `Optional` (puede estar vacío) | `SELECT ... WHERE id = ?` |
| `existsById(id)` | `true` si existe | `SELECT COUNT(*) ...` |
| `count()` | Número de filas | `SELECT COUNT(*)` |
| `deleteById(id)` | Borra por clave | `DELETE ... WHERE id = ?` |
| `delete(p)` | Borra un objeto | `DELETE` |

**Consultas por el nombre del método.** Spring entiende el nombre y construye la consulta:

```java
public interface ProductoRepository extends JpaRepository<Producto, Integer> {
    List<Producto> findByNombre(String nombre);                  // WHERE nombre = ?
    List<Producto> findByPrecioGreaterThan(double precio);       // WHERE precio > ?
    List<Producto> findByNombreContaining(String texto);         // WHERE nombre LIKE %texto%
    List<Producto> findByCategoriaId(int idCategoria);           // WHERE id_categoria = ?
}
```

## 6. Consultas propias: JPQL

Cuando el nombre del método no basta, se escribe la consulta con `@Query`. **JPQL** (la versión estándar de **HQL**) se parece a SQL, pero opera sobre **clases y atributos**, no sobre tablas y columnas: se escribe `Producto` y `p.precio`, no `producto` ni la columna.

```java
public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    // Consulta JPQL con un parámetro con nombre
    @Query("SELECT p FROM Producto p WHERE p.precio > :min ORDER BY p.precio")
    List<Producto> caros(@Param("min") double min);

    // Modificación con JPQL (necesita @Modifying y @Transactional)
    @Modifying
    @Transactional
    @Query("UPDATE Producto p SET p.stock = :stock WHERE p.id = :id")
    int cambiarStock(@Param("id") int id, @Param("stock") int stock);

    // SQL nativo (cuando hace falta algo propio del motor): se escribe con nombres de tabla y columna
    @Query(value = "SELECT * FROM producto WHERE stock > :min", nativeQuery = true)
    List<Producto> conStock(@Param("min") int min);
}
```

Se recomienda JPQL frente a SQL nativo porque es independiente del motor de base de datos (portabilidad).

## 7. Transacciones

Una **transacción** es una unidad de trabajo atómica: si un paso falla, se revierte todo el bloque. Se rige por las propiedades **ACID** (Atomicidad, Consistencia, Aislamiento, Durabilidad), ya vistas en el Bloque 2 con JDBC.

- Cada método de un repositorio (`save`, `deleteById`...) se ejecuta **en su propia transacción**: se confirma (*commit*) al terminar bien y se deshace (*rollback*) si hay una excepción.
- Para agrupar varias operaciones en **una sola transacción** se anota el método con `@Transactional`:

```java
@Transactional
public void pasarProductos(Categoria origen, Categoria destino) {
    // Si algo falla a mitad, no queda nada a medias: se hace rollback de todo
    ...
}
```

- Dentro de una transacción, los objetos están en estado *persistent*, así que un `p.setPrecio(...)` se guarda solo al terminar, sin llamar a `save()` (*dirty checking*).

## 8. Resumen

| Concepto | Idea clave |
|---|---|
| **Clase persistente** | POJO con `id`, constructor vacío y getters/setters |
| **Anotaciones (`@Entity`, `@Id`...)** | Mapeo integrado en la clase: cada clase es una tabla |
| **Relaciones** | `@ManyToOne` (1:N, en el lado de la FK), `@OneToOne`, `@ManyToMany` + `@JoinTable` |
| **Estados** | Transient → Persistent → Detached |
| **Repositorio** | Interfaz vacía que da `save`, `findAll`, `findById`, `deleteById`... |
| **Consultas** | Por el nombre del método (`findBy...`) o con `@Query` (JPQL) |
| **Transacción** | Unidad de trabajo atómica (ACID); `@Transactional` |

## 🔗 Webgrafía

- <https://hibernate.org/>
- <https://docs.spring.io/spring-data/jpa/reference/>
- <https://docs.spring.io/spring-boot/docs/current/reference/html/data.html>
