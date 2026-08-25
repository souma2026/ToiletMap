-- ============================================================
-- ToiletMap: 清掃依頼ポイント機能
-- ============================================================
-- Supabase Dashboard > SQL Editor で、このファイル全体を1回実行してください。
--
-- 追加する内容:
-- 1. toilets.cleaning_reward_points
-- 2. toilets.cleaning_requested_by
-- 3. profiles.points（未作成の場合のみ）
-- 4. 依頼時のポイント支払いを安全に処理するRPC
-- 5. 清掃完了時のポイント付与を安全に処理するRPC
-- ============================================================


-- ------------------------------------------------------------
-- 必要なカラム
-- ------------------------------------------------------------
alter table public.toilets
    add column if not exists cleaning_reward_points integer not null default 0;

alter table public.toilets
    add column if not exists cleaning_requested_by uuid null;

alter table public.profiles
    add column if not exists points integer not null default 0;


-- 既存データを安全な値へ補正
update public.toilets
set cleaning_reward_points = 0
where cleaning_reward_points is null or cleaning_reward_points < 0;

update public.profiles
set points = 0
where points is null or points < 0;


-- ------------------------------------------------------------
-- CHECK制約（既に存在する場合は追加しない）
-- ------------------------------------------------------------
do $$
begin
    if not exists (
        select 1
        from pg_constraint
        where conname = 'toilets_cleaning_reward_points_nonnegative'
          and conrelid = 'public.toilets'::regclass
    ) then
        alter table public.toilets
            add constraint toilets_cleaning_reward_points_nonnegative
            check (cleaning_reward_points >= 0);
    end if;

    if not exists (
        select 1
        from pg_constraint
        where conname = 'profiles_points_nonnegative'
          and conrelid = 'public.profiles'::regclass
    ) then
        alter table public.profiles
            add constraint profiles_points_nonnegative
            check (points >= 0);
    end if;
end
$$;


-- ------------------------------------------------------------
-- 清掃依頼
-- ------------------------------------------------------------
-- 依頼者のポイント減算と、トイレへの報酬設定を
-- 同じDBトランザクション内で行う。
-- ------------------------------------------------------------
create or replace function public.request_cleaning_with_points(
    p_toilet_id text,
    p_reward_points integer
)
returns void
language plpgsql
security definer
set search_path = public, auth
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

    if p_reward_points is null or p_reward_points < 1 or p_reward_points > 10000 then
        raise exception '支払うポイントは1～10000ptで指定してください';
    end if;

    -- ユーザー行をロックして残高を確認
    select points
      into v_user_points
      from public.profiles
     where id = v_user_id
     for update;

    if not found then
        raise exception 'ユーザープロフィールが見つかりません';
    end if;

    if v_user_points < p_reward_points then
        raise exception '所持ポイントが不足しています（所持: %pt / 必要: %pt）',
            v_user_points,
            p_reward_points;
    end if;

    -- トイレ行をロックして、二重依頼を防止
    select cleaning_status
      into v_cleaning_status
      from public.toilets
     where id::text = p_toilet_id
     for update;

    if not found then
        raise exception '対象のトイレが見つかりません';
    end if;

    if v_cleaning_status <> 'NORMAL' then
        raise exception 'このトイレにはすでに清掃依頼があります';
    end if;

    -- 依頼者からポイントを支払う
    update public.profiles
       set points = points - p_reward_points
     where id = v_user_id;

    -- トイレへ報酬額を保存し、清掃待ちへ変更
    update public.toilets
       set cleaning_status = 'REQUESTED',
           cleaning_reward_points = p_reward_points,
           cleaning_requested_by = v_user_id
     where id::text = p_toilet_id;
end;
$$;


-- ------------------------------------------------------------
-- 清掃完了
-- ------------------------------------------------------------
-- 清掃者への報酬付与と、トイレを通常状態へ戻す処理を
-- 同じDBトランザクション内で行う。
-- 行ロックにより、同じ報酬を二重取得できないようにする。
-- ------------------------------------------------------------
create or replace function public.mark_toilet_cleaned_with_points(
    p_toilet_id text
)
returns void
language plpgsql
security definer
set search_path = public, auth
as $$
declare
    v_user_id uuid;
    v_reward_points integer;
    v_cleaning_status text;
    v_now_millis bigint;
begin
    v_user_id := auth.uid();

    if v_user_id is null then
        raise exception '清掃完了を登録するにはログインが必要です';
    end if;

    -- トイレ行をロックして、二重受取を防止
    select cleaning_status,
           coalesce(cleaning_reward_points, 0)
      into v_cleaning_status,
           v_reward_points
      from public.toilets
     where id::text = p_toilet_id
     for update;

    if not found then
        raise exception '対象のトイレが見つかりません';
    end if;

    if v_cleaning_status <> 'REQUESTED' then
        raise exception 'このトイレは現在、清掃依頼中ではありません';
    end if;

    -- 清掃者プロフィール行をロック
    perform 1
      from public.profiles
     where id = v_user_id
     for update;

    if not found then
        raise exception 'ユーザープロフィールが見つかりません';
    end if;

    -- 清掃者へ報酬を付与
    update public.profiles
       set points = points + v_reward_points
     where id = v_user_id;

    v_now_millis :=
        floor(extract(epoch from clock_timestamp()) * 1000)::bigint;

    -- 清掃完了。報酬は使用済みなので0へ戻す
    update public.toilets
       set cleaning_status = 'NORMAL',
           last_cleaned_at_millis = v_now_millis,
           cleaning_reward_points = 0,
           cleaning_requested_by = null
     where id::text = p_toilet_id;
end;
$$;


-- ------------------------------------------------------------
-- RPC権限
-- ------------------------------------------------------------
revoke all on function public.request_cleaning_with_points(text, integer) from public;
revoke all on function public.mark_toilet_cleaned_with_points(text) from public;

grant execute on function public.request_cleaning_with_points(text, integer) to authenticated;
grant execute on function public.mark_toilet_cleaned_with_points(text) to authenticated;


-- ============================================================
-- トイレ削除RPC
-- ============================================================
-- ログイン中ユーザーなら、作成者に関係なく任意のトイレを削除可能。
-- 清掃依頼中の報酬は依頼者へ返金してから削除する。
-- ============================================================

-- Supabase Dashboard > SQL Editor で、このファイル全体を1回実行してください。
--
-- 仕様:
-- ・ログイン中ユーザーなら、作成者に関係なく任意のトイレを削除可能
-- ・清掃依頼中の場合は、残っている報酬ポイントを依頼者へ返金
-- ・そのトイレに紐づく口コミを削除
-- ・最後にトイレ本体を削除
-- ・すべて1トランザクションで実行
-- ============================================================

create or replace function public.delete_own_toilet(
    p_toilet_id text
)
returns void
language plpgsql
security definer
set search_path = public, auth
as $$
declare
    v_user_id uuid;
    v_cleaning_status text;
    v_reward_points integer;
    v_requested_by uuid;
begin
    v_user_id := auth.uid();

    if v_user_id is null then
        raise exception 'トイレを削除するにはログインが必要です';
    end if;

    if p_toilet_id is null or btrim(p_toilet_id) = '' then
        raise exception '削除するトイレが選択されていません';
    end if;

    select cleaning_status,
           coalesce(cleaning_reward_points, 0),
           cleaning_requested_by
      into v_cleaning_status,
           v_reward_points,
           v_requested_by
      from public.toilets
     where id::text = p_toilet_id
     for update;

    if not found then
        raise exception '対象のトイレが見つかりません';
    end if;

    -- 清掃依頼中の報酬が残っている場合は依頼者へ返金
    if v_cleaning_status = 'REQUESTED'
       and v_reward_points > 0
       and v_requested_by is not null then

        update public.profiles
           set points = points + v_reward_points
         where id = v_requested_by;
    end if;

    -- 外部キー制約がある場合にも安全に削除できるよう、口コミを先に削除
    delete from public.toilet_reviews
     where toilet_id::text = p_toilet_id;

    delete from public.toilets
     where id::text = p_toilet_id;
end;
$$;

revoke all on function public.delete_own_toilet(text) from public;
grant execute on function public.delete_own_toilet(text) to authenticated;
