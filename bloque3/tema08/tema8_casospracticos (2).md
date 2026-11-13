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

## Caso 5 — Claves ajenas y persistencia: `Producto` pertenece a una `Categoria`

**Planteamiento.** Seguimos con el proyecto `tienda-hibernate`. Cada producto debe pertenecer a **una categoría** (Periféricos, Monitores...) y una categoría tiene **muchos productos**. Queremos que Hibernate cree la clave ajena, guardar productos asociados a su categoría, modificar un precio y ver todo en el navegador.

**Nudo.** Es una relación **1:N**. En SQL la clave ajena (`id_categoria`) está en la tabla `producto` (el lado N). En Java, `Producto` guarda el **objeto** `Categoria`, no un número. La anotación que lo expresa es `@ManyToOne`.

**Desenlace:**

1. **Nueva entidad `Categoria`**, igual que `Producto` pero sin anotaciones de relación:

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

2. **Modificar `Producto`** (el del Tema 7): añadir el atributo de la relación (la clave ajena), un constructor que lo reciba y su get/set:

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

   Con `ddl-auto=update`, Hibernate crea la tabla `categoria`, añade la columna `id_categoria` a `producto` y crea la clave ajena. En la consola se ve algo así:

   ```sql
   create table categoria (id integer not null auto_increment, nombre varchar(255), primary key (id))
   alter table producto add column id_categoria integer
   alter table producto add constraint ... foreign key (id_categoria) references categoria (id)
   ```

   > 💡 **Regla:** el `@ManyToOne` va siempre en la clase del lado N, la que tiene la clave ajena en su tabla.

3. **Repositorios:** uno nuevo y una consulta más en el de productos.

   ```java
   public interface CategoriaRepository extends JpaRepository<Categoria, Integer> { }

   public interface ProductoRepository extends JpaRepository<Producto, Integer> {
       List<Producto> findByNombreContaining(String texto);
       List<Producto> findByCategoriaId(Integer idCategoria);   // WHERE id_categoria = ?
   }
   ```

4. **Controlador** (se puede añadir al de `Producto` o crear `CategoriaController`):

   ```java
   @RestController
   @RequestMapping("/categorias")
   public class CategoriaController {

       private final CategoriaRepository categorias;
       private final ProductoRepository productos;

       public CategoriaController(CategoriaRepository categorias, ProductoRepository productos) {
           this.categorias = categorias;
           this.productos = productos;
       }

       // GET /categorias
       @GetMapping
       public List<Categoria> listar() { return categorias.findAll(); }

       // POST /categorias   {"nombre":"Periféricos"}
       @PostMapping
       public Categoria crear(@RequestBody Categoria c) {
           c.setId(0);
           return categorias.save(c);
       }

       // GET /categorias/1/productos  -> los productos de UNA categoría (el 1:N)
       @GetMapping("/{id}/productos")
       public List<Producto> productosDe(@PathVariable Integer id) {
           return productos.findByCategoriaId(id);
       }

       // POST /categorias/1/productos   {"nombre":"Ratón","precio":12.5,"stock":30}
       // El producto nace asignado a la categoría de la URL.
       @PostMapping("/{id}/productos")
       public Producto nuevoProducto(@PathVariable Integer id, @RequestBody Producto p) {
           Categoria c = categorias.findById(id)
                   .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
           p.setId(0);   // 0 = objeto nuevo: la BD genera el id
           p.setCategoria(c);
           return productos.save(p);
       }

       // DELETE /categorias/1  -> si tiene productos, MySQL lo rechaza (clave ajena)
       @DeleteMapping("/{id}")
       public void borrar(@PathVariable Integer id) { categorias.deleteById(id); }

       @ExceptionHandler(DataIntegrityViolationException.class)
       @ResponseStatus(HttpStatus.CONFLICT)
       public String conflicto() { return "No se puede borrar: tiene productos asociados"; }
   }
   ```

5. **Persistencia: modificar sin llamar a `save()`.** Añade al `ProductoController`:

   ```java
   // PUT /productos/2?precio=19.9
   @PutMapping("/{id}")
   @Transactional
   public Producto cambiarPrecio(@PathVariable Integer id, @RequestParam double precio) {
       Producto p = productoRepository.findById(id).orElseThrow();
       p.setPrecio(precio);      // no hay save()
       return p;
   }
   ```

   Aunque no se llama a `save()`, la fila se actualiza. Dentro de `@Transactional` el producto está en estado **persistent** (Apartado 4): Hibernate detecta el cambio (*dirty checking*) y lanza el `UPDATE` al terminar el método. En la consola se ve el `update producto set ...`.

6. **Probar en el navegador** (los `GET` en la barra de direcciones, el resto con `fetch` en F12 como en el Caso 4):

   ```javascript
   fetch('/categorias', {method:'POST', headers:{'Content-Type':'application/json'},
     body: JSON.stringify({nombre:'Periféricos'})}).then(r=>r.json()).then(console.log)

   fetch('/categorias/1/productos', {method:'POST', headers:{'Content-Type':'application/json'},
     body: JSON.stringify({nombre:'Ratón', precio:12.5, stock:30})}).then(r=>r.json()).then(console.log)

   fetch('/productos/1?precio=19.9', {method:'PUT'}).then(r=>r.json()).then(console.log)

   fetch('/categorias/1', {method:'DELETE'}).then(r=>r.text()).then(console.log)   // 409: tiene productos
   ```

   - `http://localhost:8080/productos` → cada producto lleva dentro su `categoria`.
   - `http://localhost:8080/categorias/1/productos` → solo los de esa categoría.
   - Borrar la categoría 1 da **409**: lo impide **MySQL** por la clave ajena, no Java.

7. **Comprobar en MySQL** que se ha creado la clave ajena:

   ```sql
   SHOW CREATE TABLE producto;       -- aparece el CONSTRAINT ... FOREIGN KEY (id_categoria)
   SELECT p.nombre, c.nombre AS categoria
   FROM producto p JOIN categoria c ON p.id_categoria = c.id;
   ```

| Anotación | Qué hace |
|---|---|
| `@ManyToOne` | Muchos de esta clase se relacionan con uno de la otra (lado N) |
| `@JoinColumn(name=...)` | Nombre de la columna de la clave ajena |
| `@Transactional` | El método se ejecuta en una transacción; los cambios a objetos persistentes se guardan solos |

> ⚠️ Si cambias una tabla `producto` que ya existía, usa `ddl-auto=create` (borra y recrea) o `update` (añade la columna y la clave ajena). Con `update` los productos antiguos quedan con `id_categoria` a `NULL`.

**Ampliación:** una página `src/main/resources/static/index.html` que haga `fetch('/productos')` y muestre una tabla con nombre, precio y categoría; o relación N:M (`@ManyToMany` con `@JoinTable`) entre clientes y productos (un cliente compra varios productos y un producto lo compran varios clientes).
