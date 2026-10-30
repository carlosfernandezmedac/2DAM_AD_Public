-- Base de datos de la practica: portatiles del aula
-- Ejecutar entera en Workbench (rayo). Crea la base 'aula'.
DROP DATABASE IF EXISTS aula;
CREATE DATABASE aula;
USE aula;

CREATE TABLE portatil (
    codigo      INT PRIMARY KEY,
    ip          VARCHAR(15) NOT NULL,
    incidencias VARCHAR(200)              -- NULL = sin incidencias
);

CREATE TABLE alumno (
    id_alumno  INT AUTO_INCREMENT PRIMARY KEY,
    nombre     VARCHAR(50)  NOT NULL,
    apellidos  VARCHAR(80)  NOT NULL,
    email      VARCHAR(100) NOT NULL UNIQUE,
    codigo_pc  INT UNIQUE,                -- un portatil solo puede tener un alumno
    FOREIGN KEY (codigo_pc) REFERENCES portatil (codigo)
);

INSERT INTO portatil (codigo, ip, incidencias) VALUES
 (101, '10.206.8.101', NULL),
 (102, '10.206.8.102', 'Se reinicia solo por sobrecalentamiento'),
 (103, '10.206.8.103', NULL),
 (104, '10.206.8.104', NULL),
 (105, '10.206.8.105', NULL),
 (106, '10.206.8.106', NULL),
 (107, '10.206.8.107', NULL),
 (108, '10.206.8.108', NULL),
 (109, '10.206.8.109', NULL),
 (110, '10.206.8.110', NULL),
 (111, '10.206.8.111', NULL),
 (112, '10.206.8.112', NULL),
 (113, '10.206.8.113', NULL),
 (114, '10.206.8.114', NULL),
 (115, '10.206.8.115', NULL),
 (116, '10.206.8.116', NULL),
 (117, '10.206.8.117', NULL),
 (118, '10.206.8.118', NULL),
 (119, '10.206.8.119', NULL),
 (120, '10.206.8.120', NULL),
 (121, '10.206.8.121', 'No conecta al wifi MEDAC'),
 (122, '10.206.8.122', NULL),
 (123, '10.206.8.123', NULL),
 (124, '10.206.8.124', NULL),
 (125, '10.206.8.125', NULL),
 (126, '10.206.8.126', NULL),
 (127, '10.206.8.127', NULL),
 (128, '10.206.8.128', NULL),
 (129, '10.206.8.129', NULL),
 (130, '10.206.8.130', NULL);
