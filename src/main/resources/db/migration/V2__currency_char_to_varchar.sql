ALTER TABLE contributions
    ALTER COLUMN currency TYPE VARCHAR(3),
    ALTER COLUMN currency SET NOT NULL;

