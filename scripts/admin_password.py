"""Generate an initial administrator INSERT locally. Requires: pip install bcrypt."""
import getpass
import bcrypt

password = getpass.getpass('Initial admin password (at least 12 characters): ')
if len(password) < 12:
    raise SystemExit('Use at least 12 characters.')
if password != getpass.getpass('Repeat password: '):
    raise SystemExit('Passwords differ.')
encoded = bcrypt.hashpw(password.encode(), bcrypt.gensalt(rounds=12)).decode()
print("INSERT INTO employee (name, username, password, status, create_time, update_time) "
      "VALUES ('Administrator', 'admin', '" + encoded + "', 1, NOW(), NOW());")
