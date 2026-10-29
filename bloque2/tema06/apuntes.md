# Tema 6 — Manejo de Conectores III: Sentencias

---

## Índice

1. [Introducción](#1-introducción)
2. [Sentencias de definición de datos (DDL)](#3-sentencias-de-definición-de-datos-ddl)
3. [Sentencias de manipulación de datos (DML)](#4-sentencias-de-manipulación-de-datos-dml)
4. [Consultas: SELECT, WHERE y ORDER BY](#5-consultas-select-where-y-order-by)
5. [Repaso: consultas con JOIN](#2-repaso-consultas-con-join)
6. [Gestión de transacciones](#6-gestión-de-transacciones)

---

## 1. Introducción

En este tema aprendemos el lenguaje con el que realmente le "hablamos": **SQL**. Veremos las sentencias de definición de estructura (DDL), las de manipulación de datos (DML), las consultas, y cómo se ejecuta todo esto desde Java a través de la interfaz `Statement` una vez sepamos **conectar** con una base de datos (Tema 4) y **elegir el motor** adecuado (Tema 5).

---


## 2. Sentencias de definición de datos (DDL) - estructura

*Data Definition Language*: sentencias que definen o modifican la **estructura** de la base de datos (no los datos).

| Sentencia | Qué hace |
|---|---|
| `CREATE DATABASE` | Crea una nueva base de datos. Requiere permiso `CREATE` |
| `ALTER DATABASE` | Modifica características generales de una BBDD existente. Requiere permiso `ALTER`. |
| `CREATE TABLE` | Crea una tabla nueva. Da error si la tabla ya existe (salvo que se use `IF NOT EXISTS`) |
| `ALTER TABLE` | Modifica la estructura de una tabla ya creada: añadir/borrar columnas, crear/borrar índices, renombrar columnas, cambiar tipos... |
| `DROP DATABASE` | Borra permanentemente una base de datos y todas sus tablas. Requiere permiso `DROP`. **Comando muy peligroso.** |
| `DROP TABLE` | Borra una o más tablas por completo. Se puede usar `IF EXISTS` para evitar error si no existe |
| `RENAME TABLE` | Renombra una o más tablas. La tabla queda bloqueada temporalmente hasta que termina la operación. |
| `CREATE INDEX` | Crea un índice (acelera las búsquedas por esa columna)|
| `DROP INDEX` | Borra un índice|

```sql
CREATE DATABASE IF NOT EXISTS tienda CHARACTER SET utf8mb4;

CREATE TABLE IF NOT EXISTS clientes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL
);

ALTER TABLE clientes ADD COLUMN email VARCHAR(150);

DROP TABLE IF EXISTS clientes_temporales;
```

---

## 3. Sentencias de manipulación de datos (DML)

*Data Manipulation Language*: sentencias que trabajan con el **contenido** de las tablas.

| Sentencia | Qué hace |
|---|---|
| `INSERT INTO tabla (columnas) VALUES (valores)` | Añade filas. Existen también `INSERT ... SET` e `INSERT ... SELECT` (inserta el resultado de una consulta). **El orden de las columnas debe coincidir con el de los valores**; los textos y fechas van entre comillas simples y los números sin comillas. |
| `UPDATE tabla SET col = valor WHERE condición` | Modifica las columnas indicadas de las filas que cumplen el `WHERE`. **Sin `WHERE` se modifican todas las filas.** |
| `DELETE FROM tabla WHERE condición` | Borra las filas que cumplen la condición y devuelve cuántas ha borrado. **Sin `WHERE` se borran todas.** |
| `REPLACE` | Igual que `INSERT`, pero si ya existe una fila con la misma `PRIMARY KEY` o índice `UNIQUE`, **borra la anterior** y escribe la nueva. |
| `TRUNCATE TABLE` | Vacía una tabla entera. Es como un `DELETE` sin `WHERE`, pero más rápido, **no es transaccional** (no se puede deshacer) y reinicia el contador `AUTO_INCREMENT`. |


---

## 4. Consultas: SELECT, WHERE y ORDER BY

`SELECT` es la sentencia para consultar registros de una o más tablas. El resultado es una **tabla ficticia** (no persiste en disco, solo vive en memoria mientras se usa).

```sql
SELECT [ALL|DISTINCT] columna1, columna2 [AS alias]
FROM tabla
WHERE condiciones
ORDER BY columna [ASC|DESC]
```

- `ALL` / `DISTINCT`: mostrar filas duplicadas o solo únicas.
- `FROM`: tabla(s) o vista(s) sobre las que consultar.
- `WHERE`: condiciones de filtro.
- `ORDER BY`: orden del resultado (`ASC` ascendente por defecto, `DESC` descendente).

### Condiciones del WHERE

| Condición | Ejemplo | Qué hace |
|---|---|---|
| Operadores | `>` `>=` `<` `=` `<>` / `!=` | Comparaciones clásicas |
| Nulos | `IS NULL` / `IS NOT NULL` | Comprueba si una columna tiene valor nulo (¡un campo vacío `''` no es lo mismo que `NULL`!) |
| `LIKE` | `WHERE apellido LIKE 'C%'` | Comparación con comodines: `%` = cualquier cadena, `_` = un único carácter |
| `BETWEEN` | `WHERE edad BETWEEN 3 AND 7` | Rango de valores (incluye los extremos) |
| `IN (...)` | `WHERE edad IN (3, 4, 7, 8)` | El valor coincide con alguno de la lista |

Estas condiciones se pueden combinar con **`AND`**, **`OR`** y **`NOT`**, ayudándose de paréntesis para establecer prioridades:

```sql
SELECT *
FROM CUSTOMER
WHERE (LAST_NAME LIKE 'L%') OR (LAST_NAME LIKE 'F%')
ORDER BY AGE DESC;
```

---

## 5. Repaso: consultas con JOIN

Cuando la información que necesitamos está repartida en varias tablas relacionadas (por ejemplo, `PEDIDOS` y `CLIENTES`), usamos `JOIN` para combinarlas en una sola consulta.

```
CLIENTES                    PEDIDOS
┌────┬────────┐             ┌────┬────────────┬───────────┐
│ id │ nombre │             │ id │ id_cliente │  producto │
├────┼────────┤             ├────┼────────────┼───────────┤
│ 1  │ Ana    │◄────────────┤ 10 │     1      │  Teclado  │
│ 2  │ Luis   │◄────────────┤ 11 │     2      │  Ratón    │
│ 3  │ Eva    │             └────┴────────────┴───────────┘
└────┴────────┘             (Eva no tiene pedidos)
```

| Tipo | Qué devuelve |
|---|---|
| **INNER JOIN** | Solo las filas que **coinciden** en ambas tablas |
| **LEFT JOIN** | Todas las filas de la tabla izquierda, con `NULL` donde no haya coincidencia en la derecha |
| **RIGHT JOIN** | Todas las filas de la tabla derecha, con `NULL` donde no haya coincidencia en la izquierda |

```sql
-- INNER JOIN: solo clientes que SÍ tienen pedidos
SELECT c.nombre, p.producto
FROM CLIENTES c
INNER JOIN PEDIDOS p ON c.id = p.id_cliente;

-- LEFT JOIN: todos los clientes, tengan o no pedidos
SELECT c.nombre, p.producto
FROM CLIENTES c
LEFT JOIN PEDIDOS p ON c.id = p.id_cliente;
```

Con `LEFT JOIN`, si Eva no tiene pedidos, aparecerá igualmente en el resultado con `producto = NULL`.

---

## 6. Gestión de transacciones

Una **transacción** es un conjunto de acciones que se ejecutan en serie y de forma ordenada en el SGBD. Sus dos objetivos son:

- **Consistencia:** secuencias de alta fiabilidad, con posibilidad de volver a estados anteriores.
- **Aislamiento:** que varios aplicativos puedan acceder a los datos simultáneamente sin interferirse.

| Comando | Qué hace |
|---|---|
| `COMMIT` | Persiste (confirma) los cambios en la base de datos |
| `ROLLBACK` | Deshace los cambios ejecutados hasta el momento y abandona la transacción |
| `SAVEPOINT` | Marca un punto de control al que se puede volver con un `ROLLBACK` parcial |

```
BEGIN TRANSACTION
  UPDATE cuentas SET saldo = saldo - 100 WHERE id = 1;
  SAVEPOINT antes_del_ingreso;
  UPDATE cuentas SET saldo = saldo + 100 WHERE id = 2;
  -- si algo falla aquí:
  ROLLBACK TO antes_del_ingreso;   -- deshace solo el segundo UPDATE
  -- si todo va bien:
COMMIT;                            -- confirma ambos cambios
```

---


## Resumen visual del tema

```
TEMA 6 — SENTENCIAS SQL

  DDL (estructura)
   CREATE/ALTER/DROP DATABASE · CREATE/ALTER/DROP/RENAME TABLE · CREATE/DROP INDEX

  DML (contenido)
   INSERT · DELETE · UPDATE · REPLACE (sobrescribe por PK/UNIQUE) · TRUNCATE

  CONSULTAS
   SELECT [DISTINCT] cols FROM tabla WHERE condiciones ORDER BY col [ASC|DESC]
   WHERE: operadores, IS [NOT] NULL, LIKE (% _), BETWEEN, IN(), AND/OR/NOT

  TRANSACCIONES
   COMMIT (confirma) · ROLLBACK (deshace) · SAVEPOINT (punto de control)

  REPASO: JOIN
   INNER JOIN → solo coincidencias · LEFT/RIGHT JOIN → tabla completa + NULLs

```