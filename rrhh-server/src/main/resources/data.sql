-- Catalogo gestionado por el sistema: se siembra aca y solo se consume via GET /platformStatuses.
-- El script corre en cada arranque; ON CONFLICT (code) DO NOTHING lo hace idempotente.
INSERT INTO platform_status (name, code, description, sort_order, sub_module, active,
                             created_at, updated_at, created_by, updated_by)
VALUES
	('Planificado',  'PLANNED',      'Proyecto creado, aún no inicia en terreno.',   1, 'project', true, now(), now(), 'system', 'system'),
	('En ejecución', 'IN_PROGRESS',  'Proyecto en obra.',                            2, 'project', true, now(), now(), 'system', 'system'),
	('Suspendido',   'SUSPENDED',    'Proyecto detenido temporalmente.',             3, 'project', true, now(), now(), 'system', 'system'),
	('Finalizado',   'COMPLETED',    'Proyecto terminado.',                          4, 'project', true, now(), now(), 'system', 'system'),
	('Cancelado',    'CANCELLED',    'Proyecto cancelado antes de completarse.',     5, 'project', true, now(), now(), 'system', 'system')
ON CONFLICT (code) DO NOTHING;
