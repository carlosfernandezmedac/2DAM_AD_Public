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
public class Customer {
    private int id;
    private String firstName;
    private String lastName;
    private int customerNumber;

    public Customer() {}

    public Customer(String fname, String lname, int custNum) {
        this.firstName = fname;
        this.lastName = lname;
        this.customerNumber = custNum;
    }

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

## 3. Mapeo con anotaciones JPA

Para que la clase `Customer` sea una **entidad** (se guarde en una tabla) se anota directamente sobre la clase, sin ficheros aparte. La entidad completa, con `@Entity`, `@Table`, `@Id`, `@GeneratedValue` y `@Column`, ya se vio en el Tema 7. Equivale a esta tabla:

```sql
CREATE TABLE CUSTOMER (
  id INT NOT NULL AUTO_INCREMENT,
  first_name VARCHAR(20) DEFAULT NULL,
  last_name  VARCHAR(20) DEFAULT NULL,
  customer_number INT DEFAULT NULL,
  PRIMARY KEY (id)
);
```

| Anotación | Qué hace |
|---|---|
| `@Entity` | La clase es una entidad: se corresponde con una tabla |
| `@Table(name=...)` | Nombre de la tabla (si no se pone, se usa el de la clase) |
| `@Id` | Atributo que es la clave primaria |
| `@GeneratedValue(strategy = IDENTITY)` | La PK la genera la base de datos (`AUTO_INCREMENT`) |
| `@Column(name=...)` | Nombre de la columna (si no se pone, se usa el del atributo) |

Con `ddl-auto=update` o `create`, Hibernate crea la tabla a partir de estas anotaciones. Aquí se añade lo que falta: **las relaciones entre tablas**.

### 3.1 Relaciones entre entidades

En SQL una relación se expresa con una **clave ajena (FK)**. En Java se expresa con un **atributo que es el objeto relacionado** (no un `int` con el id).

| Relación | En SQL | Anotación JPA |
|---|---|---|
| **1:N** (un monitor, muchas actividades) | FK en la tabla del lado N | `@ManyToOne` en la clase del lado N |
| **1:1** (un socio, un carnet) | FK con `UNIQUE` | `@OneToOne` |
| **N:M** (socios y actividades) | Tabla intermedia con dos FK | `@ManyToMany` + `@JoinTable` |

**1:N — `@ManyToOne`.** La FK está en `actividad`, así que la anotación va en `Actividad`:

```java
@Entity
public class Actividad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nombre;

    @ManyToOne                          // muchas actividades -> un monitor
    @JoinColumn(name = "id_monitor")    // columna de la clave ajena
    private Monitor monitor;            // el OBJETO, no un int
}
```

> 💡 **Regla:** el `@ManyToOne` va siempre en la clase que tiene la FK en su tabla (el lado N).

**1:1 — `@OneToOne`:**

```java
@OneToOne(cascade = CascadeType.ALL)    // al guardar el socio se guarda su carnet
@JoinColumn(name = "id_carnet")         // FK UNIQUE en la tabla socio
private Carnet carnet;
```

**N:M — `@ManyToMany` + `@JoinTable`.** Hibernate crea la tabla intermedia:

```java
@ManyToMany
@JoinTable(name = "inscripcion",
           joinColumns = @JoinColumn(name = "id_socio"),
           inverseJoinColumns = @JoinColumn(name = "id_actividad"))
private Set<Actividad> actividades = new HashSet<>();
```

**Detalles que conviene conocer:**
- **Otro sentido:** si además quieres `monitor.getActividades()`, en `Monitor` se añade `@OneToMany(mappedBy = "monitor")`. `mappedBy` indica que no hay columna nueva: el dueño de la relación es `Actividad.monitor`.
- **Carga:** `fetch = FetchType.EAGER` carga el objeto relacionado a la vez; `LAZY` lo carga solo cuando se usa (es aquí donde actúan los proxies del apartado 2).
- **Bucle JSON:** con relaciones en los dos sentidos, al devolver JSON hay que usar `@JsonIgnore` o un DTO (ver Caso 4).

## 4. Sesiones y objetos Hibernate — estados

Hibernate usa el objeto **`Session`** para interactuar con la base de datos. Una instancia de una clase persistente puede estar en uno de estos tres estados:

| Estado | Descripción |
|---|---|
| **Transient** | Objeto recién creado con `new`. No está asociado a ninguna `Session` ni tiene representación en base de datos. |
| **Persistent** | El objeto se asocia a una `Session` (p. ej. tras un `save()`/`persist()`, o al recuperarlo con `get()`). Tiene identificador y fila asociada en BBDD; Hibernate sincroniza sus cambios. |
| **Detached** | La `Session` que lo gestionaba se ha cerrado. El objeto sigue teniendo los datos, pero ya no está sincronizado automáticamente con la base de datos. |

```
new Customer()  →  [TRANSIENT]
     │  session.save(c)
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
Customer c = session.get(Customer.class, 1);
```

**Guardar (`persist` vs. `save`):**
```java
session.persist(new Customer("Ana", "López", 200)); // void, no devuelve nada
Serializable id = session.save(new Customer("Ana", "López", 200)); // devuelve el id generado
```

**Actualizar (`update` vs. `merge`):**
```java
c.setFirstName("Ana María");
session.update(c);  // necesita que no haya ya otra instancia de la misma fila en sesión, o lanza excepción
session.merge(c);   // hace lo mismo, pero gestiona ese conflicto internamente
```

**Eliminar:**
```java
session.delete(c);
```

**Guardar o actualizar automáticamente:**
```java
session.saveOrUpdate(c); // insert si no existe en BBDD, update si ya existe
```

## 7. Consultas: HQL y SQL nativo

**HQL** (*Hibernate Query Language*) es un lenguaje orientado a objetos muy parecido a SQL, pero en vez de operar sobre tablas/columnas, opera sobre **entidades y sus propiedades**. Hibernate traduce internamente cada consulta HQL a SQL antes de ejecutarla.

```java
String hql = "FROM Customer";
Query consulta = session.createQuery(hql);
consulta.setFirstResult(0);
consulta.setMaxResults(40);
List<Customer> results = consulta.list();
```

Se recomienda HQL frente a SQL nativo porque es independiente del motor de base de datos concreto (portabilidad) y aprovecha la caché de Hibernate.

**Update con parámetros con nombre:**
```java
Query q = session.createQuery(
    "update Customer set customerNumber = :num where id = :id and firstName = :name");
q.setParameter("num", 25);
q.setParameter("id", 105);
q.setParameter("name", "Pepe");

int status = q.executeUpdate();
transaction.commit();
```

**SQL nativo** (cuando necesitamos algo específico del motor, como un procedimiento almacenado):
```java
Query q = session.createNativeQuery("SELECT * FROM CUSTOMER WHERE customer_number > :num"); // en Hibernate 5: createSQLQuery
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
