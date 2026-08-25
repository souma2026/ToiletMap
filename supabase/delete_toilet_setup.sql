-- ============================================================
-- ToiletMap: トイレ削除機能
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
