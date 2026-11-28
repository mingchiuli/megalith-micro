"""Initialize an empty E2E database from the development schema and navigation only."""

from __future__ import annotations

import argparse
import os
from pathlib import Path
import re
import subprocess


def output(*args: str, data: bytes | None = None) -> bytes:
    return subprocess.run(args, input=data, stdout=subprocess.PIPE, check=True).stdout


def query(container: str, database: str, sql: bytes) -> bytes:
    return output("docker", "exec", "-i", container, "sh", "-ec",
                  'MYSQL_PWD="$MARIADB_ROOT_PASSWORD" exec mariadb -uroot --batch --skip-column-names "$1"',
                  "mariadb", database, data=sql)


def identifier(value: str) -> str:
    if not re.fullmatch(r"[a-zA-Z][a-zA-Z0-9_]{0,63}", value):
        raise argparse.ArgumentTypeError("Expected a MariaDB identifier")
    return value


def prepare(source: str, database: str, target: str, fixture: str, offset: int, candidate: bool) -> None:
    source_id = output("docker", "inspect", "--format", "{{.Id}}", source)
    target_id = output("docker", "inspect", "--format", "{{.Id}}", target)
    if source_id == target_id:
        raise ValueError("Choose a separate database container for the writable fixture")
    if not 0 <= offset <= 65535 - 8089:
        raise ValueError("Port offset puts a fixture service outside the TCP port range")
    password = os.environ["E2E_PASSWORD"]
    hashed = subprocess.run(
        ["bun", "-e", "console.log(await Bun.password.hash(process.env.E2E_PASSWORD,"
         "{algorithm:'bcrypt',cost:10}))"], env=os.environ | {"E2E_PASSWORD": password},
        stdout=subprocess.PIPE, check=True).stdout.decode().strip()
    if not re.fullmatch(r"\$2[aby]\$\d\d\$[./A-Za-z0-9]{53}", hashed):
        raise ValueError("Could not hash the fixture password")
    dump = ["docker", "exec", source, "sh", "-ec",
            'MYSQL_PWD="$MARIADB_ROOT_PASSWORD" exec mariadb-dump -uroot --single-transaction --skip-lock-tables "$@"',
            "mariadb-dump"]
    schema = output(*dump, "--no-data", database)
    navigation = output(*dump, "--no-create-info", database, "m_authority", "m_menu", "m_menu_authority")
    # CREATE DATABASE fails if the fixture already exists, before any rows are imported.
    query(target, "mysql", f"CREATE DATABASE `{fixture}` CHARACTER SET utf8mb4".encode())
    query(target, fixture, schema + b"\n" + navigation)
    query(target, fixture, f"""
        UPDATE m_authority SET service_host = '127.0.0.1', service_port = service_port + {offset};
        UPDATE m_authority SET route_pattern = '/public/blog/recommendations'
          WHERE route_pattern = '/public/blog/recommendations/page/*'
          AND NOT EXISTS (SELECT 1 FROM m_authority current_route
                          WHERE current_route.route_pattern = '/public/blog/recommendations');
        INSERT INTO m_role(id,name,code,status,created,updated)
        VALUES (1,'E2E Admin','e2e-admin',0,NOW(),NOW()),(2,'E2E Reader','e2e-reader',0,NOW(),NOW()),
               (3,'Registered','user',0,NOW(),NOW());
        INSERT INTO m_role_menu(role_id,menu_id,created,updated) SELECT 1,id,NOW(),NOW() FROM m_menu;
        INSERT INTO m_role_menu(role_id,menu_id,created,updated) SELECT r.id,m.id,NOW(),NOW()
          FROM m_role r JOIN m_menu m WHERE r.id IN (2,3) AND m.name='system-root';
        INSERT INTO m_role_data_permission(role_id,permission_code)
        VALUES (1,'BLOG_VIEW_ALL'),(1,'BLOG_EDIT_ALL'),(1,'BLOG_DELETE_ALL'),(1,'BLOG_EXPORT_ALL');
        INSERT INTO m_user(id,username,nickname,email,password,status,created,updated)
        VALUES (1,'e2e-admin','E2E Admin','admin@example.invalid','{hashed}',0,NOW(),NOW()),
               (2,'e2e-reader','E2E Reader','reader@example.invalid','{hashed}',0,NOW(),NOW());
        INSERT INTO m_user_role(user_id,role_id,created,updated) VALUES (1,1,NOW(),NOW()),(2,2,NOW(),NOW());
    """.encode())
    if candidate:
        migration = Path(__file__).resolve().parents[1] / "docs/sql/base"
        address = (f"SET @old_user_host='127.0.0.1', @old_user_port={offset + 8086}, "
                   f"@old_blog_host='127.0.0.1', @old_blog_port={offset + 8082}, "
                   f"@base_host='127.0.0.1', @base_port={offset + 8082};\n").encode()
        for operation in ["forward", "rollback", "forward"]:
            query(target, fixture, address + (migration / f"{operation}.sql").read_bytes())


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--source-container", required=True)
    parser.add_argument("--source-db", type=identifier, default="vueblog")
    parser.add_argument("--target-container", required=True)
    parser.add_argument("--fixture-db", required=True, type=identifier)
    parser.add_argument("--port-offset", type=int, required=True)
    parser.add_argument("--candidate", action="store_true")
    args = parser.parse_args()
    prepare(args.source_container, args.source_db, args.target_container, args.fixture_db,
            args.port_offset, args.candidate)
