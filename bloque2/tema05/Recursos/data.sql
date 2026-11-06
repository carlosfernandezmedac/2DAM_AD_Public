
DROP ALL OBJECTS;
CREATE TABLE monitor (
    id_monitor INT AUTO_INCREMENT PRIMARY KEY,
    nombre     VARCHAR(50) NOT NULL,
    telefono   VARCHAR(9)
);

CREATE TABLE socio (
    id_socio   INT AUTO_INCREMENT PRIMARY KEY,
    nombre     VARCHAR(50) NOT NULL,
    ciudad     VARCHAR(40),
    email      VARCHAR(80),
    fecha_alta DATE NOT NULL
);

CREATE TABLE actividad (
    id_actividad INT AUTO_INCREMENT PRIMARY KEY,
    nombre       VARCHAR(40) NOT NULL,
    precio       DECIMAL(6,2) NOT NULL,
    plazas       INT,
    dia          VARCHAR(10),
    id_monitor   INT NOT NULL,
    FOREIGN KEY (id_monitor) REFERENCES monitor (id_monitor)
);

CREATE TABLE inscripcion (
    id_inscripcion INT AUTO_INCREMENT PRIMARY KEY,
    id_socio       INT NOT NULL,
    id_actividad   INT NOT NULL,
    fecha          DATE NOT NULL,
    FOREIGN KEY (id_socio)     REFERENCES socio (id_socio),
    FOREIGN KEY (id_actividad) REFERENCES actividad (id_actividad)
);




INSERT INTO monitor (id_monitor, nombre, telefono) VALUES
 (1, 'Laura Vidal',  '611111111'),
 (2, 'Pablo Ortega', '622222222'),
 (3, 'Marta Rey',    '633333333');

INSERT INTO socio (id_socio, nombre, ciudad, email, fecha_alta) VALUES
 (1, 'Ana Lopez',    'Malaga',   'ana@mail.com',   '2025-09-10'),
 (2, 'Berto Ruiz',   'Sevilla',  'berto@mail.com', '2025-11-05'),
 (3, 'Carla Gil',    'Malaga',   NULL,             '2026-01-15'),
 (4, 'David Sanz',   'Granada',  'david@mail.com', '2026-02-20'),
 (5, 'Elena Mora',   'Sevilla',  'elena@mail.com', '2026-03-12'),
 (6, 'Alberto Vega', 'Cordoba',  NULL,             '2026-05-30');

INSERT INTO actividad (id_actividad, nombre, precio, plazas, dia, id_monitor) VALUES
 (1, 'Yoga',     15.00, 12, 'Lunes',  1),
 (2, 'Spinning', 20.00, 15, 'Martes', 2),
 (3, 'Pilates',  18.50, 10, 'Lunes',  1),
 (4, 'Boxeo',    25.00,  8, 'Jueves', 2);

INSERT INTO inscripcion (id_socio, id_actividad, fecha) VALUES
 (1, 1, '2026-09-01'),
 (1, 2, '2026-09-05'),
 (2, 2, '2026-09-02'),
 (3, 1, '2026-09-03'),
 (3, 3, '2026-09-10'),
 (4, 4, '2026-09-12'),
 (5, 2, '2026-09-15'),
 (1, 3, '2026-09-20');
