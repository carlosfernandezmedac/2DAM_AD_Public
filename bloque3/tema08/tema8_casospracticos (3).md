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

## Caso 3 — CRUD completo con Spring Data JPA (anotaciones)

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

> 💡 Por debajo, `JpaRepository` sigue usando Hibernate y su `EntityManager` (la evolución moderna de `Session`), pero Spring Boot nos evita escribir el `try/catch/finally` con `beginTransaction()`/`commit()`/`rollback()` para cada operación — Spring gestiona la transacción por nosotros (anotación `@Transactional` internamente). Es útil conocer el flujo manual (Casos 1 y 2) para entender **qué** ocurre por debajo, aunque en el día a día se trabaje así, con repositorios.

---

## Caso 4 — Publicar los datos en el navegador (API REST)

**Planteamiento.** Sobre el proyecto `tienda-hibernate` (entidad `Producto` y `ProductoRepository` del Caso 3), queremos **ver los productos en el navegador** y poder **añadir uno nuevo** sin escribir SQL ni HTML.

**Nudo.** Spring Web ya está en el proyecto (lo añadimos en el Tema 7). Solo falta una clase que reciba las peticiones HTTP y use el repositorio. Spring convierte las entidades a **JSON** automáticamente.

**Desenlace:**

1. **Crear el controlador** `ProductoController`:

   ```java
   @RestController
   @RequestMapping("/productos")
   public class ProductoController {

       private final ProductoRepository productoRepository;

       // Inyección de dependencias: Spring nos entrega el repositorio ya creado
       public ProductoController(ProductoRepository productoRepository) {
           this.productoRepository = productoRepository;
       }

       // GET /productos  ->  SELECT * FROM producto
       @GetMapping
       public List<Producto> listar() {
           return productoRepository.findAll();
       }

       // GET /productos/buscar/tec  ->  productos cuyo nombre contiene "tec"
       @GetMapping("/buscar/{texto}")
       public List<Producto> buscar(@PathVariable String texto) {
           return productoRepository.findByNombreContaining(texto);
       }

       // POST /productos  con un JSON en el cuerpo  ->  INSERT
       @PostMapping
       public Producto crear(@RequestBody Producto producto) {
           return productoRepository.save(producto);
       }
   }
   ```

   > La entidad `Producto` necesita sus **getters y setters** (Spring los usa para convertir a JSON y de JSON a objeto).

2. **Arrancar la aplicación** y abrir en el navegador:

   ```
   http://localhost:8080/productos
   ```

   La primera vez devuelve una lista vacía: `[]`.

3. **Añadir un producto.** Desde un navegador no se puede hacer un `POST` escribiendo la URL, así que lo hacemos desde la consola del navegador (tecla **F12** → pestaña *Consola*), estando en cualquier página de `localhost:8080`:

   ```javascript
   fetch('/productos', {
       method: 'POST',
       headers: { 'Content-Type': 'application/json' },
       body: JSON.stringify({ nombre: 'Teclado', precio: 25.90, stock: 40 })
   }).then(r => r.json()).then(console.log)
   ```

   Se puede hacer también con `curl` (en Windows, en PowerShell, usa `curl.exe` y comillas dobles escapadas):

   ```bash
   curl -X POST http://localhost:8080/productos -H "Content-Type: application/json" -d '{"nombre":"Teclado","precio":25.90,"stock":40}'
   ```

4. **Volver a abrir** `http://localhost:8080/productos`. Ahora aparece el producto con el `id` que ha generado la base de datos:

   ```json
   [{"id":1,"nombre":"Teclado","precio":25.9,"stock":40}]
   ```

5. **Mirar la consola de la aplicación.** Gracias a `show-sql=true` se ve el `insert into producto ...` y el `select ...` que Hibernate ha lanzado por debajo. Prueba también `http://localhost:8080/productos/buscar/tec`.

| Anotación | Qué hace |
|---|---|
| `@RestController` | Los métodos devuelven datos (JSON), no páginas HTML |
| `@RequestMapping("/productos")` | Prefijo común de todas las rutas del controlador |
| `@GetMapping` / `@PostMapping` | Responden a peticiones `GET` / `POST` |
| `@PathVariable` | Toma un valor de la propia URL (`{texto}`) |
| `@RequestBody` | Convierte el JSON recibido en un objeto `Producto` |

> 💡 El flujo completo es: **navegador → controlador → repositorio → Hibernate → base de datos**, y la respuesta hace el camino inverso. Cada capa solo conoce a la siguiente.

> ⚠️ Si el puerto 8080 está ocupado, añade `server.port=8081` en `application.properties` y usa `8081` en las URL.

> ⚠️ Con entidades relacionadas en **los dos sentidos** (`@OneToMany` y `@ManyToOne` a la vez) el JSON entra en un bucle infinito. Para este caso no ocurre porque `Producto` no tiene relaciones; con relaciones se suele devolver un objeto simplificado (DTO) o usar `@JsonIgnore`.

**Ampliación (para quien termine antes):** añade `DELETE /productos/{id}` con `deleteById`, o una pequeña página `src/main/resources/static/index.html` que muestre los productos en una tabla con `fetch('/productos')`.

---

## Caso 5 — Relaciones entre tablas: categorías, productos, clientes y compras

**Planteamiento.** Seguimos con el proyecto `tienda-hibernate`, que ya tiene la entidad `Producto`. Queremos ampliar la base de datos:
- Cada producto pertenece a **una categoría**, y una categoría tiene **muchos productos** (1:N).
- Un **cliente** compra **varios productos**, y un producto lo compran **varios clientes** (N:M, con una tabla intermedia `compra`).

Hibernate debe crear las tablas y las claves ajenas, y debemos poder guardar datos y comprobarlo en MySQL.

**Nudo.** Cada relación se expresa con un **atributo que es el objeto relacionado** (no un `int`). En el 1:N la clave ajena está en la tabla del lado N (`producto`), así que el `@ManyToOne` va en `Producto`. En el N:M hace falta una tabla intermedia, y se declara con `@ManyToMany` + `@JoinTable`.

```
 CATEGORIA (1) ────< PRODUCTO (N) >────< CLIENTE
 id, nombre          id, nombre,            id, nombre
                     precio, stock,
                     id_categoria (FK)      PRODUCTO ──< COMPRA >── CLIENTE
                                            (tabla intermedia: id_cliente, id_producto)
```

**Desenlace:**

1. **Entidad `Categoria`** (lado 1, entidad normal):

   ```java
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

2. **Modificar `Producto`** (el del Tema 7): añadir el atributo de la relación, un constructor que lo reciba y su get/set:

   ```java
   @ManyToOne                          // muchos productos -> una categoría
   @JoinColumn(name = "id_categoria")  // columna de la clave ajena en la tabla PRODUCTO
   private Categoria categoria;        // el OBJETO, no un int

   public Producto(String nombre, double precio, int stock, Categoria categoria) {
       this(nombre, precio, stock);
       this.categoria = categoria;
   }

   public Categoria getCategoria() { return categoria; }
   public void setCategoria(Categoria categoria) { this.categoria = categoria; }
   ```

   > 💡 **Regla:** el `@ManyToOne` va siempre en la clase que tiene la clave ajena en su tabla (el lado N).

3. **Entidad `Cliente`**, con la relación N:M hacia `Producto`:

   ```java
   @Entity
   @Table(name = "CLIENTE")
   public class Cliente {

       @Id
       @GeneratedValue(strategy = GenerationType.IDENTITY)
       private int id;

       private String nombre;

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

4. **Repositorios** (tres interfaces vacías, como en el Caso 3):

   ```java
   public interface CategoriaRepository extends JpaRepository<Categoria, Integer> { }
   public interface ProductoRepository extends JpaRepository<Producto, Integer> { }
   public interface ClienteRepository extends JpaRepository<Cliente, Integer> { }
   ```

5. **Clase que crea los datos al arrancar.** Spring ejecuta el método `run` al iniciar la aplicación. Va con `@Transactional`: todo ocurre dentro de una transacción y Hibernate guarda los cambios al terminar.

   ```java
   @Component
   public class DatosDePrueba implements CommandLineRunner {

       private final CategoriaRepository categorias;
       private final ProductoRepository productos;
       private final ClienteRepository clientes;

       public DatosDePrueba(CategoriaRepository categorias, ProductoRepository productos,
                            ClienteRepository clientes) {
           this.categorias = categorias;
           this.productos = productos;
           this.clientes = clientes;
       }

       @Override
       @Transactional
       public void run(String... args) {
           if (categorias.count() > 0) return;   // no repetir los datos si ya existen

           // 1) Categorías
           Categoria perifericos = categorias.save(new Categoria("Periféricos"));
           Categoria monitores = categorias.save(new Categoria("Monitores"));

           // 2) Productos, cada uno con su categoría (1:N)
           Producto teclado = productos.save(new Producto("Teclado", 25.90, 40, perifericos));
           Producto raton = productos.save(new Producto("Ratón", 12.50, 60, perifericos));
           Producto monitor = productos.save(new Producto("Monitor 24\"", 129.00, 15, monitores));

           // 3) Clientes y sus compras (N:M)
           Cliente ana = new Cliente("Ana");
           ana.getProductos().add(teclado);
           ana.getProductos().add(raton);
           clientes.save(ana);

           Cliente luis = new Cliente("Luis");
           luis.getProductos().add(teclado);   // el teclado lo han comprado Ana y Luis
           luis.getProductos().add(monitor);
           clientes.save(luis);

           // 4) Modificar sin llamar a save(): el producto está en estado PERSISTENT
           raton.setPrecio(14.90);

           // 5) Leer y mostrar por consola
           productos.findAll().forEach(p ->
               System.out.println(p.getNombre() + " -> " + p.getCategoria().getNombre()));
           clientes.findAll().forEach(c -> {
               System.out.println("Cliente " + c.getNombre() + " compró:");
               c.getProductos().forEach(p -> System.out.println("   - " + p.getNombre()));
           });
       }
   }
   ```

   (Imports: `org.springframework.boot.CommandLineRunner`, `org.springframework.stereotype.Component`, `org.springframework.transaction.annotation.Transactional`.)

   > 💡 En el paso 4 no hay `save()` y el precio del ratón se actualiza igualmente. Es el *dirty checking* del Apartado 4: mientras el objeto está en estado **persistent**, Hibernate detecta el cambio y lanza el `UPDATE` al terminar la transacción. En la consola se ve el `update producto set ...`.

6. **Arrancar** con `spring.jpa.hibernate.ddl-auto=update` (o `create` para empezar de cero). En la consola, gracias a `show-sql`, se ve cómo Hibernate crea las tres tablas y las claves ajenas:

   ```sql
   create table categoria (id integer not null auto_increment, nombre varchar(255), primary key (id))
   create table cliente (id integer not null auto_increment, nombre varchar(255), primary key (id))
   create table compra (id_cliente integer not null, id_producto integer not null)
   alter table producto add column id_categoria integer
   alter table producto add constraint ... foreign key (id_categoria) references categoria (id)
   alter table compra add constraint ... foreign key (id_producto) references producto (id)
   alter table compra add constraint ... foreign key (id_cliente) references cliente (id)
   ```

   Y al final, las líneas impresas:

   ```
   Teclado -> Periféricos
   Ratón -> Periféricos
   Monitor 24" -> Monitores
   Cliente Ana compró:
      - Teclado
      - Ratón
   Cliente Luis compró:
      - Teclado
      - Monitor 24"
   ```

7. **Comprobar en MySQL** que las tablas y claves ajenas existen y que los datos están bien:

   ```sql
   USE tienda_db;
   SHOW TABLES;                       -- categoria, cliente, compra, producto
   SHOW CREATE TABLE producto;        -- CONSTRAINT ... FOREIGN KEY (id_categoria)
   SHOW CREATE TABLE compra;          -- dos claves ajenas: id_cliente e id_producto

   -- 1:N: productos con su categoría
   SELECT p.nombre, c.nombre AS categoria
   FROM producto p JOIN categoria c ON p.id_categoria = c.id;

   -- N:M: qué ha comprado cada cliente
   SELECT cl.nombre AS cliente, p.nombre AS producto
   FROM cliente cl
   JOIN compra co ON co.id_cliente = cl.id
   JOIN producto p ON p.id = co.id_producto;

   -- El precio del ratón debe ser 14.9 (lo cambió Hibernate sin save())
   SELECT nombre, precio FROM producto;
   ```

8. **Probar que la clave ajena protege los datos.** Intenta borrar en MySQL una categoría que tiene productos:

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

> ⚠️ Si ya tenías productos del Caso 1, siguen en la tabla pero con `id_categoria = NULL`. Para empezar limpio, cambia un momento a `ddl-auto=create` (borra y recrea las tablas) y vuelve a `update` después.

> ⚠️ Si el servidor es MySQL en Linux, los nombres de tabla distinguen mayúsculas; Spring Boot los pasa a minúsculas (`producto`, `categoria`), por eso las consultas SQL van en minúsculas.

**Para pensar:**
1. ¿En qué clase va el `@ManyToOne` y por qué?
2. ¿Qué tabla se crea para el N:M y qué columnas tiene? ¿Aparece como clase Java?
3. ¿Por qué el precio del ratón cambia sin llamar a `save()`?
4. ¿Qué pasa si intentas borrar un producto que está en la tabla `compra`? ¿Quién lo impide?

**Ampliación:** añade una relación 1:1 (`@OneToOne`), por ejemplo `Producto` con `FichaTecnica`, o muestra estos datos en el navegador con el controlador del Caso 4.
