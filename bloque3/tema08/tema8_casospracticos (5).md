# Tema 8 · Casos prácticos

Los dos casos se hacen sobre el proyecto `tienda-hibernate` del Tema 7 (entidad `Producto` anotada, MySQL, `tienda_db`). El **Caso 1** crea las clases y las tablas; el **Caso 2** hace el CRUD con ellas.

---

## Caso 1 — Crear las clases y las tablas: `categoria`, `producto` y `cliente`

**Planteamiento.** Queremos tener en la base de datos tres tablas: **categoria**, **producto** y **cliente**, y que cada producto pertenezca a **una categoría**. Todo con Java, sin escribir SQL: es Hibernate quien crea las tablas.

**Nudo.** Cada tabla es una clase con `@Entity`. La relación 1:N (una categoría, muchos productos) se expresa en `Producto` con un atributo `Categoria` y `@ManyToOne`; Hibernate lo convierte en la clave ajena `id_categoria`.

```
 CATEGORIA (1) ────< PRODUCTO (N)          CLIENTE
 id, nombre          id, nombre,           id, nombre
                     precio, stock,
                     id_categoria (FK)
```

**Desenlace.** Todo se hace en el **mismo paquete** donde ya está `Producto`:

| Fichero | Qué hay que hacer | Paso |
|---|---|---|
| `Categoria.java` | Clase nueva | 1 |
| `Producto.java` | Ya existe: se le añade la categoría | 2 |
| `Cliente.java` | Clase nueva | 3 |
| `application.properties` | Ya existe: se cambia una línea | 4 |

### Paso 1 — `Categoria.java` (nuevo)

```java
import jakarta.persistence.*;

@Entity
@Table(name = "CATEGORIA")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String nombre;

    public Categoria() {}

    public Categoria(String nombre) {
        this.nombre = nombre;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}
```

### Paso 2 — `Producto.java` (modificar)

Se añaden **tres cosas**, marcadas con `NUEVO`: el atributo `categoria`, un constructor que la recibe y su get/set.

```java
import jakarta.persistence.*;

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

    // NUEVO: la clave ajena. Muchos productos -> una categoría
    @ManyToOne
    @JoinColumn(name = "id_categoria")
    private Categoria categoria;            // el OBJETO, no un int

    public Producto() {}

    public Producto(String nombre, double precio, int stock) {
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
    }

    // NUEVO: constructor que recibe también la categoría
    public Producto(String nombre, double precio, int stock, Categoria categoria) {
        this(nombre, precio, stock);
        this.categoria = categoria;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    // NUEVO
    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }
}
```

> 💡 **Regla:** el `@ManyToOne` va siempre en la clase que tiene la clave ajena en su tabla (el lado N).

### Paso 3 — `Cliente.java` (nuevo)

```java
import jakarta.persistence.*;

@Entity
@Table(name = "CLIENTE")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String nombre;

    public Cliente() {}

    public Cliente(String nombre) {
        this.nombre = nombre;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}
```

### Paso 4 — `application.properties` y arrancar

Mientras se practica, que Hibernate **borre y cree las tablas en cada arranque**:

```properties
spring.jpa.hibernate.ddl-auto=create
spring.jpa.show-sql=true
```

Arranca la aplicación. En la consola aparece algo equivalente a este SQL, que genera Hibernate:

```sql
create table categoria (id integer not null auto_increment, nombre varchar(255), primary key (id))
create table cliente (id integer not null auto_increment, nombre varchar(255), primary key (id))
create table producto (id integer not null auto_increment, nombre varchar(255) not null,
                       precio float(53) not null, stock integer not null,
                       id_categoria integer, primary key (id))
alter table producto add constraint ... foreign key (id_categoria) references categoria (id)
```

### Paso 5 — Comprobar en MySQL

```sql
USE tienda_db;
SHOW TABLES;                      -- categoria, cliente, producto
DESCRIBE producto;                -- la columna id_categoria aparece con índice MUL
SHOW CREATE TABLE producto;       -- aparece CONSTRAINT ... FOREIGN KEY (id_categoria)
```

Y comprueba que la clave ajena funciona. Inserta a mano una categoría y un producto bien relacionado, y otro con una categoría que no existe:

```sql
INSERT INTO categoria (nombre) VALUES ('Periféricos');
INSERT INTO producto (nombre, precio, stock, id_categoria) VALUES ('Teclado', 25.90, 40, 1);   -- OK
INSERT INTO producto (nombre, precio, stock, id_categoria) VALUES ('Fantasma', 1, 1, 99);      -- ERROR
```

El último da un error de clave ajena (*Cannot add or update a child row: a foreign key constraint fails*): la categoría 99 no existe. Quien lo impide es **MySQL**, no Java ni Hibernate.

| Anotación | Qué hace |
|---|---|
| `@Entity` / `@Table` | La clase es una tabla |
| `@Id` / `@GeneratedValue` | Clave primaria autoincremental |
| `@ManyToOne` | Muchos de esta clase se relacionan con uno de la otra (lado N) |
| `@JoinColumn(name=...)` | Nombre de la columna de la clave ajena |

> ⚠️ Si el servidor es MySQL en Linux, los nombres de tabla distinguen mayúsculas; Spring Boot los pasa a minúsculas (`producto`, `categoria`), por eso las consultas SQL van en minúsculas.

**Para pensar:**
1. ¿En qué clase va el `@ManyToOne` y por qué?
2. En la tabla `producto`, ¿qué columna es la clave ajena y de qué atributo Java sale?
3. ¿Por qué `Producto` tiene un atributo `Categoria categoria` y no un `int idCategoria`?
4. ¿Qué hace `ddl-auto=create`? ¿Qué pasaría con los datos si lo dejas así al volver a arrancar?

---

## Caso 2 — CRUD: insertar, consultar, modificar y borrar

**Planteamiento.** Con las tres tablas del Caso 1 creadas, queremos **guardar, leer, modificar y borrar** datos desde Java.

**Nudo.** Para cada tabla se crea un **repositorio**: una interfaz **vacía** que extiende `JpaRepository`. Spring genera el código de `save`, `findAll`, `findById`, `deleteById`... Sin repositorios no hay forma sencilla de guardar ni leer. Después se usan desde la clase principal.

**Desenlace.**

| Fichero | Qué hay que hacer | Paso |
|---|---|---|
| `CategoriaRepository.java`, `ProductoRepository.java`, `ClienteRepository.java` | Tres ficheros nuevos, una interfaz de 3 líneas cada uno | 1 |
| `TiendaHibernateApplication.java` | Ya existe: aquí se escribe el CRUD | 2 |

### Paso 1 — Los tres repositorios (nuevos, un fichero cada uno)

```java
// CategoriaRepository.java
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Integer> { }
```

```java
// ProductoRepository.java
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Integer> { }
```

```java
// ClienteRepository.java
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> { }
```

### Paso 2 — El CRUD, en la clase principal

En la clase principal, **debajo de la línea `SpringApplication.run(...)`**:

```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class TiendaHibernateApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(TiendaHibernateApplication.class, args);

        // Pedimos a Spring los repositorios
        CategoriaRepository categorias = ctx.getBean(CategoriaRepository.class);
        ProductoRepository productos = ctx.getBean(ProductoRepository.class);
        ClienteRepository clientes = ctx.getBean(ClienteRepository.class);

        // ===== C - CREATE (insertar) =====
        Categoria perifericos = categorias.save(new Categoria("Periféricos"));
        Categoria monitores = categorias.save(new Categoria("Monitores"));

        productos.save(new Producto("Teclado", 25.90, 40, perifericos));
        Producto raton = productos.save(new Producto("Ratón", 12.50, 60, perifericos));
        productos.save(new Producto("Monitor 24 pulgadas", 129.00, 15, monitores));
        Producto alfombrilla = productos.save(new Producto("Alfombrilla", 5.00, 100, perifericos));

        clientes.save(new Cliente("Ana"));
        clientes.save(new Cliente("Luis"));

        // ===== R - READ (consultar) =====
        System.out.println("--- PRODUCTOS ---");
        for (Producto p : productos.findAll()) {
            System.out.println(p.getNombre() + " | " + p.getPrecio() + " | " + p.getCategoria().getNombre());
        }
        System.out.println("--- CLIENTES ---");
        for (Cliente c : clientes.findAll()) {
            System.out.println(c.getNombre());
        }

        // ===== U - UPDATE (modificar) =====
        raton.setPrecio(14.90);
        productos.save(raton);            // como ya tiene id, hace UPDATE

        // ===== D - DELETE (borrar) =====
        productos.deleteById(alfombrilla.getId());

        // ===== Comprobamos el resultado =====
        System.out.println("--- PRODUCTOS DESPUÉS DE MODIFICAR Y BORRAR ---");
        for (Producto p : productos.findAll()) {
            System.out.println(p.getNombre() + " | " + p.getPrecio());
        }
    }
}
```

> La aplicación se queda arrancada al terminar (el proyecto lleva Spring Web). Cuando veas las líneas impresas, páralo con el botón *Stop*.

### Paso 3 — Qué debe aparecer

En la consola (gracias a `show-sql` se ven también los `insert`, `update` y `delete`):

```
--- PRODUCTOS ---
Teclado | 25.9 | Periféricos
Ratón | 12.5 | Periféricos
Monitor 24 pulgadas | 129.0 | Monitores
Alfombrilla | 5.0 | Periféricos
--- CLIENTES ---
Ana
Luis
--- PRODUCTOS DESPUÉS DE MODIFICAR Y BORRAR ---
Teclado | 25.9
Ratón | 14.9
Monitor 24 pulgadas | 129.0
```

### Paso 4 — Comprobar en MySQL

```sql
USE tienda_db;
SELECT * FROM categoria;
SELECT * FROM cliente;
SELECT p.nombre, p.precio, c.nombre AS categoria
FROM producto p JOIN categoria c ON p.id_categoria = c.id;
```

Prueba también a borrar una categoría que tiene productos:

```sql
DELETE FROM categoria WHERE id = 1;
```

MySQL da un error de clave ajena (*Cannot delete or update a parent row*): hay productos que apuntan a esa categoría.

| Método del repositorio | Qué hace |
|---|---|
| `save(objeto)` | Inserta si es nuevo; modifica si ya tiene `id` |
| `findAll()` | Devuelve todos los objetos |
| `findById(id)` | Busca por clave (devuelve un `Optional`) |
| `deleteById(id)` | Borra por clave |

### Ahora prueba tú

Escribe el código en el `main`, debajo de lo que ya hay:
1. Inserta una categoría "Almacenamiento" y un producto "Disco SSD" que pertenezca a ella.
2. Inserta un cliente "Marta".
3. Cambia el stock del monitor a 10 (acuérdate de `save()`).
4. Muestra por consola solo los productos con precio mayor de 20.
5. Borra al cliente "Luis" con `clientes.deleteById(...)`.

**Para pensar:**
1. ¿Para qué sirve cada repositorio?
2. ¿Qué diferencia hay entre `save()` al insertar y `save()` al modificar? ¿Cómo sabe Hibernate cuál de los dos hacer?
3. ¿Por qué hay que llamar a `save(raton)` después de `setPrecio`? (Pista: estado *detached*, apartado 4 de los apuntes.)
4. ¿Qué error da MySQL si borras una categoría con productos y qué hay que hacer antes?
