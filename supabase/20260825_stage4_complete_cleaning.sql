-- ToiletMap 清掃機能 第4段階: 清掃完了
-- 既に cleaning_requests_stage1_3.sql を実行済みの環境で1回実行してください。

begin;

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

revoke all on function public.complete_cleaning(uuid) from public;
revoke all on function public.complete_cleaning(uuid) from anon;
grant execute on function public.complete_cleaning(uuid) to authenticated;

commit;
