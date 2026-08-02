# Project tools

These scripts are intentionally kept separate from the application code because they are operational helpers, not runtime services.

- `JwtSecretGenerator.java` creates a random JWT signing secret.
- `seed.py` loads processed replay data into PostgreSQL. Run it from the repository root and provide `SEED_DB_URL` in `.env`.
