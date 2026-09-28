#!/usr/bin/env python3
"""Local Oracle administration; credentials stay in ignored .env and process input."""
from pathlib import Path
import argparse
import os
import re
import secrets
import subprocess

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[1]
TENANTS = ('CINE_TICS', 'CADENA_DEMO')
COMPOSE = ['docker', 'compose', '-f', str(HERE / 'compose.yaml')]
KEYS = ['ORACLE_ADMIN_PASSWORD'] + [f'{t}_{role}_PASSWORD' for t in TENANTS for role in ('OWNER', 'APP')]


def credentials():
    path = HERE / '.env'
    if not path.exists():
        raise SystemExit('Run: python3 infra/oracle/manage.py secrets')
    values = dict(line.split('=', 1) for line in path.read_text().splitlines() if line and not line.startswith('#'))
    for key in KEYS:
        if not re.fullmatch(r'[A-Za-z0-9]{20,100}', values.get(key, '')):
            raise SystemExit(f'Invalid or missing credential: {key}; expected 20-100 alphanumeric characters')
    return values


def safe_output(output):
    for value in credentials().values():
        output = output.replace(value, '[redacted]')
    return output


def sql(statement, tenant=None, role='APP'):
    if tenant is not None and tenant not in TENANTS:
        raise ValueError('Unknown tenant')
    login = 'connect / as sysdba'
    if tenant:
        password = credentials()[f'{tenant}_{role}_PASSWORD']
        login = f'connect CINE_{role}/"{password}"@//localhost:1521/{tenant}'
    script = ('set echo off verify off feedback off heading off pagesize 0 linesize 32767 trimspool on\n'
              'whenever oserror exit failure rollback\nwhenever sqlerror exit failure rollback\n'
              + login + '\n' + statement + '\nexit\n')
    result = subprocess.run(COMPOSE + ['exec', '-T', '-e', 'NLS_LANG=.AL32UTF8', 'oracle', 'sqlplus', '-s', '-L', '/nolog'],
                            input=script, text=True, capture_output=True, timeout=120)
    output = safe_output(result.stdout + result.stderr)
    if result.returncode or re.search(r'(ORA-|SP2-)\d+', output):
        raise RuntimeError(output.strip())
    return output.strip()


def provision():
    values = credentials()
    for tenant in TENANTS:
        exists = sql(f"select count(*) from v$pdbs where name='{tenant}';")
        if exists == '0':
            sql(f'create pluggable database {tenant} admin user pdb_admin identified by "{values["ORACLE_ADMIN_PASSWORD"]}" create_file_dest=\'/opt/oracle/oradata\';')
        mode = sql(f"select open_mode from v$pdbs where name='{tenant}';")
        if mode != 'READ WRITE':
            sql(f'alter pluggable database {tenant} open;')
        sql(f'alter pluggable database {tenant} save state;')
        prefix = f'alter session set container={tenant};\n'
        if sql(prefix + "select count(*) from dba_tablespaces where tablespace_name='CINE_DATA';") == '0':
            sql(prefix + "create tablespace CINE_DATA datafile size 50M autoextend on next 10M maxsize 512M;")
        for role in ('OWNER', 'APP'):
            user = f'CINE_{role}'
            if sql(prefix + f"select count(*) from dba_users where username='{user}';") == '0':
                quota = 'quota 512M on CINE_DATA' if role == 'OWNER' else ''
                sql(prefix + f'create user {user} identified by "{values[f"{tenant}_{role}_PASSWORD"]}" default tablespace CINE_DATA {quota};')
            privileges = 'create session, create table, create sequence' if role == 'OWNER' else 'create session'
            sql(prefix + f'grant {privileges} to {user};')
        print(f'{tenant}: open, saved state, owner and application users ready', flush=True)


def migrate():
    for tenant in TENANTS:
        env = os.environ.copy()
        env['FLYWAY_URL'] = f'jdbc:oracle:thin:@//127.0.0.1:1522/{tenant}'
        env['FLYWAY_PASSWORD'] = credentials()[f'{tenant}_OWNER_PASSWORD']
        result = subprocess.run([str(ROOT / 'Cine/mvnw'), '-B', '-ntp', '-f', str(HERE / 'pom.xml'),
                                 'flyway:migrate', 'flyway:validate'], env=env, capture_output=True, text=True)
        print(safe_output(result.stdout + result.stderr), flush=True)
        if result.returncode:
            raise SystemExit(result.returncode)


def seed():
    # Only explicit synthetic catalog values; no personal historical records.
    for tenant in TENANTS:
        branches = [(1, 'CU', 'CU'), (2, 'UNIVERSIDAD', 'Universidad'), (3, 'DELTA', 'Delta'),
                    (4, 'XOCHIMILCO', 'Xochimilco')] if tenant == 'CINE_TICS' else [(1, 'DEMO', 'Sucursal Demo')]
        for id_, code, name in branches:
            sql(f"merge into branches b using (select {id_} id from dual) s on (b.id=s.id) "
                f"when not matched then insert (id,code,name) values ({id_},'{code}','{name}');\ncommit;", tenant, 'OWNER')
        print(f'{tenant}: synthetic branches available', flush=True)


def verify():
    for tenant, expected in zip(TENANTS, ('CU', 'Sucursal Demo')):
        actual = sql("select sys_context('USERENV','CON_NAME') from dual;", tenant)
        if actual != tenant:
            raise RuntimeError(f'Wrong PDB: {actual}')
        name = sql('select name from CINE_OWNER.branches where id=1;', tenant)
        if name != expected:
            raise RuntimeError(f'{tenant}: unexpected branch name')
        # A transaction rollback must leave catalog unchanged.
        sql("update CINE_OWNER.branches set name='Rollback probe' where id=1;\nrollback;", tenant)
        if sql('select name from CINE_OWNER.branches where id=1;', tenant) != expected:
            raise RuntimeError('Rollback failed')
        forbidden = sql("select count(*) from user_sys_privs where privilege <> 'CREATE SESSION';", tenant)
        if forbidden != '0' or sql('select count(*) from user_role_privs;', tenant) != '0':
            raise RuntimeError('Application has unexpected system privileges')
        probe_id = secrets.randbelow(10**18) + 1
        sql(f"""begin
            insert into CINE_OWNER.products(id,code,name,price)
            values ({probe_id},'VERIFY_{probe_id}','Verification only',65);
            begin
                insert into CINE_OWNER.inventory(branch_id,product_id,quantity)
                values (1,{probe_id},-1);
                raise_application_error(-20001,'Negative stock was accepted');
            exception when others then
                if sqlcode != -2290 then raise; end if;
            end;
            insert into CINE_OWNER.inventory(branch_id,product_id,quantity)
            values (1,{probe_id},2);
            rollback;
        end;
        /""", tenant)
        if sql(f'select count(*) from CINE_OWNER.products where id={probe_id};', tenant) != '0':
            raise RuntimeError('Product transaction rollback failed')
        print(f'{tenant}: service, isolated catalog, rollback, stock constraint and minimal privileges verified', flush=True)
    # Cross-PDB authentication: the first chain's application credentials must not work in the other PDB.
    wrong_password = credentials()['CINE_TICS_APP_PASSWORD']
    login = f'whenever sqlerror exit failure\nconnect CINE_APP/"{wrong_password}"@//localhost:1521/CADENA_DEMO\nexit\n'
    result = subprocess.run(COMPOSE + ['exec', '-T', '-e', 'NLS_LANG=.AL32UTF8', 'oracle', 'sqlplus', '-s', '-L', '/nolog'],
                            input=login, text=True, capture_output=True, timeout=30)
    if 'ORA-01017' not in result.stdout + result.stderr:
        raise RuntimeError('Expected cross-PDB invalid credentials rejection was not observed')
    print('Cross-chain credentials rejected: PASS', flush=True)


def test():
    env = os.environ.copy()
    for tenant in TENANTS:
        env[f'{tenant}_APP_PASSWORD'] = credentials()[f'{tenant}_APP_PASSWORD']
    result = subprocess.run([str(ROOT/'Cine/mvnw'), '-B', '-ntp', '-f', str(ROOT/'Cine/pom.xml'),
                             '-Poracle-it', 'verify'], env=env)
    if result.returncode:
        raise SystemExit(result.returncode)


def demo_seed():
    # Does not refill an existing wallet or stock: rerunning cannot erase purchase effects.
    sql("""merge into demo_wallets w using (select 'DEMO' id from dual) s on (w.customer_id=s.id)
        when not matched then insert(customer_id,balance,active) values ('DEMO',1000,1);
        merge into products p using (select 9001 id from dual) s on (p.id=s.id)
        when not matched then insert(id,code,name,price) values (9001,'DEMO_REFRESCO','Refresco de demostración',65);
        merge into inventory i using (select 1 branch_id,9001 product_id from dual) s
        on (i.branch_id=s.branch_id and i.product_id=s.product_id)
        when not matched then insert(branch_id,product_id,quantity) values (1,9001,10);
        merge into auditoriums a using (select 9101 id from dual) s on (a.id=s.id)
        when not matched then insert(id,branch_id,name,capacity) values (9101,1,'Sala de demostración',12);
        commit;""", 'CINE_TICS', 'OWNER')
    print('Synthetic wallet and product ready; existing balances and stock preserved')


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('action', choices=['secrets', 'provision', 'migrate', 'seed', 'verify', 'status', 'test', 'demo_seed'])
    action = parser.parse_args().action
    if action == 'secrets':
        path = HERE / '.env'
        if path.exists():
            print('Credentials already exist; not overwritten')
            return
        fd = os.open(path, os.O_WRONLY | os.O_CREAT | os.O_EXCL, 0o600)
        with os.fdopen(fd, 'w') as f:
            for key in KEYS:
                f.write(f'{key}=Ct9{secrets.token_hex(18)}\n')
        print('Local credentials generated; do not commit .env')
    elif action == 'status':
        subprocess.run(COMPOSE + ['ps'], check=True)
    else:
        globals()[action]()


if __name__ == '__main__':
    main()
