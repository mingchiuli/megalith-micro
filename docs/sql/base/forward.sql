-- Stop all writers before executing this file. Set the variables below in the
-- same MariaDB session for installations with custom DNS names.
SET @old_user_host = COALESCE(@old_user_host, 'micro-user'),
    @old_user_port = COALESCE(@old_user_port, 8086),
    @old_blog_host = COALESCE(@old_blog_host, 'micro-blog'),
    @old_blog_port = COALESCE(@old_blog_port, 8082),
    @base_host = COALESCE(@base_host, 'micro-base'),
    @base_port = COALESCE(@base_port, 8082);

SELECT m.id AS orphan_menu_id FROM m_menu m LEFT JOIN m_menu p ON m.parent_id = p.id
WHERE m.parent_id <> 0 AND p.id IS NULL;
SELECT rp.id AS orphan_permission_id FROM m_role_data_permission rp
LEFT JOIN m_role r ON r.id = rp.role_id WHERE r.id IS NULL;

DELIMITER //
BEGIN NOT ATOMIC
    IF EXISTS (SELECT 1 FROM m_menu m LEFT JOIN m_menu p ON m.parent_id = p.id
               WHERE m.parent_id <> 0 AND p.id IS NULL)
       OR EXISTS (SELECT 1 FROM m_role_data_permission rp
                  LEFT JOIN m_role r ON r.id = rp.role_id WHERE r.id IS NULL) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Resolve the reported orphan references before migrating';
    END IF;
END//
DELIMITER ;

CREATE TABLE IF NOT EXISTS m_base_cutover_route (
    id BIGINT PRIMARY KEY, service_host VARCHAR(255) NOT NULL, service_port INT NOT NULL
);
START TRANSACTION;
INSERT INTO m_base_cutover_route (id, service_host, service_port)
SELECT id, service_host, service_port FROM m_authority
WHERE (service_host = @old_user_host AND service_port = @old_user_port)
   OR (service_host = @old_blog_host AND service_port = @old_blog_port)
ON DUPLICATE KEY UPDATE id = VALUES(id);
UPDATE m_authority a JOIN m_base_cutover_route old ON a.id = old.id
SET a.service_host = @base_host, a.service_port = @base_port
WHERE a.service_host = old.service_host AND a.service_port = old.service_port;
UPDATE m_menu SET parent_id = NULL WHERE parent_id = 0;
COMMIT;
ALTER TABLE m_menu ADD CONSTRAINT fk_menu_parent FOREIGN KEY IF NOT EXISTS (parent_id) REFERENCES m_menu (id);
ALTER TABLE m_role_data_permission ADD CONSTRAINT fk_role_data_permission_role FOREIGN KEY IF NOT EXISTS (role_id) REFERENCES m_role (id);
