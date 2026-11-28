-- Execute with writers stopped and the route snapshot from forward.sql present.
ALTER TABLE m_menu DROP FOREIGN KEY IF EXISTS fk_menu_parent;
ALTER TABLE m_role_data_permission DROP FOREIGN KEY IF EXISTS fk_role_data_permission_role;
START TRANSACTION;
UPDATE m_authority a JOIN m_base_cutover_route old ON a.id = old.id
SET a.service_host = old.service_host, a.service_port = old.service_port;
UPDATE m_menu SET parent_id = 0 WHERE parent_id IS NULL;
COMMIT;
