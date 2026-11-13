# Tema 8 · Casos prácticos

---

## Caso 1 — "Sentencias HQL"

**Planteamiento.** Con la entidad `Producto` (`id`, `nombre`, `precio`, `stock`), se pide una sentencia HQL de tipo `update` que ponga `stock = 25` cuando `id = 105` y `nombre = 'Teclado'`.

**Nudo.** Usar `Query` con parámetros con nombre sobre la sesión activa.

**Desenlace:**

```java
Session session = sessionFactory.openSession();
Transaction transaction = session.beginTransaction();

Query q = session.createQuery(
    "update Producto set stock = :num where id = :id and nombre = :name");
q.setParameter("num", 25);
q.setParameter("id", 105);
q.setParameter("name", "Teclado");

int status = q.executeUpdate();
transaction.commit();

if (status > 0)
    System.out.println("Update realizado");
else
    System.out.println("Update no realizado");
```

El primer parámetro (`num`) fija el valor de la columna `stock`; el segundo y el tercero (`id`, `name`) forman la cláusula `WHERE`. Si `status` es mayor que 0, la fila se ha actualizado correctamente.

---

## Caso 2 — CRUD completo con Spring Data JPA (anotaciones)

**Planteamiento.** Sobre el proyecto `tienda-hibernate` del Tema 7 (entidad `Producto` anotada), queremos poder guardar, consultar, actualizar y borrar productos sin escribir HQL a mano.

**Nudo.** Con Spring Data JPA no hace falta gestionar `Session`/`Transaction` manualmente para las operaciones básicas: basta con una interfaz `Repository`.

**Desenlace:**

```java
public interface ProductoRepository extends JpaRepository<Producto, Integer> {
    List<Producto> findByNombreContaining(String texto);
}
```

Con esa única interfaz, Spring Data JPA genera automáticamente la implementación con:

```java
productoRepository.save(new Producto("Teclado", 25.90, 40));   // INSERT (o UPDATE si el id ya existe)
productoRepository.findById(1);                                 // equivalente a session.get()
productoRepository.findAll();                                   // equivalente a "FROM Producto"
productoRepository.deleteById(1);                                // equivalente a session.delete()
productoRepository.findByNombreContaining("tec");                // HQL generado automáticamente a partir del nombre del método
```

> 💡 Por debajo, `JpaRepository` sigue usando Hibernate y su `EntityManager` (la evolución moderna de `Session`), pero Spring Boot nos evita escribir el `try/catch/finally` con `beginTransaction()`/`commit()`/`rollback()` para cada operación — Spring gestiona la transacción por nosotros (anotación `@Transactional` internamente). Es útil conocer el flujo manual (Caso 1) para entender **qué** ocurre por debajo, aunque en el día a día se trabaje así, con repositorios.

---

## Caso 3 — Relaciones entre tablas: categorías, productos, clientes y compras

**Planteamiento.** Seguimos con el proyecto `tienda-hibernate`, que ya tiene la entidad `Producto`. Vamos a ampliar la base de datos:
- Cada producto pertenece a **una categoría**, y una categoría tiene **muchos productos** (1:N).
- Un **cliente** compra **varios productos**, y un producto lo compran **varios clientes** (N:M, con una tabla intermedia `compra`).

Después probaremos a **insertar, consultar, modificar y borrar** datos desde Java y comprobaremos las tablas en MySQL.

**Nudo.** Cada relación se expresa con un **atributo que es el objeto relacionado** (no un `int`). En el 1:N la clave ajena está en la tabla del lado N (`producto`), así que el `@ManyToOne` va en `Producto`. En el N:M hace falta una tabla intermedia, que se declara con `@ManyToMany` + `@JoinTable`.

```
 CATEGORIA (1) ────< PRODUCTO (N)  >────<  CLIENTE (N)
 id, nombre          id, nombre,           id, nombre
                     precio, stock,
                     id_categoria (FK)     tabla intermedia COMPRA: id_cliente, id_producto
```

**Desenlace:**

### Paso 0 — Dónde va cada cosa

Todo va en el **mismo paquete** donde ya está `Producto` (dentro de `src/main/java`). Las clases marcadas con **(nuevo)** se crean con *clic derecho → New → Java Class*; la marcada con **(modificar)** ya existe:

```
src/main/java/com/....../tienda/        <- el paquete de tu proyecto
├── TiendaHibernateApplication.java     (ya existe, no se toca)
├── Producto.java                       (modificar)  Paso 2
├── Categoria.java                      (nuevo)      Paso 1
├── Cliente.java                        (nuevo)      Paso 3
├── CategoriaRepository.java            (nuevo)      Paso 4
├── ProductoRepository.java             (nuevo*)     Paso 4
├── ClienteRepository.java              (nuevo)      Paso 4
└── Pruebas.java                        (nuevo)      Paso 5
src/main/resources/
└── application.properties              (modificar)  Paso 6
```

\* Si ya creaste `ProductoRepository` en el Caso 2, déjalo como está.

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

Hay que añadir **tres cosas** a la clase del Tema 7: el atributo `categoria` con su anotación, un constructor que lo reciba y su get/set. Queda así (lo nuevo está marcado):

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
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "CLIENTE")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String nombre;

    // Muchos clientes <-> muchos productos, con la tabla intermedia COMPRA
    @ManyToMany
    @JoinTable(name = "compra",                                        // tabla intermedia
               joinColumns = @JoinColumn(name = "id_cliente"),         // FK hacia Cliente
               inverseJoinColumns = @JoinColumn(name = "id_producto")) // FK hacia Producto
    private Set<Producto> productos = new HashSet<>();

    public Cliente() {}

    public Cliente(String nombre) {
        this.nombre = nombre;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Set<Producto> getProductos() { return productos; }
    public void setProductos(Set<Producto> productos) { this.productos = productos; }
}
```

### Paso 4 — Los tres repositorios (nuevos, un fichero cada uno)

Son interfaces vacías: Spring crea el código de `save`, `findAll`, `delete`... (como en el Caso 2).

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

### Paso 5 — `Pruebas.java` (nuevo): insertar, consultar, modificar y borrar

Esta clase se ejecuta **sola al arrancar la aplicación**. Aquí es donde los alumnos escriben y prueban su código; cada bloque está separado y comentado.

```java
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class Pruebas implements CommandLineRunner {

    private final CategoriaRepository categorias;
    private final ProductoRepository productos;
    private final ClienteRepository clientes;

    // Spring nos entrega los repositorios ya creados
    public Pruebas(CategoriaRepository categorias, ProductoRepository productos,
                   ClienteRepository clientes) {
        this.categorias = categorias;
        this.productos = productos;
        this.clientes = clientes;
    }

    @Override
    @Transactional
    public void run(String... args) {

        // ===== 1. INSERTAR =====
        Categoria perifericos = categorias.save(new Categoria("Periféricos"));
        Categoria monitores = categorias.save(new Categoria("Monitores"));

        Producto teclado = productos.save(new Producto("Teclado", 25.90, 40, perifericos));
        Producto raton = productos.save(new Producto("Ratón", 12.50, 60, perifericos));
        Producto monitor = productos.save(new Producto("Monitor 24 pulgadas", 129.00, 15, monitores));
        Producto alfombrilla = productos.save(new Producto("Alfombrilla", 5.00, 100, perifericos));

        Cliente ana = new Cliente("Ana");
        ana.getProductos().add(teclado);
        ana.getProductos().add(raton);
        clientes.save(ana);

        Cliente luis = new Cliente("Luis");
        luis.getProductos().add(teclado);      // el teclado lo han comprado Ana y Luis
        luis.getProductos().add(monitor);
        clientes.save(luis);

        // ===== 2. CONSULTAR =====
        System.out.println("--- PRODUCTOS ---");
        productos.findAll().forEach(p ->
            System.out.println(p.getNombre() + " | " + p.getPrecio() + " | " + p.getCategoria().getNombre()));

        System.out.println("--- COMPRAS ---");
        clientes.findAll().forEach(c -> {
            System.out.println(c.getNombre() + " ha comprado:");
            c.getProductos().forEach(p -> System.out.println("   - " + p.getNombre()));
        });

        // ===== 3. MODIFICAR =====
        // No hace falta save(): el objeto está en estado PERSISTENT y Hibernate
        // detecta el cambio y lanza el UPDATE al terminar (dirty checking).
        raton.setPrecio(14.90);

        // ===== 4. BORRAR =====
        // La alfombrilla no la ha comprado nadie, así que se puede borrar.
        productos.delete(alfombrilla);

        // ===== Comprobamos el resultado =====
        System.out.println("--- PRODUCTOS DESPUÉS DE MODIFICAR Y BORRAR ---");
        productos.findAll().forEach(p ->
            System.out.println(p.getNombre() + " | " + p.getPrecio()));
    }
}
```

> La aplicación se queda arrancada cuando termina el método `run` (porque el proyecto lleva Spring Web). Cuando veas las líneas impresas, ya puedes pararla con el botón *Stop*.

### Paso 6 — `application.properties` (modificar)

Mientras se practica, que Hibernate **borre y cree las tablas en cada arranque**, así los datos no se duplican al volver a ejecutar:

```properties
spring.jpa.hibernate.ddl-auto=create
spring.jpa.show-sql=true
```

(Con `create` se pierden los datos en cada arranque. Para conservarlos, cambia a `update` y quita las inserciones de `Pruebas`.)

### Paso 7 — Qué debe aparecer

**En la consola**, gracias a `show-sql`, Hibernate crea las tablas y las claves ajenas:

```sql
create table categoria (id integer not null auto_increment, nombre varchar(255), primary key (id))
create table cliente (id integer not null auto_increment, nombre varchar(255), primary key (id))
create table compra (id_cliente integer not null, id_producto integer not null)
create table producto (..., id_categoria integer, primary key (id))
alter table producto add constraint ... foreign key (id_categoria) references categoria (id)
alter table compra add constraint ... foreign key (id_producto) references producto (id)
alter table compra add constraint ... foreign key (id_cliente) references cliente (id)
```

y después, las líneas impresas por el programa:

```
--- PRODUCTOS ---
Teclado | 25.9 | Periféricos
Ratón | 12.5 | Periféricos
Monitor 24 pulgadas | 129.0 | Monitores
Alfombrilla | 5.0 | Periféricos
--- COMPRAS ---
Ana ha comprado:
   - Teclado
   - Ratón
Luis ha comprado:
   - Teclado
   - Monitor 24 pulgadas
--- PRODUCTOS DESPUÉS DE MODIFICAR Y BORRAR ---
Teclado | 25.9
Ratón | 14.9
Monitor 24 pulgadas | 129.0
```

(El orden de las líneas puede variar.)

### Paso 8 — Comprobar en MySQL

```sql
USE tienda_db;
SHOW TABLES;                       -- categoria, cliente, compra, producto
SHOW CREATE TABLE producto;        -- aparece CONSTRAINT ... FOREIGN KEY (id_categoria)
SHOW CREATE TABLE compra;          -- dos claves ajenas: id_cliente e id_producto

-- 1:N: productos con su categoría
SELECT p.nombre, c.nombre AS categoria
FROM producto p JOIN categoria c ON p.id_categoria = c.id;

-- N:M: qué ha comprado cada cliente
SELECT cl.nombre AS cliente, p.nombre AS producto
FROM cliente cl
JOIN compra co ON co.id_cliente = cl.id
JOIN producto p ON p.id = co.id_producto;
```

### Paso 9 — Probar que la clave ajena protege los datos

Intenta borrar en MySQL una categoría que tiene productos:

```sql
DELETE FROM categoria WHERE id = 1;
```

MySQL responde con un error de clave ajena (*Cannot delete or update a parent row: a foreign key constraint fails*). Quien lo impide es **MySQL**, no Java ni Hibernate.

| Anotación | Qué hace |
|---|---|
| `@ManyToOne` | Muchos de esta clase se relacionan con uno de la otra (lado N) |
| `@JoinColumn(name=...)` | Nombre de la columna de la clave ajena |
| `@ManyToMany` | Muchos a muchos: necesita una tabla intermedia |
| `@JoinTable(name=..., joinColumns=..., inverseJoinColumns=...)` | Nombre de la tabla intermedia y sus dos claves ajenas |
| `@Transactional` | El método se ejecuta en una transacción; los cambios en objetos persistentes se guardan solos |

> ⚠️ Si el servidor es MySQL en Linux, los nombres de tabla distinguen mayúsculas; Spring Boot los pasa a minúsculas (`producto`, `categoria`), por eso las consultas SQL van en minúsculas.

**Ahora prueba tú** (escribe el código en `Pruebas.java`, dentro de `run`):
1. Inserta una tercera categoría, "Almacenamiento", y un producto "Disco SSD" que pertenezca a ella.
2. Crea un cliente "Marta" que compre el ratón y el disco SSD.
3. Cambia el stock del monitor a 10 (sin llamar a `save()`).
4. Intenta borrar el teclado con `productos.delete(teclado)`. ¿Qué error da? ¿Por qué?
5. Muestra por consola todos los productos de la categoría "Periféricos" recorriendo `productos.findAll()` y comparando el nombre de la categoría.

**Para pensar:**
1. ¿En qué clase va el `@ManyToOne` y por qué?
2. ¿Qué tabla se crea para el N:M y qué columnas tiene? ¿Aparece como clase Java?
3. ¿Por qué el precio del ratón cambia sin llamar a `save()`?
4. ¿Qué pasa si intentas borrar un producto que está en la tabla `compra`? ¿Quién lo impide?
