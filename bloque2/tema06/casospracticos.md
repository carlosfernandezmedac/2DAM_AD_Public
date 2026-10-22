# Casos Prácticos — 6. Manejo de Conectores III: sentencias

---

# PARTE 1 · Práctica guiada en MySQL Workbench

---

## Paso 1 · El diseño: diagrama E/R

Una tienda de informática necesita tres **entidades**: **CLIENTE** (quién compra), **PRODUCTO** (lo que se vende) y **PROVEEDOR** (a quién le compra la tienda). Y dos **relaciones muchos a muchos (N:M)**, las dos con atributos propios:

| Relación | Cardinalidad | Atributos de la relación | Cómo se guarda |
|---|---|---|---|
| El cliente **compra** productos: un cliente compra muchos productos y un producto lo compran muchos clientes | **N : M** | `fecha`, `cantidad` | Tabla intermedia `venta` con `id_cliente`, `id_producto`, `fecha` y `cantidad` |
| El proveedor **suministra** productos: un proveedor suministra muchos productos y un producto lo pueden suministrar varios proveedores | **N : M** | `fecha`, `cantidad`, `coste` | Tabla intermedia `compra` con `id_proveedor`, `id_producto`, `fecha`, `cantidad` y `coste` |

Una relación N:M **nunca** se guarda con una columna en una de las dos tablas (habría que meter varios valores en una celda): siempre se convierte en una **tabla intermedia** con las dos claves foráneas y los atributos de la relación.

---

## Paso 2 · Crear la base de datos y las tablas (`CREATE DATABASE`, `CREATE TABLE`)  

**Del tema:** `CREATE DATABASE` crea una base de datos nueva; `CREATE TABLE` crea una tabla y da error si ya existe, salvo que usemos `IF NOT EXISTS`.

```sql
DROP DATABASE IF EXISTS tienda;
CREATE DATABASE IF NOT EXISTS tienda;
USE tienda;
```

Primero las tres tablas que **no dependen de ninguna otra**:

```sql
CREATE TABLE cliente (
    id_cliente INT AUTO_INCREMENT PRIMARY KEY,
    nombre     VARCHAR(30)  NOT NULL,
    direccion  VARCHAR(100) NOT NULL,
    ciudad     VARCHAR(30)  NOT NULL,
    email      VARCHAR(100)
);

CREATE TABLE producto (
    id_producto INT AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(60) NOT NULL,
    categoria   VARCHAR(30),
    marca       VARCHAR(30) NOT NULL,
    precio      DECIMAL(8,2) NOT NULL,
    stock       INT DEFAULT 0
);

CREATE TABLE proveedor (
    id_proveedor INT AUTO_INCREMENT PRIMARY KEY,
    nombre       VARCHAR(60) NOT NULL,
    ciudad       VARCHAR(60) NOT NULL,
    telefono     VARCHAR(15)
);
```

Ahora las dos tablas que **dependen de las anteriores** (las tablas intermedias de las N:M). Llevan **claves foráneas** (`FOREIGN KEY`), que obligan a que el valor exista en la otra tabla:

```sql
CREATE TABLE compra (
    id_compra    INT AUTO_INCREMENT PRIMARY KEY,
    id_proveedor INT NOT NULL,
    id_producto  INT NOT NULL,
    fecha        DATE NOT NULL,
    cantidad     INT NOT NULL,
    coste        DECIMAL(8,2) NOT NULL,
    FOREIGN KEY (id_proveedor) REFERENCES proveedor (id_proveedor),
    FOREIGN KEY (id_producto)  REFERENCES producto (id_producto)
);

CREATE TABLE venta (
    id_venta    INT AUTO_INCREMENT PRIMARY KEY,
    id_cliente  INT NOT NULL,
    id_producto INT NOT NULL,
    fecha       DATE NOT NULL,
    cantidad    INT NOT NULL,
    FOREIGN KEY (id_cliente)  REFERENCES cliente (id_cliente),
    FOREIGN KEY (id_producto) REFERENCES producto (id_producto)
);
```

Comprobamos qué se ha creado:

```sql
SHOW TABLES;
DESCRIBE cliente;
```

---

## Paso 3 · Modificar la estructura (`ALTER TABLE`, `ALTER DATABASE`) e índices

**Del tema:** `ALTER TABLE` permite agregar, borrar y renombrar columnas, y cambiar su tipo. `CREATE INDEX` y `DROP INDEX` añaden y quitan índices. `ALTER DATABASE` cambia las características generales de la base de datos.

Añadimos una columna `telefono` a `cliente`, le cambiamos el nombre, el tipo, y la quitamos:

```sql
ALTER TABLE cliente ADD COLUMN telefono VARCHAR(9);
ALTER TABLE cliente RENAME COLUMN telefono TO movil;
ALTER TABLE cliente MODIFY COLUMN movil VARCHAR(15);
DESCRIBE cliente;
ALTER TABLE cliente DROP COLUMN movil;
DESCRIBE cliente;
```

| Sentencia | Qué hace |
|---|---|
| `ADD COLUMN` | Añade una columna nueva al final. |
| `RENAME COLUMN a TO b` | Cambia el nombre de la columna. (En MySQL 5.7 no existe: se usa `CHANGE COLUMN`.) |
| `MODIFY COLUMN` | Cambia el tipo (o las restricciones) de una columna. |
| `DROP COLUMN` | La borra, **con todos los datos que tuviera**. |

Un **índice** hace más rápidas las búsquedas por una columna:

```sql
CREATE INDEX idx_ciudad ON cliente (ciudad);
SHOW INDEX FROM cliente;
DROP INDEX idx_ciudad ON cliente;
SHOW INDEX FROM cliente;
```

> `SHOW INDEX` saca una tabla muy ancha. Lo importante es la columna `Key_name`: tras el `CREATE INDEX` aparecen dos (`PRIMARY` e `idx_ciudad`) y tras el `DROP INDEX` vuelve a quedar solo `PRIMARY`. La clave primaria ya crea su propio índice automáticamente.

---

## Paso 4 · Insertar datos (`INSERT`)  ·  *ya hecho en clase (primera parte)*

`INSERT INTO tabla (columnas) VALUES (valores)` agrega filas. El orden de las columnas **tiene que coincidir** con el orden de los valores. Los textos y las fechas van entre comillas simples; los números, sin comillas.

Una fila (observa que no indicamos `id_cliente`: lo pone `AUTO_INCREMENT`):

```sql
INSERT INTO cliente (nombre, direccion, ciudad, email)
VALUES ('Juan Lopez', 'C/Andalucia nº 2', 'Badajoz', 'juan.lopez@davante.org');

```

Varias filas a la vez (se separan con comas): los cuatro proveedores, dos productos y dos ventas.

```sql
INSERT INTO proveedor (nombre, ciudad, telefono) VALUES
 ('HP',            'Madrid',    '915000001'),
 ('Lenovo',        'Barcelona', '934000002'),
 ('Logitech',      'Valencia',  '963000003'),
 ('TechMayorista', 'Malaga',    '952000004');

INSERT INTO producto (nombre, categoria, marca, precio, stock) VALUES
 ('Portatil HP 15',          'Portatiles', 'HP',     549.00, 15),
 ('Portatil Lenovo IdeaPad', 'Portatiles', 'Lenovo', 479.00, 20);

INSERT INTO venta (id_cliente, id_producto, fecha, cantidad) VALUES
 (1, 1, '2026-09-01', 1),
 (1, 2, '2026-09-01', 1);

```

**Prueba los errores más habituales** (cada sentencia da un error distinto; léelos):

```sql
-- 1) Menos valores que columnas:
INSERT INTO cliente VALUES ('x', 'y', 'z');

-- 2) Un texto donde va un número:
INSERT INTO producto (nombre, marca, precio) VALUES ('Raton', 'Logitech', 'barato');

-- 3) Clave foránea inexistente (el cliente 99 no existe):
INSERT INTO venta (id_cliente, id_producto, fecha, cantidad) VALUES (99, 1, '2026-10-10', 1);

-- 4) Falta una columna obligatoria (NOT NULL):
INSERT INTO cliente (nombre, email) VALUES ('Marta', 'marta@davante.org');
```

**Resultado:**

```text
ERROR 1136 (21S01): Column count doesn't match value count at row 1
ERROR 1366 (HY000): Incorrect decimal value: 'barato' for column 'precio' at row 1
ERROR 1452 (23000): Cannot add or update a child row: a foreign key constraint fails (`tienda`.`venta`, CONSTRAINT `venta_ibfk_1` FOREIGN KEY (`id_cliente`) REFERENCES `cliente` (`id_cliente`))
ERROR 1364 (HY000): Field 'direccion' doesn't have a default value
```


El tercero es el importante: la base de datos **protege la integridad** y no deja crear una venta de un cliente que no existe.

---

## Paso 5 · Insertamos el resto de los datos  ·  *a partir de aquí seguimos*

Para no perder tiempo tecleando, **copia este bloque entero** (también está en `tienda_datos_restantes.sql`) y ejecútalo. Añade 8 clientes más, 6 productos más, el resto de las ventas y las compras a proveedores. Fíjate en que **primero** van las tablas "padre" (`cliente`, `producto`, `proveedor`) y **después** las que apuntan a ellas (`venta`, `compra`):

```sql
INSERT INTO cliente (nombre, direccion, ciudad, email) VALUES
 ('Pepe Tejada',      'Avda. de Sevilla 12',  'Sevilla',   'pepe.tejada@davante.org'),
 ('Victor Fernandez', 'C/Mayor 5',            'Jaen',      NULL),
 ('Jose Assensio',    'C/Huesca 8',           'Huesca',    'jose.assensio@davante.org'),
 ('Leo Perez',        'Plaza Mayor 1',        'Orense',    NULL),
 ('Patricia Aranda',  'Paseo de Gracia 20',   'Barcelona', 'patricia.aranda@davante.org'),
 ('David Lopez',      'Gran Via 45',          'Madrid',    'david.lopez@davante.org'),
 ('Enrique Ferreras', 'C/Larios 3',           'Malaga',    'enrique.ferreras@davante.org'),
 ('Luis Guzman',    'C/Colon 18',           'Valencia',  NULL);

INSERT INTO producto (nombre, categoria, marca, precio, stock) VALUES
 ('Monitor Samsung 24',    'Monitores',      'Samsung',  139.90,  30),
 ('Raton Logitech M185',   'Perifericos',    'Logitech',  14.90, 100),
 ('Teclado Logitech K120', 'Perifericos',    'Logitech',  12.50,  80),
 ('Impresora HP DeskJet',  'Impresoras',     'HP',        69.00,  25),
 ('Disco SSD 1TB',         'Almacenamiento', 'Kingston',  74.90,  45),
 ('Pendrive 64GB',         'Almacenamiento', 'Kingston',   8.90, 150);

INSERT INTO venta (id_cliente, id_producto, fecha, cantidad) VALUES
 (2, 3, '2026-09-01', 2),
 (2, 5, '2026-09-01', 1),
 (1, 7, '2026-09-15', 1),
 (1, 6, '2026-09-15', 1),
 (5, 2, '2026-09-20', 1),
 (5, 4, '2026-09-20', 1),
 (5, 5, '2026-09-20', 1),
 (7, 6, '2026-10-01', 2),
 (7, 8, '2026-10-01', 3),
 (2, 4, '2026-10-05', 2),
 (2, 7, '2026-10-05', 1);

INSERT INTO compra (id_proveedor, id_producto, fecha, cantidad, coste) VALUES
 (1, 1, '2026-08-25',  20, 430.00),
 (1, 6, '2026-08-25',  30,  48.00),
 (2, 2, '2026-08-26',  25, 380.00),
 (3, 4, '2026-08-27', 120,   9.50),
 (3, 5, '2026-08-27', 100,   7.80),
 (4, 3, '2026-08-28',  40, 105.00),
 (4, 4, '2026-08-28',  50,  10.20),
 (4, 7, '2026-08-29',  60,  58.00),
 (4, 8, '2026-08-29', 200,   5.20);
```


Comprobamos que todo se ha guardado correctamente:

```sql
SELECT * FROM cliente;
SELECT * FROM producto;
SELECT COUNT(*) AS ventas FROM venta;
SELECT COUNT(*) AS compras FROM compra;
```

**Resultado:**

```text
+------------+------------------+---------------------+-----------+------------------------------+
| id_cliente | nombre           | direccion           | ciudad    | email                        |
+------------+------------------+---------------------+-----------+------------------------------+
|          1 | Juan Lopez       | C/Andalucia nº 2    | Badajoz   | juan.lopez@davante.org       |
|          2 | Pepe Tejada      | Avda. de Sevilla 12 | Sevilla   | pepe.tejada@davante.org      |
|          3 | Victor Fernandez | C/Mayor 5           | Jaen      | NULL                         |
|          4 | Jose Assensio    | C/Huesca 8          | Huesca    | jose.assensio@davante.org    |
|          5 | Leo Perez        | Plaza Mayor 1       | Orense    | NULL                         |
|          6 | Patricia Aranda  | Paseo de Gracia 20  | Barcelona | patricia.aranda@davante.org  |
|          7 | David Lopez      | Gran Via 45         | Madrid    | david.lopez@davante.org      |
|          8 | Enrique Ferreras | C/Larios 3          | Malaga    | enrique.ferreras@davante.org |
|          9 | Luis Guzman      | C/Colon 18          | Valencia  | NULL                         |
+------------+------------------+---------------------+-----------+------------------------------+
+-------------+-------------------------+----------------+----------+--------+-------+
| id_producto | nombre                  | categoria      | marca    | precio | stock |
+-------------+-------------------------+----------------+----------+--------+-------+
|           1 | Portatil HP 15          | Portatiles     | HP       | 549.00 |    15 |
|           2 | Portatil Lenovo IdeaPad | Portatiles     | Lenovo   | 479.00 |    20 |
|           3 | Monitor Samsung 24      | Monitores      | Samsung  | 139.90 |    30 |
|           4 | Raton Logitech M185     | Perifericos    | Logitech |  14.90 |   100 |
|           5 | Teclado Logitech K120   | Perifericos    | Logitech |  12.50 |    80 |
|           6 | Impresora HP DeskJet    | Impresoras     | HP       |  69.00 |    25 |
|           7 | Disco SSD 1TB           | Almacenamiento | Kingston |  74.90 |    45 |
|           8 | Pendrive 64GB           | Almacenamiento | Kingston |   8.90 |   150 |
+-------------+-------------------------+----------------+----------+--------+-------+
+--------+
| ventas |
+--------+
|     13 |
+--------+
+---------+
| compras |
+---------+
|       9 |
+---------+
```


> Mira el **Ratón Logitech** (`id_producto` 4): lo han comprado varios clientes, y se lo hemos comprado a **dos proveedores distintos** (el 3 y el 4). Eso es una relación muchos a muchos funcionando.

---

## Paso 6 · "Insertando datos más datos"

**Planteamiento (del tema):** hay que introducir **dos nuevas filas** en la tabla de clientes, con distinto nombre, que pertenezcan a Málaga y a Granada respectivamente.

**Nudo:** es una sentencia de manipulación de datos: usamos `INSERT`.

```sql
INSERT INTO cliente (nombre, direccion, ciudad, email)
VALUES ('Ana Ruiz', 'C/Sol 7', 'Malaga', 'ana.ruiz@davante.org');

INSERT INTO cliente (nombre, direccion, ciudad, email)
VALUES ('Luis Soto', 'C/Luna 9', 'Granada', 'luis.soto@davante.org');

SELECT id_cliente, nombre, ciudad FROM cliente WHERE id_cliente > 8;
```

**Resultado:**

```text
+------------+---------------+----------+
| id_cliente | nombre        | ciudad   |
+------------+---------------+----------+
|          9 | Luis Guzman   | Valencia |
|         10 | Ana Ruiz      | Malaga   |
|         11 | Luis Soto     | Granada  |
+------------+---------------+----------+
```


Si has seguido los pasos, Ana y Luis reciben los ids 10 y 11 (míralo en la salida). Los vamos a necesitar en el paso 9.

---

## Paso 7 · Modificar datos (`UPDATE`)

`UPDATE tabla SET columna = valor WHERE condición` cambia las columnas indicadas en `SET` de las filas que cumplen el `WHERE`. **Si no hay `WHERE`, se modifican TODAS las filas.**

Un cliente se muda de ciudad:

```sql
UPDATE cliente SET ciudad = 'Cordoba' WHERE id_cliente = 3;
SELECT id_cliente, nombre, ciudad FROM cliente WHERE id_cliente = 3;
```

**Resultado:**

```text
+------------+------------------+---------+
| id_cliente | nombre           | ciudad  |
+------------+------------------+---------+
|          3 | Victor Fernandez | Cordoba |
+------------+------------------+---------+
```


Se pueden cambiar varias columnas a la vez, y usar el valor anterior en la expresión. Llegan 5 portátiles HP más y rebajamos su precio; y subimos un 10 % el precio del ratón:

```sql
UPDATE producto SET stock = stock + 5, precio = 529.00 WHERE id_producto = 1;
UPDATE producto SET precio = precio * 1.10 WHERE id_producto = 4;
SELECT id_producto, nombre, precio, stock FROM producto WHERE id_producto IN (1, 4);
```

**Resultado:**

```text
+-------------+---------------------+--------+-------+
| id_producto | nombre              | precio | stock |
+-------------+---------------------+--------+-------+
|           1 | Portatil HP 15      | 529.00 |    20 |
|           4 | Raton Logitech M185 |  16.39 |   100 |
+-------------+---------------------+--------+-------+
```


### Importante: el modo seguro de Workbench (error 1175)

Workbench trae activado por defecto el **modo seguro** (*safe update mode*): no deja ejecutar un `UPDATE` o un `DELETE` si el `WHERE` no usa una **clave** (como el `id`). Prueba a modificar usando una columna que no es clave:

```sql
UPDATE cliente SET ciudad = 'Cordoba' WHERE ciudad = 'Jaen';
```

**Resultado:**

```text
ERROR 1175 (HY000): You are using safe update mode and you tried to update a table without a WHERE that uses a KEY column.
```


En Workbench lo verás como **`Error Code: 1175. You are using safe update mode and you tried to update a table without a WHERE that uses a KEY column.`** Es una protección contra el típico desastre de olvidar el `WHERE`. Tienes tres opciones:

1. Usar la clave en el `WHERE`: `WHERE id_cliente = 3` (la forma recomendada).
2. Desactivarlo solo en tu sesión: `SET SQL_SAFE_UPDATES = 0;` (y volver a activarlo después con `= 1`).
3. Desactivarlo del todo: **Edit → Preferences → SQL Editor → Safe Updates**, y reconectar.

Dejamos a Víctor como estaba y seguimos:

```sql
UPDATE cliente SET ciudad = 'Jaen' WHERE id_cliente = 3;
SELECT id_cliente, nombre, ciudad FROM cliente WHERE id_cliente = 3;
```

**Resultado:**

```text
+------------+------------------+--------+
| id_cliente | nombre           | ciudad |
+------------+------------------+--------+
|          3 | Victor Fernandez | Jaen   |
+------------+------------------+--------+
```

---

## Paso 8 · `REPLACE`

`REPLACE` hace lo mismo que `INSERT`, con una excepción: si ya existe una fila con la misma `PRIMARY KEY` (o un índice `UNIQUE`), **borra la anterior y escribe la nueva**. Solo tiene sentido en tablas con ese tipo de índices.

Un producto nuevo (el 9 no existe todavía), que `REPLACE` inserta como un `INSERT` normal:

```sql
REPLACE INTO producto (id_producto, nombre, categoria, marca, precio, stock)
VALUES (9, 'Alfombrilla raton', 'Perifericos', 'Logitech', 4.90, 60);

SELECT * FROM producto WHERE id_producto = 9;
```

**Resultado:**

```text
+-------------+-------------------+-------------+----------+--------+-------+
| id_producto | nombre            | categoria   | marca    | precio | stock |
+-------------+-------------------+-------------+----------+--------+-------+
|           9 | Alfombrilla raton | Perifericos | Logitech |   4.90 |    60 |
+-------------+-------------------+-------------+----------+--------+-------+
```


Ahora lo repetimos con el **mismo id**. Como el 9 ya existe, `REPLACE` borra la fila antigua y escribe la nueva (aquí no indicamos el `stock` a propósito):

```sql
REPLACE INTO producto (id_producto, nombre, categoria, marca, precio)
VALUES (9, 'Alfombrilla raton XL', 'Perifericos', 'Logitech', 9.90);

SELECT * FROM producto WHERE id_producto = 9;
```

**Resultado:**

```text
+-------------+----------------------+-------------+----------+--------+-------+
| id_producto | nombre               | categoria   | marca    | precio | stock |
+-------------+----------------------+-------------+----------+--------+-------+
|           9 | Alfombrilla raton XL | Perifericos | Logitech |   9.90 |     0 |
+-------------+----------------------+-------------+----------+--------+-------+
```


Fíjate: el `stock` ya **no es 60**, ha vuelto a su valor por defecto (0). `REPLACE` no actualiza solo las columnas que indicas: **borra la fila y la crea de nuevo**. Para cambiar un dato suelto, mejor `UPDATE`.

**Y hay otro peligro**: como `REPLACE` borra la fila antigua, no funciona si otras tablas dependen de ella. El producto 4 (Ratón Logitech) aparece en ventas y compras:

```sql
REPLACE INTO producto (id_producto, nombre, categoria, marca, precio, stock)
VALUES (4, 'Raton Logitech M185 negro', 'Perifericos', 'Logitech', 15.90, 100);
```

**Resultado:**

```text
ERROR 1451 (23000): Cannot delete or update a parent row: a foreign key constraint fails (`tienda`.`compra`, CONSTRAINT `compra_ibfk_2` FOREIGN KEY (`id_producto`) REFERENCES `producto` (`id_producto`))
```


Da el error `1451` (clave foránea): no se puede borrar un producto que tiene ventas y compras.

---

## Paso 9 · Borrar filas (`DELETE`)

`DELETE FROM tabla WHERE condición` borra las filas que cumplen la condición y devuelve cuántas ha eliminado. **Si no hay `WHERE`, se borran todas las filas de la tabla.**

Borramos los dos clientes del paso 6 (Ana y Luis, que tienen los ids 10 y 11):

```sql
DELETE FROM cliente WHERE id_cliente IN (10, 11);
SELECT COUNT(*) AS clientes FROM cliente;
```

**Resultado:**

```text
+----------+
| clientes |
+----------+
|        9 |
+----------+
```


Ahora intenta borrar un cliente **que tiene ventas** (el cliente 1, Juan):

```sql
DELETE FROM cliente WHERE id_cliente = 1;
```

**Resultado:**

```text
ERROR 1451 (23000): Cannot delete or update a parent row: a foreign key constraint fails (`tienda`.`venta`, CONSTRAINT `venta_ibfk_1` FOREIGN KEY (`id_cliente`) REFERENCES `cliente` (`id_cliente`))
```


Error `1451 Cannot delete or update a parent row: a foreign key constraint fails`. La base de datos no deja borrar un cliente mientras tenga ventas que dependan de él. Para borrarlo habría que borrar antes sus ventas.

Por último, borramos la alfombrilla (producto 9) que añadimos con `REPLACE`, porque no la usa ninguna venta:

```sql
DELETE FROM producto WHERE id_producto = 9;
SELECT COUNT(*) AS productos FROM producto;
```

**Resultado:**

```text
+-----------+
| productos |
+-----------+
|         8 |
+-----------+
```


---

## Paso 10 · Consultas: `SELECT` y `FROM`

Una consulta es **una pregunta a la base de datos con una serie de criterios**. El resultado es una **tabla ficticia** que solo existe en memoria mientras la miras: no se guarda en disco.

```sql
SELECT * FROM cliente;
SELECT nombre, ciudad FROM cliente;
SELECT nombre AS Producto, precio AS Precio FROM producto;
```

`ALL` (por defecto) muestra todas las filas, aunque se repitan; `DISTINCT` elimina las repetidas:

```sql
SELECT marca FROM producto;
SELECT DISTINCT marca FROM producto;
SELECT DISTINCT categoria FROM producto;
```

---

## Paso 11 · `WHERE`: operadores y nulos

`WHERE` filtra las filas. Operadores de comparación: `>`, `>=`, `<`, `<=`, `=`, `<>` (o `!=`).

```sql
SELECT nombre, precio FROM producto WHERE precio > 100;
SELECT nombre, precio FROM producto WHERE precio >= 74.90;
SELECT nombre, precio FROM producto WHERE precio < 15;
SELECT nombre, marca FROM producto WHERE marca = 'HP';
SELECT nombre, marca FROM producto WHERE marca <> 'HP';
```

**Nulos:** para comprobar si un valor es nulo se usa `IS NULL` / `IS NOT NULL`. **No se puede usar `= NULL`**:

```sql
SELECT id_cliente, nombre, email FROM cliente WHERE email IS NULL;
SELECT id_cliente, nombre, email FROM cliente WHERE email = NULL;
SELECT id_cliente, nombre, email FROM cliente WHERE email IS NOT NULL;
```

La segunda consulta **no da error, pero devuelve cero filas**: `NULL` no es igual a nada, ni siquiera a sí mismo. Es el fallo más típico con los nulos. (Un `email` en blanco `''` **no** es nulo: nulo significa que no existe valor.)

---

## Paso 12 · `WHERE`: `LIKE`, `BETWEEN` e `IN`

- `LIKE` compara con un patrón. `%` = cualquier cadena de caracteres (también vacía); `_` = exactamente un carácter.
- `BETWEEN a AND b` = rango, **incluyendo** los dos extremos.
- `IN (...)` = el valor coincide con alguno de la lista.

```sql
SELECT nombre FROM cliente WHERE nombre LIKE 'P%';       -- empieza por P
SELECT nombre FROM cliente WHERE nombre LIKE '%Lopez';   -- termina en Lopez
SELECT nombre FROM cliente WHERE nombre LIKE '%er%';     -- contiene "er"
SELECT nombre FROM cliente WHERE nombre LIKE '_e%';      -- segunda letra: e
SELECT nombre, email FROM cliente WHERE email LIKE '%@davante.org';
```

```sql
SELECT nombre, precio FROM producto WHERE nombre LIKE 'Portatil%';
SELECT nombre, marca FROM producto WHERE nombre LIKE '%Logitech%';
SELECT nombre, precio FROM producto WHERE precio BETWEEN 10 AND 100;
SELECT nombre, marca FROM producto WHERE marca IN ('HP', 'Kingston');
SELECT nombre, categoria FROM producto WHERE categoria IN ('Monitores', 'Impresoras');
```

---

## Paso 13 · Combinar condiciones: `AND`, `OR`, `NOT` y paréntesis

Las condiciones se combinan con `AND` (y), `OR` (o) y `NOT` (negación), y los **paréntesis** establecen prioridades.

```sql
SELECT nombre, precio FROM producto WHERE precio > 20 AND precio < 150;
SELECT nombre, marca FROM producto WHERE marca = 'HP' OR marca = 'Lenovo';
SELECT nombre, categoria FROM producto WHERE NOT categoria = 'Portatiles';
```

**Los paréntesis cambian el resultado.** Estas dos consultas solo se diferencian en los paréntesis:

```sql
-- Sin paréntesis: AND se evalúa antes que OR
SELECT nombre, marca, precio FROM producto
WHERE marca = 'HP' OR marca = 'Logitech' AND precio < 20;

-- Con paréntesis: primero el OR
SELECT nombre, marca, precio FROM producto
WHERE (marca = 'HP' OR marca = 'Logitech') AND precio < 20;
```

**Resultado:**

```text
+-----------------------+----------+--------+
| nombre                | marca    | precio |
+-----------------------+----------+--------+
| Portatil HP 15        | HP       | 529.00 |
| Raton Logitech M185   | Logitech |  16.39 |
| Teclado Logitech K120 | Logitech |  12.50 |
| Impresora HP DeskJet  | HP       |  69.00 |
+-----------------------+----------+--------+
+-----------------------+----------+--------+
| nombre                | marca    | precio |
+-----------------------+----------+--------+
| Raton Logitech M185   | Logitech |  16.39 |
| Teclado Logitech K120 | Logitech |  12.50 |
+-----------------------+----------+--------+
```


La primera se lee como "marca HP, **o** (marca Logitech **y** menos de 20 euros)", y por eso salen los dos productos HP aunque cuesten mucho. La segunda es "(marca HP o Logitech) **y** menos de 20 euros". Ante la duda, usa paréntesis.

---

## Paso 14 · Ordenar con `ORDER BY` y limitar con `LIMIT`

**Del tema:** `ORDER BY` va **después del `WHERE`** y ordena el resultado por una o varias columnas. `ASC` es ascendente (de menor a mayor, por defecto) y `DESC` descendente.

```sql
SELECT nombre, precio FROM producto ORDER BY precio;
SELECT nombre, precio FROM producto ORDER BY precio DESC;
```

Se puede ordenar por varias columnas: primero por categoría y, dentro de cada una, del más barato al más caro:

```sql
SELECT categoria, nombre, precio FROM producto ORDER BY categoria ASC, precio ASC;
```

**Resultado:**

```text
+----------------+-------------------------+--------+
| categoria      | nombre                  | precio |
+----------------+-------------------------+--------+
| Almacenamiento | Pendrive 64GB           |   8.90 |
| Almacenamiento | Disco SSD 1TB           |  74.90 |
| Impresoras     | Impresora HP DeskJet    |  69.00 |
| Monitores      | Monitor Samsung 24      | 139.90 |
| Perifericos    | Teclado Logitech K120   |  12.50 |
| Perifericos    | Raton Logitech M185     |  16.39 |
| Portatiles     | Portatil Lenovo IdeaPad | 479.00 |
| Portatiles     | Portatil HP 15          | 529.00 |
+----------------+-------------------------+--------+
```


`LIMIT n` devuelve solo las `n` primeras filas. Junto con `ORDER BY` sirve para sacar "los 3 más...":

```sql
SELECT nombre, precio FROM producto ORDER BY precio DESC LIMIT 3;
SELECT nombre, ciudad FROM cliente ORDER BY nombre;
```

**Resultado:**

```text
+-------------------------+--------+
| nombre                  | precio |
+-------------------------+--------+
| Portatil HP 15          | 529.00 |
| Portatil Lenovo IdeaPad | 479.00 |
| Monitor Samsung 24      | 139.90 |
+-------------------------+--------+
+------------------+-----------+
| nombre           | ciudad    |
+------------------+-----------+
| David Lopez      | Madrid    |
| Enrique Ferreras | Malaga    |
| Jose Assensio    | Huesca    |
| Juan Lopez       | Badajoz   |
| Leo Perez        | Orense    |
| Luis Guzman      | Valencia  |
| Patricia Aranda  | Barcelona |
| Pepe Tejada      | Sevilla   |
| Victor Fernandez | Jaen      |
+------------------+-----------+
```


---

## Paso 15 · Casos prácticos 

**Planteamiento:** se quiere una lista de los productos que **cuesten más de 100 euros**, ordenada **alfabéticamente por nombre**.

```sql
SELECT *
FROM producto
WHERE precio > 100
ORDER BY nombre ASC;
```

**Resultado:**

```text
+-------------+-------------------------+------------+---------+--------+-------+
| id_producto | nombre                  | categoria  | marca   | precio | stock |
+-------------+-------------------------+------------+---------+--------+-------+
|           3 | Monitor Samsung 24      | Monitores  | Samsung | 139.90 |    30 |
|           1 | Portatil HP 15          | Portatiles | HP      | 529.00 |    20 |
|           2 | Portatil Lenovo IdeaPad | Portatiles | Lenovo  | 479.00 |    20 |
+-------------+-------------------------+------------+---------+--------+-------+
```


**Planteamiento:** nos encargan una consulta sobre los productos con **precio entre 10 y 150 euros**, con los resultados en **orden alfabético inverso** (descendente) y seleccionando solo los campos `nombre`, `marca` y `precio`.

```sql
SELECT nombre, marca, precio
FROM producto
WHERE precio BETWEEN 10 AND 150
ORDER BY nombre DESC;
```

**Resultado:**

```text
+-----------------------+----------+--------+
| nombre                | marca    | precio |
+-----------------------+----------+--------+
| Teclado Logitech K120 | Logitech |  12.50 |
| Raton Logitech M185   | Logitech |  16.39 |
| Monitor Samsung 24    | Samsung  | 139.90 |
| Impresora HP DeskJet  | HP       |  69.00 |
| Disco SSD 1TB         | Kingston |  74.90 |
+-----------------------+----------+--------+
```

---

## Paso 16 · Ampliación: consultar varias tablas (`JOIN`)

> Tablas intermedias: sin `JOIN` no podríamos preguntar "¿qué ha comprado cada cliente?"

`JOIN ... ON` une dos tablas emparejando la clave foránea con la clave primaria. En `venta` hacen falta **dos** `JOIN`: uno con `cliente` y otro con `producto`.

**Todas las ventas con el nombre del cliente y del producto, y su importe:**

```sql
SELECT v.id_venta, v.fecha, c.nombre AS cliente, p.nombre AS producto,
       v.cantidad, v.cantidad * p.precio AS importe
FROM venta v
JOIN cliente c  ON v.id_cliente  = c.id_cliente
JOIN producto p ON v.id_producto = p.id_producto
ORDER BY v.id_venta;
```

**Resultado:**

```text
+----------+------------+-------------+-------------------------+----------+---------+
| id_venta | fecha      | cliente     | producto                | cantidad | importe |
+----------+------------+-------------+-------------------------+----------+---------+
|        1 | 2026-09-01 | Juan Lopez  | Portatil HP 15          |        1 |  529.00 |
|        2 | 2026-09-01 | Juan Lopez  | Portatil Lenovo IdeaPad |        1 |  479.00 |
|        4 | 2026-09-01 | Pepe Tejada | Monitor Samsung 24      |        2 |  279.80 |
|        5 | 2026-09-01 | Pepe Tejada | Teclado Logitech K120   |        1 |   12.50 |
|        6 | 2026-09-15 | Juan Lopez  | Disco SSD 1TB           |        1 |   74.90 |
|        7 | 2026-09-15 | Juan Lopez  | Impresora HP DeskJet    |        1 |   69.00 |
|        8 | 2026-09-20 | Leo Perez   | Portatil Lenovo IdeaPad |        1 |  479.00 |
|        9 | 2026-09-20 | Leo Perez   | Raton Logitech M185     |        1 |   16.39 |
|       10 | 2026-09-20 | Leo Perez   | Teclado Logitech K120   |        1 |   12.50 |
|       11 | 2026-10-01 | David Lopez | Impresora HP DeskJet    |        2 |  138.00 |
|       12 | 2026-10-01 | David Lopez | Pendrive 64GB           |        3 |   26.70 |
|       13 | 2026-10-05 | Pepe Tejada | Raton Logitech M185     |        2 |   32.78 |
|       14 | 2026-10-05 | Pepe Tejada | Disco SSD 1TB           |        1 |   74.90 |
+----------+------------+-------------+-------------------------+----------+---------+
```


**Qué ha comprado el cliente 1** (se añade un `WHERE`, como en cualquier consulta):

```sql
SELECT v.fecha, p.nombre AS producto, v.cantidad
FROM venta v
JOIN producto p ON v.id_producto = p.id_producto
WHERE v.id_cliente = 1
ORDER BY v.fecha;
```

**Resultado:**

```text
+------------+-------------------------+----------+
| fecha      | producto                | cantidad |
+------------+-------------------------+----------+
| 2026-09-01 | Portatil HP 15          |        1 |
| 2026-09-01 | Portatil Lenovo IdeaPad |        1 |
| 2026-09-15 | Disco SSD 1TB           |        1 |
| 2026-09-15 | Impresora HP DeskJet    |        1 |
+------------+-------------------------+----------+
```


**Dinero gastado por cada cliente** (suma de los importes, agrupando por cliente, de más a menos):

```sql
SELECT c.nombre, SUM(v.cantidad * p.precio) AS total_gastado
FROM venta v
JOIN cliente c  ON v.id_cliente  = c.id_cliente
JOIN producto p ON v.id_producto = p.id_producto
GROUP BY c.id_cliente, c.nombre
ORDER BY total_gastado DESC;
```

**Resultado:**

```text
+-------------+---------------+
| nombre      | total_gastado |
+-------------+---------------+
| Juan Lopez  |       1151.90 |
| Leo Perez   |        507.89 |
| Pepe Tejada |        399.98 |
| David Lopez |        164.70 |
+-------------+---------------+
```


**Qué proveedores suministran cada producto** (la otra N:M; el Ratón Logitech sale dos veces):

```sql
SELECT p.nombre AS producto, pr.nombre AS proveedor
FROM compra c
JOIN producto p   ON c.id_producto  = p.id_producto
JOIN proveedor pr ON c.id_proveedor = pr.id_proveedor
ORDER BY p.nombre, pr.nombre;
```

**Resultado:**

```text
+-------------------------+---------------+
| producto                | proveedor     |
+-------------------------+---------------+
| Disco SSD 1TB           | TechMayorista |
| Impresora HP DeskJet    | HP            |
| Monitor Samsung 24      | TechMayorista |
| Pendrive 64GB           | TechMayorista |
| Portatil HP 15          | HP            |
| Portatil Lenovo IdeaPad | Lenovo        |
| Raton Logitech M185     | Logitech      |
| Raton Logitech M185     | TechMayorista |
| Teclado Logitech K120   | Logitech      |
+-------------------------+---------------+
```


---

## Paso 17 · Transacciones (`COMMIT`, `ROLLBACK`, `SAVEPOINT`)

`COMMIT` guarda los cambios, `ROLLBACK` los deshace y `SAVEPOINT` marca un punto intermedio al que volver con `ROLLBACK TO`.

> **Autocommit.** Workbench trabaja por defecto con *autocommit* activado: cada sentencia se guarda en cuanto la ejecutas, como si llevara un `COMMIT`. Para agrupar sentencias hay que abrir la transacción a mano con `START TRANSACTION;`. (En la barra de herramientas hay un botón para activar o desactivar el autocommit.)

**Situación real:** el cliente 3 (Víctor) compra 1 portátil HP y 2 ratones Logitech. Para registrarlo hay que hacer **cosas que deben ir juntas**: apuntar las dos ventas y descontar el stock de los dos productos. Si falla una, no debe quedar ninguna. Primero, el estado antes de empezar:

```sql
SELECT id_producto, nombre, stock FROM producto WHERE id_producto IN (1, 4);
SELECT COUNT(*) AS ventas FROM venta;
```

**Resultado:**

```text
+-------------+---------------------+-------+
| id_producto | nombre              | stock |
+-------------+---------------------+-------+
|           1 | Portatil HP 15      |    20 |
|           4 | Raton Logitech M185 |   100 |
+-------------+---------------------+-------+
+--------+
| ventas |
+--------+
|     13 |
+--------+
```


Probamos con **`ROLLBACK`** (nos arrepentimos):

```sql
START TRANSACTION;

INSERT INTO venta (id_cliente, id_producto, fecha, cantidad) VALUES (3, 1, '2026-10-10', 1);
INSERT INTO venta (id_cliente, id_producto, fecha, cantidad) VALUES (3, 4, '2026-10-10', 2);
UPDATE producto SET stock = stock - 1 WHERE id_producto = 1;
UPDATE producto SET stock = stock - 2 WHERE id_producto = 4;

-- Dentro de la transacción vemos los cambios:
SELECT id_producto, nombre, stock FROM producto WHERE id_producto IN (1, 4);
SELECT COUNT(*) AS ventas FROM venta;

ROLLBACK;

-- Tras el ROLLBACK todo ha vuelto atrás:
SELECT id_producto, nombre, stock FROM producto WHERE id_producto IN (1, 4);
SELECT COUNT(*) AS ventas FROM venta;
```

**Resultado:**

```text
+-------------+---------------------+-------+
| id_producto | nombre              | stock |
+-------------+---------------------+-------+
|           1 | Portatil HP 15      |    19 |
|           4 | Raton Logitech M185 |    98 |
+-------------+---------------------+-------+
+--------+
| ventas |
+--------+
|     15 |
+--------+
+-------------+---------------------+-------+
| id_producto | nombre              | stock |
+-------------+---------------------+-------+
|           1 | Portatil HP 15      |    20 |
|           4 | Raton Logitech M185 |   100 |
+-------------+---------------------+-------+
+--------+
| ventas |
+--------+
|     13 |
+--------+
```


Ahora lo mismo, pero con **`COMMIT`** (la venta es buena y se guarda):

```sql
START TRANSACTION;

INSERT INTO venta (id_cliente, id_producto, fecha, cantidad) VALUES (3, 1, '2026-10-10', 1);
INSERT INTO venta (id_cliente, id_producto, fecha, cantidad) VALUES (3, 4, '2026-10-10', 2);
UPDATE producto SET stock = stock - 1 WHERE id_producto = 1;
UPDATE producto SET stock = stock - 2 WHERE id_producto = 4;

COMMIT;

SELECT id_producto, nombre, stock FROM producto WHERE id_producto IN (1, 4);
SELECT id_venta, id_cliente, id_producto, fecha, cantidad FROM venta ORDER BY id_venta DESC LIMIT 2;
```

**Resultado:**

```text
+-------------+---------------------+-------+
| id_producto | nombre              | stock |
+-------------+---------------------+-------+
|           1 | Portatil HP 15      |    19 |
|           4 | Raton Logitech M185 |    98 |
+-------------+---------------------+-------+
+----------+------------+-------------+------------+----------+
| id_venta | id_cliente | id_producto | fecha      | cantidad |
+----------+------------+-------------+------------+----------+
|       18 |          3 |           4 | 2026-10-10 |        2 |
|       17 |          3 |           1 | 2026-10-10 |        1 |
+----------+------------+-------------+------------+----------+
```


> Fíjate en los `id_venta` (17 y 18 en esta ejecución, no 14 y 15): el `ROLLBACK` deshizo las filas, pero **`AUTO_INCREMENT` no da marcha atrás**; los ids que se gastaron se pierden. Tus números pueden variar un poco según los errores que hayas provocado antes; es normal.

**`SAVEPOINT`**: un punto intermedio. Cambiamos el precio de dos productos y nos arrepentimos solo del segundo cambio (el del ratón):

```sql
START TRANSACTION;

UPDATE producto SET precio = 519.00 WHERE id_producto = 1;   -- Portatil HP
SAVEPOINT despues_del_portatil;
UPDATE producto SET precio = 149.90 WHERE id_producto = 4;   -- Raton (error de tecleo)

SELECT id_producto, nombre, precio FROM producto WHERE id_producto IN (1, 4);

ROLLBACK TO despues_del_portatil;                            -- deshace solo lo posterior al savepoint

SELECT id_producto, nombre, precio FROM producto WHERE id_producto IN (1, 4);

COMMIT;                                                       -- guardamos lo que queda (el precio del portatil)
```

**Resultado:**

```text
+-------------+---------------------+--------+
| id_producto | nombre              | precio |
+-------------+---------------------+--------+
|           1 | Portatil HP 15      | 519.00 |
|           4 | Raton Logitech M185 | 149.90 |
+-------------+---------------------+--------+
+-------------+---------------------+--------+
| id_producto | nombre              | precio |
+-------------+---------------------+--------+
|           1 | Portatil HP 15      | 519.00 |
|           4 | Raton Logitech M185 |  16.39 |
+-------------+---------------------+--------+
```


### Aislamiento (opcional, con dos conexiones)

Para ver el **aislamiento** necesitas dos conexiones a la vez: en Workbench, desde la pantalla de inicio abre la conexión una segunda vez (una pestaña por conexión).

1. En la **conexión A**: `START TRANSACTION; UPDATE cliente SET ciudad = 'Merida' WHERE id_cliente = 1;` y **no** hagas `COMMIT`. Si haces un `SELECT` aquí, ves `Merida`.
2. En la **conexión B**: `SELECT ciudad FROM cliente WHERE id_cliente = 1;` ve la ciudad **antigua** (`Badajoz`): el cambio de A no es visible hasta que se confirme.
3. En la **conexión A**: `ROLLBACK;`. Comprueba que en B la ciudad sigue siendo `Badajoz`.

(Con `COMMIT` en lugar de `ROLLBACK`, B vería `Merida` a partir de ese momento.)

### Qué sentencias no se pueden deshacer

Las sentencias de definición de datos (`CREATE TABLE`, `DROP TABLE`, `ALTER TABLE`...) y `TRUNCATE` **hacen `COMMIT` automáticamente**: no se pueden deshacer con `ROLLBACK`. Por eso hay que tener cuidado con ellas.

---

## Paso 18 · Copiar, renombrar, vaciar y borrar tablas

`RENAME TABLE`, `TRUNCATE TABLE`, `DROP TABLE`, `DO`.

Creamos una tabla de prueba con los productos caros (más de 100 euros) copiando la estructura y los datos de `producto`:

```sql
CREATE TABLE producto_caro LIKE producto;
INSERT INTO producto_caro SELECT * FROM producto WHERE precio > 100;
SELECT * FROM producto_caro;
```

**Resultado:**

```text
+-------------+-------------------------+------------+---------+--------+-------+
| id_producto | nombre                  | categoria  | marca   | precio | stock |
+-------------+-------------------------+------------+---------+--------+-------+
|           1 | Portatil HP 15          | Portatiles | HP      | 519.00 |    19 |
|           2 | Portatil Lenovo IdeaPad | Portatiles | Lenovo  | 479.00 |    20 |
|           3 | Monitor Samsung 24      | Monitores  | Samsung | 139.90 |    30 |
+-------------+-------------------------+------------+---------+--------+-------+
```


- `CREATE TABLE ... LIKE` copia solo la **estructura**.
- `INSERT INTO ... SELECT` inserta el **resultado de una consulta** en otra tabla (el `INSERT-SELECT` que nombra el tema).

Le cambiamos el nombre:

```sql
RENAME TABLE producto_caro TO productos_destacados;
SHOW TABLES;
```

**Resultado:**

```text
+----------------------+
| Tables_in_tienda     |
+----------------------+
| cliente              |
| compra               |
| producto             |
| productos_destacados |
| proveedor            |
| venta                |
+----------------------+
```


**`TRUNCATE TABLE`** vacía la tabla completa. Es parecido a un `DELETE` sin `WHERE`, pero más rápido, no se puede deshacer y reinicia el contador del `AUTO_INCREMENT`. Mira la diferencia con `DELETE`:

```sql
DELETE FROM productos_destacados WHERE id_producto > 0;
INSERT INTO productos_destacados (nombre, marca, precio) VALUES ('Prueba 1', 'HP', 5.00);
SELECT id_producto, nombre FROM productos_destacados;

TRUNCATE TABLE productos_destacados;
INSERT INTO productos_destacados (nombre, marca, precio) VALUES ('Prueba 2', 'HP', 5.00);
SELECT id_producto, nombre FROM productos_destacados;
```

**Resultado:**

```text
+-------------+----------+
| id_producto | nombre   |
+-------------+----------+
|           4 | Prueba 1 |
+-------------+----------+
+-------------+----------+
| id_producto | nombre   |
+-------------+----------+
|           1 | Prueba 2 |
+-------------+----------+
```


Tras el `DELETE`, el nuevo producto sigue la numeración de antes (el contador no se reinicia); tras el `TRUNCATE`, vuelve a empezar en 1.

**`DROP TABLE`** borra la tabla entera, con estructura y datos, y hace `COMMIT` automático. Con `IF EXISTS` evitamos el error si ya no existe:

```sql
DROP TABLE productos_destacados;
DROP TABLE productos_destacados;
DROP TABLE IF EXISTS productos_destacados;
SHOW TABLES;
```

**Resultado:**

```text
ERROR 1051 (42S02): Unknown table 'tienda.productos_destacados'
+------------------+
| Tables_in_tienda |
+------------------+
| cliente          |
| compra           |
| producto         |
| proveedor        |
| venta            |
+------------------+
```

El segundo `DROP` da el error `1051 Unknown table`; el tercero no da error.


> **No ejecutes `DROP DATABASE tienda;`** salvo que quieras borrar toda la práctica. Borra todas las tablas y la propia base de datos, y no se puede deshacer.

---

## Ejercicios propuestos

Hazlos tú en Workbench;

1. Muestra el nombre y el stock de los productos con **menos de 30 unidades**, ordenados de menos a más stock.
2. Muestra los clientes cuya **ciudad empiece por `M`**.
3. Muestra los clientes **sin email**, ordenados por nombre.
4. Muestra los productos de la **marca `Logitech` que cuesten menos de 14 euros**.
5. Sube un **10 %** el precio del teclado y del pendrive (ids 5 y 8) y comprueba el resultado.
6. Inserta un nuevo proveedor (`'Kingston'`, `'Sevilla'`, `'954000005'`) y una compra de **100 pendrives** (id 8) a ese proveedor, a **5.00** de coste, con fecha `'2026-10-12'`. (Pista: `LAST_INSERT_ID()` devuelve el último id generado con `AUTO_INCREMENT`; así reutilizas el id del proveedor recién creado.)
7. **(Ampliación)** Muestra, para cada proveedor, el **dinero total gastado** en compras (`cantidad * coste`), de mayor a menor.

---

## Soluciones

```sql
-- 1
SELECT nombre, stock FROM producto WHERE stock < 30 ORDER BY stock;
```

**Resultado:**

```text
+-------------------------+-------+
| nombre                  | stock |
+-------------------------+-------+
| Portatil HP 15          |    19 |
| Portatil Lenovo IdeaPad |    20 |
| Impresora HP DeskJet    |    25 |
+-------------------------+-------+
```


```sql
-- 2
SELECT * FROM cliente WHERE ciudad LIKE 'M%';
```

**Resultado:**

```text
+------------+------------------+-------------+--------+------------------------------+
| id_cliente | nombre           | direccion   | ciudad | email                        |
+------------+------------------+-------------+--------+------------------------------+
|          7 | David Lopez      | Gran Via 45 | Madrid | david.lopez@davante.org      |
|          8 | Enrique Ferreras | C/Larios 3  | Malaga | enrique.ferreras@davante.org |
+------------+------------------+-------------+--------+------------------------------+
```


```sql
-- 3
SELECT * FROM cliente WHERE email IS NULL ORDER BY nombre;
```

**Resultado:**

```text
+------------+------------------+---------------+----------+-------+
| id_cliente | nombre           | direccion     | ciudad   | email |
+------------+------------------+---------------+----------+-------+
|          5 | Leo Perez        | Plaza Mayor 1 | Orense   | NULL  |
|          9 | Seluis Guzman    | C/Colon 18    | Valencia | NULL  |
|          3 | Victor Fernandez | C/Mayor 5     | Jaen     | NULL  |
+------------+------------------+---------------+----------+-------+
```


```sql
-- 4
SELECT nombre, marca, precio FROM producto WHERE marca = 'Logitech' AND precio < 14;
```

**Resultado:**

```text
+-----------------------+----------+--------+
| nombre                | marca    | precio |
+-----------------------+----------+--------+
| Teclado Logitech K120 | Logitech |  12.50 |
+-----------------------+----------+--------+
```


```sql
-- 5
UPDATE producto SET precio = precio * 1.10 WHERE id_producto IN (5, 8);
SELECT id_producto, nombre, precio FROM producto WHERE id_producto IN (5, 8);
```

**Resultado:**

```text
+-------------+-----------------------+--------+
| id_producto | nombre                | precio |
+-------------+-----------------------+--------+
|           5 | Teclado Logitech K120 |  13.75 |
|           8 | Pendrive 64GB         |   9.79 |
+-------------+-----------------------+--------+
```


```sql
-- 6
INSERT INTO proveedor (nombre, ciudad, telefono) VALUES ('Kingston', 'Sevilla', '954000005');
INSERT INTO compra (id_proveedor, id_producto, fecha, cantidad, coste)
VALUES (LAST_INSERT_ID(), 8, '2026-10-12', 100, 5.00);
SELECT * FROM compra WHERE fecha = '2026-10-12';
```

**Resultado:**

```text
+-----------+--------------+-------------+------------+----------+-------+
| id_compra | id_proveedor | id_producto | fecha      | cantidad | coste |
+-----------+--------------+-------------+------------+----------+-------+
|        10 |            5 |           8 | 2026-10-12 |      100 |  5.00 |
+-----------+--------------+-------------+------------+----------+-------+
```


```sql
-- 7
SELECT pr.nombre AS proveedor, SUM(c.cantidad * c.coste) AS total_gastado
FROM compra c
JOIN proveedor pr ON c.id_proveedor = pr.id_proveedor
GROUP BY pr.nombre
ORDER BY total_gastado DESC;
```

**Resultado:**

```text
+---------------+---------------+
| proveedor     | total_gastado |
+---------------+---------------+
| HP            |      10040.00 |
| Lenovo        |       9500.00 |
| TechMayorista |       9230.00 |
| Logitech      |       1920.00 |
| Kingston      |        500.00 |
+---------------+---------------+
```

