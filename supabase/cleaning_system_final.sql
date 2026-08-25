-- ============================================================================
-- ToiletMap 清掃依頼 + 可変報酬ポイント + 清掃引受 + 清掃完了 + 削除
-- ============================================================================
-- Supabase Dashboard > SQL Editor で、このファイル全体を1回実行してください。
--
-- 最終仕様
--   1. 依頼者が 1～10000pt の報酬を設定して清掃依頼
--   2. 依頼時に依頼者の points から報酬分を差し引く
--   3. 別ユーザーが清掃を引き受けると IN_PROGRESS
--   4. 引き受けた本人だけが清掃完了できる
--   5. 完了時に清掃者へ報酬ポイントを付与
--   6. 削除時、未完了の依頼があれば依頼者へ報酬を返金
-- ============================================================================

begin;


-- ----------------------------------------------------------------------------
-- 1. profiles / toilets の必要列
-- ----------------------------------------------------------------------------
alter table public.profiles
    add column if not exists points integer not null default 0;

alter table public.toilets
    add column if not exists cleaning_status text not null default 'NORMAL';

alter table public.toilets
    add column if not exists cleaning_reward_points integer not null default 0;

alter table public.toilets
    add column if not exists cleaning_requested_by uuid null;

alter table public.toilets
    add column if not exists last_cleaned_at_millis bigint null;

update public.profiles
   set points = greatest(coalesce(points, 0), 0);

update public.toilets
   set cleaning_reward_points = greatest(coalesce(cleaning_reward_points, 0), 0),
       cleaning_status = case
           when cleaning_status in ('NORMAL', 'REQUESTED', 'IN_PROGRESS', 'COMPLETED')
               then cleaning_status
           else 'NORMAL'
       end;


-- cleaning_status の古い CHECK を置き換える。
do $$
declare
    v_constraint record;
begin
    for v_constraint in
        select conname
          from pg_constraint
         where conrelid = 'public.toilets'::regclass
           and contype = 'c'
           and pg_get_constraintdef(oid) ilike '%cleaning_status%'
    loop
        execute format(
            'alter table public.toilets drop constraint %I',
            v_constraint.conname
        );
    end loop;
end
$$;

alter table public.toilets
    alter column cleaning_status set default 'NORMAL',
    alter column cleaning_status set not null;

alter table public.toilets
    add constraint toilets_cleaning_status_check
    check (
        cleaning_status in (
            'NORMAL',
            'REQUESTED',
            'IN_PROGRESS',
            'COMPLETED'
        )
    );


-- ----------------------------------------------------------------------------
-- 2. cleaning_requests
-- ----------------------------------------------------------------------------
create table if not exists public.cleaning_requests (
    id uuid primary key default gen_random_uuid(),
    toilet_id text not null,
    requester_id uuid not null references auth.users(id) on delete cascade,
    cleaner_id uuid null references auth.users(id) on delete set null,
    status text not null default 'REQUESTED',
    request_points_used integer not null default 0,
    reward_points integer not null default 5,
    requested_at timestamptz not null default now(),
    accepted_at timestamptz null,
    completed_at timestamptz null,
    created_at timestamptz not null default now()
);

alter table public.cleaning_requests
    add column if not exists id uuid default gen_random_uuid();
alter table public.cleaning_requests
    add column if not exists toilet_id text;
alter table public.cleaning_requests
    add column if not exists requester_id uuid;
alter table public.cleaning_requests
    add column if not exists cleaner_id uuid;
alter table public.cleaning_requests
    add column if not exists status text default 'REQUESTED';
alter table public.cleaning_requests
    add column if not exists request_points_used integer default 0;
alter table public.cleaning_requests
    add column if not exists reward_points integer default 5;
alter table public.cleaning_requests
    add column if not exists requested_at timestamptz default now();
alter table public.cleaning_requests
    add column if not exists accepted_at timestamptz;
alter table public.cleaning_requests
    add column if not exists completed_at timestamptz;
alter table public.cleaning_requests
    add column if not exists created_at timestamptz default now();

-- toilet_id が過去に uuid で作られていても text に統一する。
-- このアプリでは Kotlin 側の Toilet.id / CleaningRequest.toiletId を String として扱う。
do $$
declare
    v_constraint record;
begin
    for v_constraint in
        select con.conname
          from pg_constraint con
          join pg_class rel on rel.oid = con.conrelid
          join pg_namespace nsp on nsp.oid = rel.relnamespace
         where nsp.nspname = 'public'
           and rel.relname = 'cleaning_requests'
           and con.contype = 'f'
           and pg_get_constraintdef(con.oid) ilike '%toilet_id%'
    loop
        execute format(
            'alter table public.cleaning_requests drop constraint %I',
            v_constraint.conname
        );
    end loop;
end
$$;

alter table public.cleaning_requests
    alter column toilet_id type text using toilet_id::text;

update public.cleaning_requests
   set id = coalesce(id, gen_random_uuid()),
       status = case
           when status in ('REQUESTED', 'IN_PROGRESS', 'COMPLETED')
               then status
           else 'COMPLETED'
       end,
       request_points_used = greatest(coalesce(request_points_used, 0), 0),
       reward_points = greatest(coalesce(reward_points, 5), 0),
       requested_at = coalesce(requested_at, created_at, now()),
       created_at = coalesce(created_at, requested_at, now());

-- 対象トイレまたは依頼者が存在しない壊れたデータを除外。
delete from public.cleaning_requests as cleaning_request
 where cleaning_request.toilet_id is null
    or btrim(cleaning_request.toilet_id) = ''
    or cleaning_request.requester_id is null
    or not exists (
        select 1
          from public.toilets as toilet
         where toilet.id::text = cleaning_request.toilet_id
    )
    or not exists (
        select 1
          from auth.users as auth_user
         where auth_user.id = cleaning_request.requester_id
    );

-- 削除済み担当者を募集状態へ戻す。
update public.cleaning_requests as cleaning_request
   set cleaner_id = null,
       status = 'REQUESTED',
       accepted_at = null
 where cleaning_request.cleaner_id is not null
   and not exists (
       select 1
         from auth.users as auth_user
        where auth_user.id = cleaning_request.cleaner_id
   );

update public.cleaning_requests
   set cleaner_id = null,
       accepted_at = null
 where status = 'REQUESTED';

update public.cleaning_requests
   set status = 'REQUESTED',
       accepted_at = null
 where status = 'IN_PROGRESS'
   and cleaner_id is null;

update public.cleaning_requests
   set completed_at = coalesce(completed_at, now())
 where status = 'COMPLETED';

-- 古い状態 CHECK を置き換える。
do $$
declare
    v_constraint record;
begin
    for v_constraint in
        select conname
          from pg_constraint
         where conrelid = 'public.cleaning_requests'::regclass
           and contype = 'c'
           and (
               pg_get_constraintdef(oid) ilike '%status%'
               or pg_get_constraintdef(oid) ilike '%reward_points%'
               or pg_get_constraintdef(oid) ilike '%request_points_used%'
           )
    loop
        execute format(
            'alter table public.cleaning_requests drop constraint %I',
            v_constraint.conname
        );
    end loop;
end
$$;

alter table public.cleaning_requests
    alter column id set default gen_random_uuid(),
    alter column id set not null,
    alter column toilet_id set not null,
    alter column requester_id set not null,
    alter column status set default 'REQUESTED',
    alter column status set not null,
    alter column request_points_used set default 0,
    alter column request_points_used set not null,
    alter column reward_points set default 5,
    alter column reward_points set not null,
    alter column requested_at set default now(),
    alter column requested_at set not null,
    alter column created_at set default now(),
    alter column created_at set not null;

-- primary key が無い試行テーブルに追加。
do $$
begin
    if not exists (
        select 1
          from pg_constraint
         where conrelid = 'public.cleaning_requests'::regclass
           and contype = 'p'
    ) then
        alter table public.cleaning_requests
            add constraint cleaning_requests_pkey primary key (id);
    end if;
end
$$;

alter table public.cleaning_requests
    add constraint cleaning_requests_status_check
    check (status in ('REQUESTED', 'IN_PROGRESS', 'COMPLETED'));

alter table public.cleaning_requests
    add constraint cleaning_requests_request_points_nonnegative
    check (request_points_used >= 0);

alter table public.cleaning_requests
    add constraint cleaning_requests_reward_points_nonnegative
    check (reward_points >= 0);

-- 同一トイレに有効依頼が複数ある場合は最新1件だけ残す。
with ranked_active_requests as (
    select
        id,
        row_number() over (
            partition by toilet_id
            order by created_at desc, id desc
        ) as row_number_in_toilet
    from public.cleaning_requests
    where status in ('REQUESTED', 'IN_PROGRESS')
)
update public.cleaning_requests as cleaning_request
   set status = 'COMPLETED',
       completed_at = coalesce(cleaning_request.completed_at, now())
  from ranked_active_requests as ranked
 where cleaning_request.id = ranked.id
   and ranked.row_number_in_toilet > 1;

create unique index if not exists
    cleaning_requests_one_active_per_toilet
on public.cleaning_requests (toilet_id)
where status in ('REQUESTED', 'IN_PROGRESS');

create index if not exists
    cleaning_requests_cleaner_status_idx
on public.cleaning_requests (cleaner_id, status);

create index if not exists
    cleaning_requests_requester_status_idx
on public.cleaning_requests (requester_id, status);


-- ----------------------------------------------------------------------------
-- 3. 旧 toilets.REQUESTED データを cleaning_requests へ移行
-- ----------------------------------------------------------------------------
insert into public.cleaning_requests (
    toilet_id,
    requester_id,
    cleaner_id,
    status,
    request_points_used,
    reward_points,
    requested_at,
    created_at
)
select
    toilet.id::text,
    toilet.cleaning_requested_by,
    null,
    'REQUESTED',
    0,
    greatest(coalesce(toilet.cleaning_reward_points, 0), 0),
    now(),
    now()
from public.toilets as toilet
join auth.users as requester
  on requester.id = toilet.cleaning_requested_by
where toilet.cleaning_status = 'REQUESTED'
  and not exists (
      select 1
        from public.cleaning_requests as cleaning_request
       where cleaning_request.toilet_id = toilet.id::text
         and cleaning_request.status in ('REQUESTED', 'IN_PROGRESS')
  );

-- 有効依頼を toilets 表示状態へ同期。
update public.toilets as toilet
   set cleaning_status = cleaning_request.status,
       cleaning_reward_points = cleaning_request.reward_points,
       cleaning_requested_by = cleaning_request.requester_id
  from public.cleaning_requests as cleaning_request
 where toilet.id::text = cleaning_request.toilet_id
   and cleaning_request.status in ('REQUESTED', 'IN_PROGRESS');


-- ----------------------------------------------------------------------------
-- 4. RLS: 認証済みユーザーは読むだけ。状態変更は RPC のみ。
-- ----------------------------------------------------------------------------
alter table public.cleaning_requests enable row level security;

revoke all on table public.cleaning_requests from public;
revoke all on table public.cleaning_requests from anon;
revoke insert, update, delete on table public.cleaning_requests from authenticated;
grant select on table public.cleaning_requests to authenticated;

drop policy if exists cleaning_requests_select_authenticated
on public.cleaning_requests;

create policy cleaning_requests_select_authenticated
on public.cleaning_requests
for select
to authenticated
using (true);


-- ----------------------------------------------------------------------------
-- 5. 旧 RPC を整理
-- ----------------------------------------------------------------------------
drop function if exists public.request_cleaning(text);
drop function if exists public.request_cleaning(text, integer);
drop function if exists public.request_cleaning_with_points(text, integer);
drop function if exists public.mark_toilet_cleaned_with_points(text);
drop function if exists public.complete_cleaning(uuid);


-- ----------------------------------------------------------------------------
-- 6. 清掃依頼: 可変報酬ポイント
-- ----------------------------------------------------------------------------
create or replace function public.request_cleaning(
    p_toilet_id text,
    p_reward_points integer
)
returns void
language plpgsql
security definer
set search_path = pg_catalog, public, auth
as $$
declare
    v_user_id uuid;
    v_user_points integer;
    v_cleaning_status text;
begin
    v_user_id := auth.uid();

    if v_user_id is null then
        raise exception '清掃を依頼するにはログインが必要です';
    end if;

    if p_toilet_id is null or btrim(p_toilet_id) = '' then
        raise exception '対象のトイレを選択してください';
    end if;

    if p_reward_points is null or p_reward_points < 1 or p_reward_points > 10000 then
        raise exception '支払うポイントは1～10000ptで指定してください';
    end if;

    select profile.points
      into v_user_points
      from public.profiles as profile
     where profile.id = v_user_id
     for update;

    if not found then
        raise exception 'ユーザープロフィールが見つかりません';
    end if;

    if v_user_points < p_reward_points then
        raise exception '所持ポイントが不足しています（所持: %pt / 必要: %pt）',
            v_user_points,
            p_reward_points;
    end if;

    select toilet.cleaning_status
      into v_cleaning_status
      from public.toilets as toilet
     where toilet.id::text = p_toilet_id
     for update;

    if not found then
        raise exception '対象のトイレが見つかりません';
    end if;

    if v_cleaning_status <> 'NORMAL' then
        raise exception 'このトイレにはすでに清掃依頼があります';
    end if;

    if exists (
        select 1
          from public.cleaning_requests as cleaning_request
         where cleaning_request.toilet_id = p_toilet_id
           and cleaning_request.status in ('REQUESTED', 'IN_PROGRESS')
    ) then
        raise exception 'このトイレにはすでに清掃依頼があります';
    end if;

    update public.profiles
       set points = points - p_reward_points
     where id = v_user_id;

    insert into public.cleaning_requests (
        toilet_id,
        requester_id,
        cleaner_id,
        status,
        request_points_used,
        reward_points,
        requested_at,
        created_at
    ) values (
        p_toilet_id,
        v_user_id,
        null,
        'REQUESTED',
        p_reward_points,
        p_reward_points,
        now(),
        now()
    );

    update public.toilets
       set cleaning_status = 'REQUESTED',
           cleaning_reward_points = p_reward_points,
           cleaning_requested_by = v_user_id
     where id::text = p_toilet_id;
end;
$$;


-- ----------------------------------------------------------------------------
-- 7. 清掃引受
-- ----------------------------------------------------------------------------
create or replace function public.accept_cleaning(
    p_cleaning_request_id uuid
)
returns void
language plpgsql
security definer
set search_path = pg_catalog, public, auth
as $$
declare
    v_user_id uuid;
    v_request public.cleaning_requests%rowtype;
begin
    v_user_id := auth.uid();

    if v_user_id is null then
        raise exception '清掃を引き受けるにはログインが必要です';
    end if;

    select *
      into v_request
      from public.cleaning_requests
     where id = p_cleaning_request_id
     for update;

    if not found then
        raise exception '清掃依頼が見つかりません';
    end if;

    if v_request.status <> 'REQUESTED'
       or v_request.cleaner_id is not null then
        raise exception 'この清掃依頼は、すでにほかのユーザーが引き受けています';
    end if;

    if v_request.requester_id = v_user_id then
        raise exception '自分が出した清掃依頼は引き受けられません';
    end if;

    update public.cleaning_requests
       set cleaner_id = v_user_id,
           status = 'IN_PROGRESS',
           accepted_at = now()
     where id = p_cleaning_request_id;

    update public.toilets
       set cleaning_status = 'IN_PROGRESS',
           cleaning_reward_points = v_request.reward_points,
           cleaning_requested_by = v_request.requester_id
     where id::text = v_request.toilet_id;

    if not found then
        raise exception '対象のトイレが見つかりません';
    end if;
end;
$$;


-- ----------------------------------------------------------------------------
-- 8. 清掃完了: 担当者本人へ報酬付与
-- ----------------------------------------------------------------------------
create or replace function public.complete_cleaning(
    p_cleaning_request_id uuid
)
returns void
language plpgsql
security definer
set search_path = pg_catalog, public, auth
as $$
declare
    v_user_id uuid;
    v_request public.cleaning_requests%rowtype;
    v_now_millis bigint;
begin
    v_user_id := auth.uid();

    if v_user_id is null then
        raise exception '清掃完了を登録するにはログインが必要です';
    end if;

    select *
      into v_request
      from public.cleaning_requests
     where id = p_cleaning_request_id
     for update;

    if not found then
        raise exception '清掃依頼が見つかりません';
    end if;

    if v_request.status <> 'IN_PROGRESS' then
        raise exception 'この清掃依頼は現在、清掃中ではありません';
    end if;

    if v_request.cleaner_id is distinct from v_user_id then
        raise exception '清掃を引き受けた本人だけが完了できます';
    end if;

    perform 1
      from public.profiles
     where id = v_user_id
     for update;

    if not found then
        raise exception 'ユーザープロフィールが見つかりません';
    end if;

    -- 二重受取防止: request 行をロックしたまま一度だけ付与。
    update public.profiles
       set points = points + v_request.reward_points
     where id = v_user_id;

    update public.cleaning_requests
       set status = 'COMPLETED',
           completed_at = now()
     where id = p_cleaning_request_id;

    v_now_millis :=
        floor(extract(epoch from clock_timestamp()) * 1000)::bigint;

    update public.toilets
       set cleaning_status = 'NORMAL',
           last_cleaned_at_millis = v_now_millis,
           cleaning_reward_points = 0,
           cleaning_requested_by = null
     where id::text = v_request.toilet_id;

    if not found then
        raise exception '対象のトイレが見つかりません';
    end if;
end;
$$;


-- ----------------------------------------------------------------------------
-- 9. 清掃担当キャンセル: 依頼自体は残して再募集
-- ----------------------------------------------------------------------------
create or replace function public.cancel_cleaning(
    p_cleaning_request_id uuid
)
returns void
language plpgsql
security definer
set search_path = pg_catalog, public, auth
as $$
declare
    v_user_id uuid;
    v_request public.cleaning_requests%rowtype;
begin
    v_user_id := auth.uid();

    if v_user_id is null then
        raise exception '清掃担当をキャンセルするにはログインが必要です';
    end if;

    select *
      into v_request
      from public.cleaning_requests
     where id = p_cleaning_request_id
     for update;

    if not found then
        raise exception '清掃依頼が見つかりません';
    end if;

    if v_request.status <> 'IN_PROGRESS' then
        raise exception 'この清掃依頼は現在、清掃中ではありません';
    end if;

    if v_request.cleaner_id is distinct from v_user_id then
        raise exception '清掃担当者本人だけがキャンセルできます';
    end if;

    update public.cleaning_requests
       set cleaner_id = null,
           status = 'REQUESTED',
           accepted_at = null
     where id = p_cleaning_request_id;

    update public.toilets
       set cleaning_status = 'REQUESTED',
           cleaning_reward_points = v_request.reward_points,
           cleaning_requested_by = v_request.requester_id
     where id::text = v_request.toilet_id;
end;
$$;


-- ----------------------------------------------------------------------------
-- 10. 以前 uuid=text エラーを起こしていた不要トリガーを削除
--     toilet_reviews -> toilets は ON DELETE CASCADE のため不要。
-- ----------------------------------------------------------------------------
drop trigger if exists delete_reviews_after_toilet_delete
on public.toilets;

drop function if exists public.delete_reviews_for_deleted_toilet();


-- ----------------------------------------------------------------------------
-- 11. トイレ削除: ログイン中なら削除可能。未完了報酬は返金。
-- ----------------------------------------------------------------------------
create or replace function public.delete_own_toilet(
    p_toilet_id text
)
returns void
language plpgsql
security definer
set search_path = pg_catalog, public, auth
as $$
declare
    v_user_id uuid;
    v_toilet_reward integer := 0;
    v_toilet_requester uuid;
    v_request public.cleaning_requests%rowtype;
    v_has_active_request boolean := false;
begin
    v_user_id := auth.uid();

    if v_user_id is null then
        raise exception 'トイレを削除するにはログインが必要です';
    end if;

    if p_toilet_id is null or btrim(p_toilet_id) = '' then
        raise exception '削除するトイレが選択されていません';
    end if;

    select
        coalesce(toilet.cleaning_reward_points, 0),
        toilet.cleaning_requested_by
      into
        v_toilet_reward,
        v_toilet_requester
      from public.toilets as toilet
     where toilet.id::text = p_toilet_id
     for update;

    if not found then
        raise exception '対象のトイレが見つかりません';
    end if;

    select *
      into v_request
      from public.cleaning_requests
     where toilet_id = p_toilet_id
       and status in ('REQUESTED', 'IN_PROGRESS')
     order by created_at desc
     limit 1
     for update;

    v_has_active_request := found;

    if v_has_active_request then
        if v_request.reward_points > 0 then
            update public.profiles
               set points = points + v_request.reward_points
             where id = v_request.requester_id;
        end if;
    elsif v_toilet_reward > 0 and v_toilet_requester is not null then
        -- cleaning_requests 導入前の古い依頼も返金する。
        update public.profiles
           set points = points + v_toilet_reward
         where id = v_toilet_requester;
    end if;

    -- cleaning_requests.toilet_id は text のため手動で削除する。
    delete from public.cleaning_requests
     where toilet_id = p_toilet_id;

    -- 口コミは CASCADE がある環境でも、ない環境でも安全に先に消す。
    delete from public.toilet_reviews
     where toilet_id::text = p_toilet_id;

    delete from public.toilets
     where id::text = p_toilet_id;
end;
$$;


-- ----------------------------------------------------------------------------
-- 12. RPC 権限
-- ----------------------------------------------------------------------------
revoke all on function public.request_cleaning(text, integer) from public;
revoke all on function public.request_cleaning(text, integer) from anon;
revoke all on function public.accept_cleaning(uuid) from public;
revoke all on function public.accept_cleaning(uuid) from anon;
revoke all on function public.complete_cleaning(uuid) from public;
revoke all on function public.complete_cleaning(uuid) from anon;
revoke all on function public.cancel_cleaning(uuid) from public;
revoke all on function public.cancel_cleaning(uuid) from anon;
revoke all on function public.delete_own_toilet(text) from public;
revoke all on function public.delete_own_toilet(text) from anon;

grant execute on function public.request_cleaning(text, integer) to authenticated;
grant execute on function public.accept_cleaning(uuid) to authenticated;
grant execute on function public.complete_cleaning(uuid) to authenticated;
grant execute on function public.cancel_cleaning(uuid) to authenticated;
grant execute on function public.delete_own_toilet(text) to authenticated;

commit;
