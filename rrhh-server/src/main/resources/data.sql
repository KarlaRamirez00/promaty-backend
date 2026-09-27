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
