-- ============================================================
-- DATOS DEL GIMNASIO (importar DESPUES de insertar tu socio 1)
-- En Workbench: File > Open SQL Script... > elegir este fichero > rayo
-- ===========================================================

INSERT INTO monitor (id_monitor, nombre, telefono) VALUES
 (1, 'Laura Vidal',  '611111111'),
 (2, 'Pablo Ortega', '622222222'),
 (3, 'Marta Rey',    '633333333');

INSERT INTO socio (id_socio, nombre, ciudad, email) VALUES
 (2, 'Berto Ruiz',   'Sevilla',  'berto@mail.com'),
 (3, 'Carla Gil',    'Malaga',   NULL),
 (4, 'David Sanz',   'Granada',  'david@mail.com'),
 (5, 'Elena Mora',   'Sevilla',  'elena@mail.com'),
 (6, 'Alberto Vega', 'Cordoba',  NULL);

INSERT INTO actividad (id_actividad, nombre, precio, plazas, id_monitor) VALUES
 (1, 'Yoga',     15.00, 12, 1),
 (2, 'Spinning', 20.00, 15, 2),
 (3, 'Pilates',  18.50, 10, 1),
 (4, 'Boxeo',    25.00,  8, 2);

INSERT INTO inscripcion (id_socio, id_actividad, fecha) VALUES
 (1, 1, '2026-09-01'),
 (1, 2, '2026-09-05'),
 (2, 2, '2026-09-02'),
 (3, 1, '2026-09-03'),
 (3, 3, '2026-09-10'),
 (4, 4, '2026-09-12'),
 (5, 2, '2026-09-15'),
 (1, 3, '2026-09-20');
