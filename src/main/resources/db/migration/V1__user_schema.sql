CREATE TABLE IF NOT EXISTS "users"
(
    "id"              TEXT        NOT NULL,
    "login"           TEXT        NOT NULL,
    "email_lowercase" TEXT        NOT NULL,
    "login_lowercase" TEXT        NOT NULL,
    "password"        TEXT        NOT NULL,
    "created_on"      TIMESTAMPTZ NOT NULL
);

ALTER TABLE "users"
    ADD CONSTRAINT "users_pk" PRIMARY KEY ("id");
CREATE UNIQUE INDEX "users_login_lowercase_ak" ON "users" ("login_lowercase");
CREATE UNIQUE INDEX "users_email_lowercase_ak" ON "users" ("email_lowercase");