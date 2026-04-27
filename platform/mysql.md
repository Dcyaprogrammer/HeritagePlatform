# MySQL CLI Cheat Sheet

This is a quick reference guide for common MySQL command-line operations, specifically tailored for the `heritage_platform` database.

## 1. Login & Connection

### Login with a specific user
If your root password is empty:
```bash
mysql -u root
```
If you have a password:
```bash
mysql -u root -p
```

### Exit MySQL
```sql
exit;
```
*(Alternatively, you can type `quit` or press `Ctrl + D`)*

---

## 2. Databases Operations

### View all databases
```sql
SHOW DATABASES;
```

### Select/Switch to a database
```sql
USE heritage_platform;
```

### Create a new database (if not exists)
```sql
CREATE DATABASE IF NOT EXISTS heritage_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Delete a database (Use with extreme caution!)
```sql
DROP DATABASE heritage_platform;
```

---

## 3. Tables Operations

### View all tables in the current database
*(Make sure you have run `USE heritage_platform;` first)*
```sql
SHOW TABLES;
```

### View table structure (columns, types, keys)
```sql
DESCRIBE users;
-- or the shorthand:
DESC users;
```

### Show the SQL statement used to create a table
```sql
SHOW CREATE TABLE resources;
```

### Delete a table (Use with caution!)
```sql
DROP TABLE comments;
```

---

## 4. Data Operations (CRUD)

### Query data
View all records in a table:
```sql
SELECT * FROM users;
```
View specific columns with a condition:
```sql
SELECT id, username, email FROM users WHERE status = 'ACTIVE';
```

### Insert data
```sql
INSERT INTO roles (name) VALUES ('ADMIN'), ('CONTRIBUTOR');
```

### Update data
```sql
UPDATE users SET status = 'ACTIVE' WHERE username = 'dopamine';
```

### Delete data
```sql
DELETE FROM resources WHERE status = 'DRAFT';
```

---

## 5. Helpful Tips

* **Always end SQL statements with a semicolon (`;`)**. If you press Enter without it, MySQL will just wait for you to finish on the next line (you'll see a `->` prompt). Just type `;` and press Enter to execute.
* **Case Sensitivity**: SQL keywords (`SELECT`, `SHOW`, `CREATE`) are generally case-insensitive, but it's best practice to uppercase them. Database and table names might be case-sensitive depending on your OS (macOS/Linux).
* **Cancel a command**: If you made a mistake and haven't hit enter with a `;` yet, you can type `\c` and hit enter to clear the current command.