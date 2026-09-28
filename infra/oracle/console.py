#!/usr/bin/env python3
"""Run the synthetic Oracle console without exposing passwords in command arguments."""
import os
import re
import subprocess
import sys
import manage

def main():
    args = sys.argv[1:] or ['catalog']
    env = os.environ.copy()
    env['CINE_JDBC_URL'] = 'jdbc:oracle:thin:@//127.0.0.1:1522/CINE_TICS'
    env['CINE_APP_PASSWORD'] = manage.credentials()['CINE_TICS_APP_PASSWORD']
    # Maven exec's argument string is parsed again: restrict demo inputs to a simple grammar.
    if any(not re.fullmatch(r'[A-Za-z0-9_-]+', arg) for arg in args):
        raise SystemExit('Use command names, alphanumeric request keys and integer IDs/quantities')
    result = subprocess.run([str(manage.ROOT/'Cine/mvnw'), '-q', '-f', str(manage.ROOT/'Cine/pom.xml'),
        'compile', 'exec:exec', '-Dexec.mainClass=com.penta.cinetics.consola.OracleConsole',
        '-Dexec.args=-classpath %classpath com.penta.cinetics.consola.OracleConsole ' + ' '.join(args)], env=env)
    raise SystemExit(result.returncode)

if __name__ == '__main__':
    main()
