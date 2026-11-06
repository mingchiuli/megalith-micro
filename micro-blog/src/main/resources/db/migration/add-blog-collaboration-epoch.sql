-- Applied automatically before Hibernate initializes existing blog tables.
-- Existing articles get independent rooms; old collaboration state is not merged.
ALTER TABLE m_blog ADD COLUMN IF NOT EXISTS collaboration_epoch VARCHAR(36) NULL;
UPDATE m_blog SET collaboration_epoch = UUID() WHERE collaboration_epoch IS NULL;
ALTER TABLE m_blog MODIFY COLUMN collaboration_epoch VARCHAR(36) NOT NULL;
