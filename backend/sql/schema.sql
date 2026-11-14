-- Derycode ID + Derycode App Market schema (Phase 2 target).
create table if not exists dery_accounts (
  id            uuid primary key default gen_random_uuid(),
  derycode_id   text unique not null,
  email         text unique not null,
  password_hash text not null,
  display_name  text,
  created_at    timestamptz not null default now(),
  disabled_at   timestamptz
);

create table if not exists dery_devices (
  id           uuid primary key default gen_random_uuid(),
  account_id   uuid not null references dery_accounts(id) on delete cascade,
  device_name  text not null,
  platform     text not null check (platform in ('mobile','desktop','tablet')),
  build        text,
  last_seen_at timestamptz default now(),
  revoked_at   timestamptz
);

create table if not exists dery_market_apps (
  id          text primary key,
  name        text not null,
  developer   text not null,
  summary     text,
  version     text,
  icon_ref    text,
  published   boolean not null default false
);

create table if not exists dery_installs (
  account_id uuid not null references dery_accounts(id) on delete cascade,
  app_id     text not null references dery_market_apps(id) on delete cascade,
  installed_at timestamptz not null default now(),
  primary key (account_id, app_id)
);

create index if not exists dery_devices_account_idx on dery_devices(account_id);
