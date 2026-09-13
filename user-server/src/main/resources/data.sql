-- Sembrado RBAC de user-server: catalogo Module/SubModule/Permission + asignacion al rol Editor.
-- Idempotente (guardas NOT EXISTS): Spring lo corre en cada arranque tras el DDL de Hibernate.
-- Los 'path' de sub_modules son provisorios; cruzar con las rutas reales del frontend.

INSERT INTO modules (name, alias, description, created_at, updated_at, created_by, updated_by)
SELECT 'sistema', 'Sistema', 'Modulo raiz sembrado', localtimestamp, localtimestamp, 'seed', 'seed'
WHERE NOT EXISTS (SELECT 1 FROM modules WHERE name = 'sistema');

INSERT INTO sub_modules (name, alias, description, path, module_id, created_at, updated_at, created_by, updated_by)
SELECT s.name, s.alias, NULL, s.path, m.id, localtimestamp, localtimestamp, 'seed', 'seed'
FROM (VALUES
  ('roles', 'Roles', '/roles'),
  ('users', 'Usuarios', '/users'),
  ('clients', 'Mandantes', '/mandantes'),
  ('projectTypes', 'Tipos de proyecto', '/tipos-proyecto'),
  ('projectSpecialties', 'Especialidades', '/especialidades'),
  ('projects', 'Proyectos', '/proyectos'),
  ('platformStatuses', 'Estados', '/estados')
) AS s(name, alias, path)
CROSS JOIN (SELECT id FROM modules WHERE name = 'sistema') m
WHERE NOT EXISTS (SELECT 1 FROM sub_modules sm WHERE sm.name = s.name);

INSERT INTO permissions (name, alias, description, sub_module_id, created_at, updated_at, created_by, updated_by)
SELECT p.name, p.alias, NULL, sm.id, localtimestamp, localtimestamp, 'seed', 'seed'
FROM (VALUES
  ('role.read', 'Ver roles', 'roles'),
  ('role.create', 'Crear roles', 'roles'),
  ('role.update', 'Editar roles', 'roles'),
  ('role.active', 'Activar roles', 'roles'),
  ('user.read', 'Ver usuarios', 'users'),
  ('user.create', 'Crear usuarios', 'users'),
  ('user.update', 'Editar usuarios', 'users'),
  ('user.active', 'Activar usuarios', 'users'),
  ('client.read', 'Ver mandantes', 'clients'),
  ('client.create', 'Crear mandantes', 'clients'),
  ('client.update', 'Editar mandantes', 'clients'),
  ('client.active', 'Activar mandantes', 'clients'),
  ('projectType.read', 'Ver tipos de proyecto', 'projectTypes'),
  ('projectType.create', 'Crear tipos de proyecto', 'projectTypes'),
  ('projectType.update', 'Editar tipos de proyecto', 'projectTypes'),
  ('projectType.active', 'Activar tipos de proyecto', 'projectTypes'),
  ('projectSpecialty.read', 'Ver especialidades', 'projectSpecialties'),
  ('projectSpecialty.create', 'Crear especialidades', 'projectSpecialties'),
  ('projectSpecialty.update', 'Editar especialidades', 'projectSpecialties'),
  ('projectSpecialty.active', 'Activar especialidades', 'projectSpecialties'),
  ('project.read', 'Ver proyectos', 'projects'),
  ('project.create', 'Crear proyectos', 'projects'),
  ('project.update', 'Editar proyectos', 'projects'),
  ('project.status', 'Cambiar estado de proyecto', 'projects'),
  ('platformStatus.read', 'Ver estados de plataforma', 'platformStatuses')
) AS p(name, alias, sub)
JOIN sub_modules sm ON sm.name = p.sub
WHERE NOT EXISTS (SELECT 1 FROM permissions pe WHERE pe.name = p.name);

INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, pe.id
FROM roles r
CROSS JOIN permissions pe
WHERE r.name = 'Editor'
  AND NOT EXISTS (
    SELECT 1 FROM role_permission rp WHERE rp.role_id = r.id AND rp.permission_id = pe.id
  );

INSERT INTO role_submodule (role_id, submodule_id)
SELECT r.id, sm.id
FROM roles r
CROSS JOIN sub_modules sm
WHERE r.name = 'Editor'
  AND NOT EXISTS (
    SELECT 1 FROM role_submodule rs WHERE rs.role_id = r.id AND rs.submodule_id = sm.id
  );
