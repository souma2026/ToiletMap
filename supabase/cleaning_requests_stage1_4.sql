-- ============================================================================
-- ToiletMap 清掃依頼機能: 第1〜第3段階
--
-- 実装内容
--   1. NORMAL / REQUESTED / IN_PROGRESS / COMPLETED の4状態
--   2. cleaning_requests テーブル
--   3. 清掃依頼RPC
--   4. 清掃引受RPC（同時操作対策あり）
--   5. 清掃担当キャンセルRPC
--
-- Supabase Dashboard > SQL Editor で、このファイル全体を実行してください。
--
-- 注意
--   清掃完了・報酬ポイント付与・デイリーポイントは次の段階で実装します。
--   現段階の request_points_used は 0、予定報酬は 5pt です。
-- ============================================================================

begin;


-- ----------------------------------------------------------------------------
-- 1. toilets の清掃状態カラムを4状態へ拡張
-- ----------------------------------------------------------------------------
alter table public.toilets
    add column if not exists cleaning_status text not null default 'NORMAL';

alter table public.toilets
    add column if not exists cleaning_reward_points integer not null default 0;

alter table public.toilets
    add column if not exists cleaning_requested_by uuid null;


update public.toilets
   set cleaning_status = 'NORMAL'
 where cleaning_status is null
    or cleaning_status not in (
        'NORMAL',
        'REQUESTED',
        'IN_PROGRESS',
        'COMPLETED'
    );

update public.toilets
   set cleaning_reward_points = 0
 where cleaning_reward_points is null
    or cleaning_reward_points < 0;


-- 過去の「NORMAL / REQUESTEDのみ」を許可するCHECK制約を削除する。
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
    alter column cleaning_status set default 'NORMAL';

alter table public.toilets
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
-- 2. 清掃依頼テーブル
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
    created_at timestamptz not null default now(),

    constraint cleaning_requests_status_check
        check (
            status in (
                'REQUESTED',
                'IN_PROGRESS',
                'COMPLETED'
            )
        ),

    constraint cleaning_requests_request_points_nonnegative
        check (request_points_used >= 0),

    constraint cleaning_requests_reward_points_nonnegative
        check (reward_points >= 0)
);


-- 同名テーブルを以前の試行で作成済みでも、必要列を補完する。
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


-- 不完全な試行データを安全な値へ補正する。
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


-- 対象または依頼者が特定できない試行データは利用しない。
delete from public.cleaning_requests
 where toilet_id is null
    or btrim(toilet_id) = ''
    or requester_id is null
    or not exists (
        select 1
          from auth.users as auth_user
         where auth_user.id = cleaning_requests.requester_id
    );


-- 削除済みユーザーが担当者として残っている場合は募集状態へ戻す。
update public.cleaning_requests
   set cleaner_id = null,
       status = 'REQUESTED',
       accepted_at = null
 where cleaner_id is not null
   and not exists (
       select 1
         from auth.users as auth_user
        where auth_user.id = cleaning_requests.cleaner_id
   );


-- 状態と担当者・日時の組み合わせを正規化する。
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


-- 以前の試行で付いた状態CHECK制約を置き換える。
do $$
declare
    v_constraint record;
begin
    for v_constraint in
        select conname
          from pg_constraint
         where conrelid = 'public.cleaning_requests'::regclass
           and contype = 'c'
           and pg_get_constraintdef(oid) ilike '%status%'
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


-- primary key が無い試行テーブルにのみ追加する。
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
    check (
        status in (
            'REQUESTED',
            'IN_PROGRESS',
            'COMPLETED'
        )
    );


do $$
begin
    if not exists (
        select 1
          from pg_constraint
         where conrelid = 'public.cleaning_requests'::regclass
           and conname = 'cleaning_requests_request_points_nonnegative'
    ) then
        alter table public.cleaning_requests
            add constraint cleaning_requests_request_points_nonnegative
            check (request_points_used >= 0);
    end if;

    if not exists (
        select 1
          from pg_constraint
         where conrelid = 'public.cleaning_requests'::regclass
           and conname = 'cleaning_requests_reward_points_nonnegative'
    ) then
        alter table public.cleaning_requests
            add constraint cleaning_requests_reward_points_nonnegative
            check (reward_points >= 0);
    end if;
end
$$;


-- 以前の試行で同じトイレに複数の有効依頼が残っていた場合は、
-- 最新1件だけを有効にし、それ以外を履歴状態へ移す。
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

create index if not exists
    cleaning_requests_requested_at_idx
on public.cleaning_requests (requested_at desc);


-- ----------------------------------------------------------------------------
-- 3. 旧実装のREQUESTEDデータを cleaning_requests へ移行
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
    case
        when coalesce(toilet.cleaning_reward_points, 0) > 0
            then toilet.cleaning_reward_points
        else 5
    end,
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


-- 有効依頼を toilets の表示状態へ同期する。
update public.toilets as toilet
   set cleaning_status = cleaning_request.status,
       cleaning_reward_points = cleaning_request.reward_points,
       cleaning_requested_by = cleaning_request.requester_id
  from public.cleaning_requests as cleaning_request
 where toilet.id::text = cleaning_request.toilet_id
   and cleaning_request.status in ('REQUESTED', 'IN_PROGRESS');


-- 有効依頼を特定できない古い表示状態は通常へ戻す。
update public.toilets as toilet
   set cleaning_status = 'NORMAL',
       cleaning_reward_points = 0,
       cleaning_requested_by = null
 where toilet.cleaning_status in ('REQUESTED', 'IN_PROGRESS', 'COMPLETED')
   and not exists (
       select 1
         from public.cleaning_requests as cleaning_request
        where cleaning_request.toilet_id = toilet.id::text
          and cleaning_request.status in ('REQUESTED', 'IN_PROGRESS')
   );


-- ----------------------------------------------------------------------------
-- 4. RLS
-- ----------------------------------------------------------------------------
alter table public.cleaning_requests enable row level security;

revoke all on table public.cleaning_requests from public;
revoke all on table public.cleaning_requests from anon;
revoke insert, update, delete on table public.cleaning_requests from authenticated;
grant select on table public.cleaning_requests to authenticated;


drop policy if exists
    cleaning_requests_select_authenticated
on public.cleaning_requests;

create policy
    cleaning_requests_select_authenticated
on public.cleaning_requests
for select
to authenticated
using (true);


-- ----------------------------------------------------------------------------
-- 5. 旧RPCを削除
-- ----------------------------------------------------------------------------
drop function if exists public.request_cleaning_with_points(text, integer);
drop function if exists public.mark_toilet_cleaned_with_points(text);


-- ----------------------------------------------------------------------------
-- 6. 清掃依頼RPC
-- ----------------------------------------------------------------------------
create or replace function public.request_cleaning(
    p_toilet_id text
)
returns void
language plpgsql
security definer
set search_path = pg_catalog, public, auth
as $$
declare
    v_user_id uuid;
    v_cleaning_status text;
begin
    v_user_id := auth.uid();

    if v_user_id is null then
        raise exception '清掃を依頼するにはログインが必要です';
    end if;

    if p_toilet_id is null or btrim(p_toilet_id) = '' then
        raise exception '対象のトイレを選択してください';
    end if;

    -- トイレ行をロックし、同じトイレへの二重依頼を防ぐ。
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
        0,
        5,
        now(),
        now()
    );

    update public.toilets
       set cleaning_status = 'REQUESTED',
           cleaning_reward_points = 5,
           cleaning_requested_by = v_user_id
     where id::text = p_toilet_id;
end;
$$;


-- ----------------------------------------------------------------------------
-- 7. 清掃引受RPC
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

    if p_cleaning_request_id is null then
        raise exception '清掃依頼を選択してください';
    end if;

    -- 行ロックにより、同時に押しても最初の1人だけが成功する。
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
        raise exception 'この清掃依頼は、すでにほかのユーザーが引き受けています。';
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
-- 8. 清掃担当キャンセルRPC
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

    if p_cleaning_request_id is null then
        raise exception '清掃依頼を選択してください';
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

    if not found then
        raise exception '対象のトイレが見つかりません';
    end if;
end;
$$;


-- ----------------------------------------------------------------------------
-- 9. 清掃完了RPC（第4段階）
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
begin
    v_user_id := auth.uid();

    if v_user_id is null then
        raise exception '清掃を完了するにはログインが必要です';
    end if;

    if p_cleaning_request_id is null then
        raise exception '清掃依頼を選択してください';
    end if;

    -- 同じ依頼が同時に完了処理されても1回だけ成功するよう行ロックを取得する。
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
        raise exception '清掃担当者本人だけが清掃完了を実行できます';
    end if;

    update public.cleaning_requests
       set status = 'COMPLETED',
           completed_at = now()
     where id = p_cleaning_request_id;

    update public.toilets
       set cleaning_status = 'NORMAL',
           last_cleaned_at_millis = floor(
               extract(epoch from clock_timestamp()) * 1000
           )::bigint,
           cleaning_reward_points = 0,
           cleaning_requested_by = null
     where id::text = v_request.toilet_id;

    if not found then
        raise exception '対象のトイレが見つかりません';
    end if;

    -- 第4段階ではポイントは増減しない。
    -- reward_points は履歴として cleaning_requests に残す。
end;
$$;

-- ----------------------------------------------------------------------------
-- 10. RPC権限
-- ----------------------------------------------------------------------------
revoke all on function public.request_cleaning(text) from public;
revoke all on function public.accept_cleaning(uuid) from public;
revoke all on function public.cancel_cleaning(uuid) from public;
revoke all on function public.complete_cleaning(uuid) from public;

revoke all on function public.request_cleaning(text) from anon;
revoke all on function public.accept_cleaning(uuid) from anon;
revoke all on function public.cancel_cleaning(uuid) from anon;
revoke all on function public.complete_cleaning(uuid) from anon;

grant execute on function public.request_cleaning(text) to authenticated;
grant execute on function public.accept_cleaning(uuid) to authenticated;
grant execute on function public.cancel_cleaning(uuid) to authenticated;
grant execute on function public.complete_cleaning(uuid) to authenticated;


commit;
