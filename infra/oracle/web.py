#!/usr/bin/env python3
"""Run the local API or provision an operator. No secrets in process arguments."""
import argparse
import getpass
import os
from pathlib import Path
import subprocess
import manage


def main():
    parser = argparse.ArgumentParser()
    sub = parser.add_subparsers(dest='action', required=True)
    sub.add_parser('serve')
    user = sub.add_parser('create-user')
    user.add_argument('tenant', choices=manage.TENANTS)
    user.add_argument('role', choices=['CLIENTE', 'EMPLEADO', 'ADMIN'])
    args = parser.parse_args()
    env = os.environ.copy()
    for tenant in manage.TENANTS:
        env[f'{tenant}_APP_PASSWORD'] = manage.credentials()[f'{tenant}_APP_PASSWORD']
    maven = [str(manage.ROOT/'Cine/mvnw'), '-B', '-ntp', '-f', str(manage.ROOT/'Cine/pom.xml')]
    if args.action == 'serve':
        subprocess.run(['npm', 'ci'], cwd=manage.ROOT/'frontend', check=True)
        subprocess.run(['npm', 'run', 'build'], cwd=manage.ROOT/'frontend', check=True)
        subprocess.run(maven + ['clean', 'package', '-DskipTests'], env=env, check=True)
        java = str(Path(env['JAVA_HOME'])/'bin/java') if env.get('JAVA_HOME') else 'java'
        os.execvpe(java, [java, '-jar', str(manage.ROOT/'Cine/target/Cine-1.0-SNAPSHOT-web.jar')], env)
    else:
        for name, label in [('FIRST','Nombre'), ('LAST','Apellidos'), ('ADDRESS','Dirección'), ('PHONE','Teléfono'), ('EMAIL','Correo')]:
            env[f'CINE_USER_{name}'] = input(label+': ').strip()
        password = getpass.getpass('Contraseña (mínimo 8 caracteres): ')
        if password != getpass.getpass('Repetir contraseña: '):
            raise SystemExit('Las contraseñas no coinciden')
        env['CINE_USER_PASSWORD'] = password
        subprocess.run(maven + ['compile', 'exec:exec',
            '-Dexec.mainClass=com.penta.cinetics.web.ProvisionarUsuario',
            '-Dexec.args=-classpath %classpath com.penta.cinetics.web.ProvisionarUsuario '+args.tenant+' '+args.role], env=env, check=True)


if __name__ == '__main__':
    main()
