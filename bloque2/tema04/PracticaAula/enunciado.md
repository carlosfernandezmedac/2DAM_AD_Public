# Práctica en clase — Portátiles del aula (JDBC + MySQL)

**Material permitido:** tus apuntes (Temas 4 y 6)

## Preparación

1. En Workbench, ejecuta `aula.sql`. Crea la base `aula` con dos tablas:
   - `portatil` (`codigo`, `ip`, `incidencias`): los portátiles 101 a 130. `incidencias` es `NULL` si no tiene ninguna.
   - `alumno` (`id_alumno`, `nombre`, `apellidos`, `email`, `codigo_pc`): empieza vacía. `codigo_pc` es el portátil del alumno y **no se puede repetir**.
2. Abre `PracticaAula.java`. La conexión, el menú y la lectura de datos por teclado ya están hechos. Cambia `CAMBIAR` por tu contraseña.
3. Ejecuta el programa: debe salir `Conexion OK` y el menú.

## Qué tienes que hacer

Completa los cuatro métodos que están vacíos. Cada uno lleva un comentario con lo que debe hacer:

1. **`listarPortatiles` (2 puntos).** Con un `Statement`, muestra todos los portátiles (`codigo - ip - incidencias`)
2. **`insertarAlumno` (2 puntos).** Con un `PreparedStatement`, inserta el alumno y muestra las filas afectadas.
3. **`verPortatil` (3 puntos).** Con un `PreparedStatement`, busca el portátil por su código y muestra sus datos y los del alumno que lo tiene asignado (une las tablas `portatil` y `alumno`). Al final, muestra cuántos portátiles ha encontrado la consulta.
4. **`cambiarPortatil` (2 puntos).** Con un `PreparedStatement`, cambia el portátil del alumno con ese email y muestra las filas afectadas.

## Pruebas (1 punto)

Haz estas pruebas por este orden y comprueba que el programa no se cierra:

1. Opción 1: lista los portátiles.
2. Opción 2: inserta tus datos con el portátil que usas en clase.
3. Opción 3: mira tu portátil, uno libre y el 999 (no existe).
4. Opción 4: cámbiate a un portátil libre.
5. Opción 4: cámbiate a uno que ya tenga otro compañero (debe salir un error) y al 999 (también error).

## Qué se valora

- Que compile y funcione.
- `Statement` solo con SQL fijo y `PreparedStatement` con `?` cuando hay datos (nunca concatenar los datos en el SQL).
- Recursos cerrados con `try-with-resources`.