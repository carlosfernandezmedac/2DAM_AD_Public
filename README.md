# Acceso a Datos — 2º DAM

Apuntes, ejemplos y ejercicios de la asignatura **Acceso a Datos** de 2º DAM.

---

## Contenidos

### 1er Trimestre

| Bloque | Temas | Contenido |
|--------|-------|-----------|
| [Bloque 1 — Manejo de ficheros](bloque1/README.md) | 1, 2, 3 | Ficheros y directorios, flujos (bytes/caracteres), ficheros XML, excepciones |
| Bloque 2 — Acceso a bases de datos relacionales | 4, 5, 6 | Conectores, JDBC, conexión, ejecución de consultas y sentencias SQL, transacciones |
| Bloque 3 — Persistencia con ORM | 7, 8 | Mapeo objeto-relacional, Hibernate, HQL |

### 2º Trimestre

| Bloque | Temas | Contenido |
|--------|-------|-----------|
| Bloque 4 — Bases de datos objeto-relacionales y orientadas a objetos (I) | 9, 10 | SQL objeto-relacional, gestores objeto-relacionales, definición y modificación de objetos |
| Bloque 5 — Bases de datos objeto-relacionales y orientadas a objetos (II) | 11, 12, 13 | Bases de datos orientadas a objetos, consultas No-SQL, transacciones |
| Bloque 6 — Bases de datos nativas XML | 14 | Gestores XML, colecciones y documentos, indexación, consultas |
| Bloque 7 — Componentes de acceso a datos | 15 | Programación orientada a componentes, propiedades, eventos, empaquetado |

---

## ¿Qué vamos a ver este curso?

La asignatura sigue un hilo conductor: cómo un programa Java accede, guarda y gestiona información, cada vez con herramientas más potentes.

```
Bloque 1  ──►  Ficheros            → la forma más básica de guardar datos (texto, binario, XML)
Bloque 2  ──►  JDBC                → conectar Java con una base de datos relacional y lanzar SQL
Bloque 3  ──►  ORM (Hibernate)     → dejar de escribir SQL a mano: mapear objetos Java a tablas
Bloque 4-5──►  Objeto-relacional   → bases de datos que van más allá de tablas simples, y No-SQL
Bloque 6  ──►  BBDD nativas XML    → bases de datos pensadas directamente para documentos XML
Bloque 7  ──►  Componentes         → empaquetar todo lo anterior en piezas reutilizables
```

Cada bloque se apoya en el anterior: primero se aprende a manejar datos "a mano" con ficheros, después a delegar ese trabajo en una base de datos, y por último a automatizar y empaquetar esa gestión.

---

## Estructura de cada bloque

- **apuntes.md** — Resumen teórico de cada tema, con diagramas y tablas comparativas.
- **casospracticos.md** — Ejemplos guiados y casos prácticos, con su resolución.
- **ejercicios.md** — Ejercicios para practicar de forma autónoma y corregir en clase.

---

## Herramientas

- **Java** (con `java.io`, JDBC, Hibernate según el bloque)
- **JDBC** — conectores para bases de datos relacionales (MySQL, Oracle...)
- **Spring Boot + H2** — base de datos embebida para prácticas rápidas
- **Hibernate / HQL** — mapeo objeto-relacional (ORM)
- **Gestor de base de datos nativa XML** (a definir en el Bloque 6)
