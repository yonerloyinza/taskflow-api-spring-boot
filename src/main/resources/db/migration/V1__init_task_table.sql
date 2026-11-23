-- Flyway Migration: V1__init_task_table.sql
-- Tabla compartida "Task" del ecosistema TaskFlow.
-- NestJS/Prisma la crea con columnas camelCase y tipos enum; esta migracion es
-- idempotente: no altera un esquema existente, solo garantiza tipos, tabla e indices.

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'TaskStatus') THEN
        CREATE TYPE "TaskStatus" AS ENUM ('pendiente', 'en_curso', 'hecha');
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'TaskPriority') THEN
        CREATE TYPE "TaskPriority" AS ENUM ('baja', 'media', 'alta');
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS "Task" (
    id          text PRIMARY KEY,
    title       varchar(100) NOT NULL,
    description varchar(500),
    status      "TaskStatus"   NOT NULL DEFAULT 'pendiente',
    priority    "TaskPriority" NOT NULL DEFAULT 'media',
    assignee    varchar(100) NOT NULL,
    "dueDate"   timestamp(3) NOT NULL,
    "createdAt" timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" timestamp(3) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_task_status ON "Task" (status);
CREATE INDEX IF NOT EXISTS idx_task_priority ON "Task" (priority);
CREATE INDEX IF NOT EXISTS idx_task_assignee ON "Task" (assignee);
CREATE INDEX IF NOT EXISTS idx_task_created_at ON "Task" ("createdAt" DESC);