-- For a new RAMISA Supabase database. Existing deployments should migrate their
-- policies and columns against their current schema before applying this file.
create table if not exists public.community_messages (
    id uuid primary key,
    user_id uuid not null references auth.users (id) on delete cascade,
    user_name text not null check (length(user_name) between 1 and 120),
    message text not null check (length(message) between 1 and 4000),
    is_sos_alert boolean not null default false,
    created_at timestamptz not null default now()
);

alter table public.community_messages enable row level security;
revoke all on public.community_messages from anon, authenticated;
grant select (id, user_name, message, is_sos_alert, created_at)
    on public.community_messages to authenticated;
grant insert on public.community_messages to authenticated;

create policy "Signed-in members read community messages"
    on public.community_messages for select to authenticated
    using (true);

create policy "Members post under their own account"
    on public.community_messages for insert to authenticated
    with check ((select auth.uid()) = user_id);
