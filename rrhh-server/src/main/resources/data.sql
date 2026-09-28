-- Requerida por StaffQueryBuilder para que el filtro de busqueda ignore tildes (ej. "ramir" encuentra
-- "Ramírez"). CREATE EXTENSION es idempotente por si sola, sin necesitar ON CONFLICT.
CREATE EXTENSION IF NOT EXISTS unaccent;

-- Catalogo gestionado por el sistema: se siembra aca y solo se consume via GET /platformStatuses.
-- El script corre en cada arranque; ON CONFLICT (sub_module, code) DO NOTHING lo hace idempotente.
-- code es unico por submodulo, no global: otros submodulos pueden reusar el mismo code (ej. ACTIVE).
INSERT INTO platform_status (name, code, description, sort_order, sub_module, active,
                             created_at, updated_at, created_by, updated_by)
VALUES
	('Planificado',  'PLANNED',      'Proyecto creado, aún no inicia en terreno.',   1, 'project', true, now(), now(), 'system', 'system'),
	('En ejecución', 'IN_PROGRESS',  'Proyecto en obra.',                            2, 'project', true, now(), now(), 'system', 'system'),
	('Suspendido',   'SUSPENDED',    'Proyecto detenido temporalmente.',             3, 'project', true, now(), now(), 'system', 'system'),
	('Finalizado',   'COMPLETED',    'Proyecto terminado.',                          4, 'project', true, now(), now(), 'system', 'system'),
	('Cancelado',    'CANCELLED',    'Proyecto cancelado antes de completarse.',     5, 'project', true, now(), now(), 'system', 'system'),
	('Pendiente de aprobación', 'PENDING_APPROVAL',  'Esperando aprobación de jefatura.',        1, 'request', true, now(), now(), 'system', 'system'),
	('Pendiente de validación', 'PENDING_VALIDATION', 'Aprobada por jefatura, esperando validación de RRHH.', 2, 'request', true, now(), now(), 'system', 'system'),
	('Aprobada',                'APPROVED',           'Validada por RRHH, cambios ya aplicados.', 3, 'request', true, now(), now(), 'system', 'system'),
	('Rechazada',               'REJECTED',           'Rechazada en algún nivel de aprobación.',  4, 'request', true, now(), now(), 'system', 'system')
ON CONFLICT (sub_module, code) DO NOTHING;

-- Catálogos de solo lectura para la ficha de Staff (HU-B23, extensión legal). Valores editables por
-- Karla: son datos de referencia, no una regla de negocio en si (salvo BANCO_ESTADO, usado por
-- StaffValidation para la regla de "Cuenta Rut").
INSERT INTO registered_sex (name, code, active, created_at, updated_at, created_by, updated_by)
VALUES
	('Femenino',  'FEMALE',  true, now(), now(), 'system', 'system'),
	('Masculino', 'MALE',    true, now(), now(), 'system', 'system')
ON CONFLICT (code) DO NOTHING;

INSERT INTO marital_status (name, code, active, created_at, updated_at, created_by, updated_by)
VALUES
	('Soltero/a', 'SINGLE',    true, now(), now(), 'system', 'system'),
	('Casado/a',  'MARRIED',   true, now(), now(), 'system', 'system'),
	('Divorciado/a', 'DIVORCED', true, now(), now(), 'system', 'system'),
	('Viudo/a',   'WIDOWED',   true, now(), now(), 'system', 'system'),
	('Conviviente civil (AUC)', 'CIVIL_UNION', true, now(), now(), 'system', 'system')
ON CONFLICT (code) DO NOTHING;

INSERT INTO nationality (name, code, active, created_at, updated_at, created_by, updated_by)
VALUES
	('Chilena',      'CHL', true, now(), now(), 'system', 'system'),
	('Venezolana',   'VEN', true, now(), now(), 'system', 'system'),
	('Colombiana',   'COL', true, now(), now(), 'system', 'system'),
	('Peruana',      'PER', true, now(), now(), 'system', 'system'),
	('Haitiana',     'HTI', true, now(), now(), 'system', 'system'),
	('Boliviana',    'BOL', true, now(), now(), 'system', 'system')
ON CONFLICT (code) DO NOTHING;

INSERT INTO education_level (name, code, active, created_at, updated_at, created_by, updated_by)
VALUES
	('Educación básica',              'BASIC',      true, now(), now(), 'system', 'system'),
	('Educación media',               'HIGH_SCHOOL', true, now(), now(), 'system', 'system'),
	('Técnico nivel superior',        'TECHNICAL',  true, now(), now(), 'system', 'system'),
	('Educación universitaria',       'UNIVERSITY', true, now(), now(), 'system', 'system'),
	('Sin estudios',                  'NONE',       true, now(), now(), 'system', 'system')
ON CONFLICT (code) DO NOTHING;

INSERT INTO afp (name, code, active, created_at, updated_at, created_by, updated_by)
VALUES
	('AFP Capital',    'CAPITAL',   true, now(), now(), 'system', 'system'),
	('AFP Cuprum',     'CUPRUM',    true, now(), now(), 'system', 'system'),
	('AFP Habitat',    'HABITAT',   true, now(), now(), 'system', 'system'),
	('AFP Modelo',     'MODELO',    true, now(), now(), 'system', 'system'),
	('AFP PlanVital',  'PLANVITAL', true, now(), now(), 'system', 'system'),
	('AFP ProVida',    'PROVIDA',   true, now(), now(), 'system', 'system'),
	('AFP Uno',        'UNO',       true, now(), now(), 'system', 'system')
ON CONFLICT (code) DO NOTHING;

INSERT INTO health_system (name, code, active, created_at, updated_at, created_by, updated_by)
VALUES
	('Fonasa',             'FONASA',      true, now(), now(), 'system', 'system'),
	('Isapre Banmédica',   'BANMEDICA',   true, now(), now(), 'system', 'system'),
	('Isapre Colmena',     'COLMENA',     true, now(), now(), 'system', 'system'),
	('Isapre Consalud',    'CONSALUD',    true, now(), now(), 'system', 'system'),
	('Isapre Cruz Blanca', 'CRUZ_BLANCA', true, now(), now(), 'system', 'system'),
	('Isapre Vida Tres',   'VIDA_TRES',   true, now(), now(), 'system', 'system')
ON CONFLICT (code) DO NOTHING;

INSERT INTO bank (name, code, active, created_at, updated_at, created_by, updated_by)
VALUES
	('BancoEstado',        'BANCO_ESTADO', true, now(), now(), 'system', 'system'),
	('Banco de Chile',     'BANCO_CHILE',  true, now(), now(), 'system', 'system'),
	('Banco Santander',    'SANTANDER',    true, now(), now(), 'system', 'system'),
	('Banco BCI',          'BCI',          true, now(), now(), 'system', 'system'),
	('Scotiabank',         'SCOTIABANK',   true, now(), now(), 'system', 'system')
ON CONFLICT (code) DO NOTHING;
