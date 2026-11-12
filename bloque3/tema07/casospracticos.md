# Tema 7 · Casos prácticos

---

## Caso 1 — Montar el proyecto Spring Boot + Hibernate + MySQL desde cero

**Planteamiento.** Queremos arrancar un proyecto nuevo, `tienda-hibernate`, que gestione una entidad `Producto` (id, nombre, precio, stock) contra una base de datos MySQL llamada `tienda_db`.

**Nudo.** ¿Qué pasos hay que seguir para tener el proyecto listo y la primera entidad mapeada con anotaciones?

**Desenlace:**

1. **Generar el arquetipo** en [start.spring.io](https://start.spring.io/) con `Spring Web`, `Spring Data JPA` y `MySQL Driver`.
2. **Crear la base de datos** vacía en MySQL:
   ```sql
   CREATE DATABASE tienda_db CHARACTER SET utf8mb4;
   ```
3. **Configurar `application.properties`:**
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/tienda_db
   spring.datasource.username=root
   spring.datasource.password=tu_contraseña

   spring.jpa.hibernate.ddl-auto=update
   spring.jpa.show-sql=true
   spring.jpa.properties.hibernate.format_sql=true
   ```
4. **Crear la entidad** con anotaciones (sin fichero `.hbm.xml`):
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
5. **Arrancar la aplicación.** Con `ddl-auto=update`, Hibernate crea automáticamente la tabla `PRODUCTO` a partir de la entidad — no hace falta escribir el `CREATE TABLE` a mano ni ningún `.hbm.xml`. En el log veremos (gracias a `show-sql=true`) el `CREATE TABLE` o `ALTER TABLE` que Hibernate ha generado.

