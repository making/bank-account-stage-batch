import csv
import random
import datetime
import argparse
from typing import List, Dict, Any

def generate_test_data(num_records: int, month_end_date: datetime.date = None) -> List[Dict[str, Any]]:
    """
    指定された件数のテストデータを生成する

    Parameters:
        num_records (int): 生成するレコード数
        month_end_date (datetime.date, optional): 月末日。指定がない場合は現在の月の末日を使用

    Returns:
        List[Dict[str, Any]]: 生成されたデータのリスト
    """
    # 月末日が指定されていない場合、現在の月の末日を計算
    if month_end_date is None:
        today = datetime.date.today()
        # 翌月の1日から1日引くと月末日になる
        next_month = today.replace(day=28) + datetime.timedelta(days=4)
        month_end_date = next_month - datetime.timedelta(days=next_month.day)

    # ステージコードの定義
    stage_codes = ["NONE", "SILVER", "GOLD", "PLATINUM"]

    # 顧客データを生成
    data = []
    for i in range(num_records):
        # 顧客IDは「C」+8桁の数字で生成
        customer_id = f"C{random.randint(10000000, 99999999)}"

        # 現在のステージをランダムに選択（新規顧客の場合はNONEが多めになるよう調整）
        weighted_stages = ["NONE"] * 6 + ["SILVER"] * 3 + ["GOLD"] * 2 + ["PLATINUM"]
        current_stage_code = random.choice(weighted_stages)

        # 基本残高情報を生成
        # 高額な金額が出ないよう調整したが、条件を満たすデータと満たさないデータの両方が含まれるよう設定
        total_balance = random.randint(0, 10000000)  # 0円〜1000万円

        # シルバー条件を考慮したデータ生成
        # 現在シルバー以上のユーザーは基準値に近い値を生成する確率を高める
        if current_stage_code != "NONE":
            monthly_foreign_currency_purchase = random.choice([
                random.randint(0, 20000),  # 基準値未満
                random.randint(30000, 100000)  # 基準値以上
            ])
            monthly_investment_trust_purchase = random.choice([
                random.randint(0, 20000),  # 基準値未満
                random.randint(30000, 100000)  # 基準値以上
            ])
        else:
            # NONEユーザーは低い値が出やすくする
            monthly_foreign_currency_purchase = random.choice([
                random.randint(0, 20000),  # 基準値未満
                random.randint(30000, 100000),  # 基準値以上
                0, 0, 0  # 0円の確率を高める
            ])
            monthly_investment_trust_purchase = random.choice([
                random.randint(0, 20000),  # 基準値未満
                random.randint(30000, 100000),  # 基準値以上
                0, 0, 0  # 0円の確率を高める
            ])

        # ゴールド・プラチナ条件を考慮したデータ生成
        if current_stage_code in ["GOLD", "PLATINUM"]:
            # 現在ゴールド以上のユーザーは残高が高い確率を高める
            foreign_currency_balance = random.randint(1000000, 8000000)  # 100万円〜800万円
            investment_trust_balance = random.randint(1000000, 8000000)  # 100万円〜800万円
        elif current_stage_code == "SILVER":
            # シルバーユーザーは中程度の残高
            foreign_currency_balance = random.choice([
                random.randint(0, 2000000),  # 低め
                random.randint(2000000, 5000000)  # 中程度
            ])
            investment_trust_balance = random.choice([
                random.randint(0, 2000000),  # 低め
                random.randint(2000000, 5000000)  # 中程度
            ])
        else:
            # NONEユーザーは低い残高が多い
            foreign_currency_balance = random.choice([
                random.randint(0, 3000000),
                0, 0, 0  # 0円の確率を高める
            ])
            investment_trust_balance = random.choice([
                random.randint(0, 3000000),
                0, 0, 0  # 0円の確率を高める
            ])

        # ランクアップ条件のデータ生成
        # 住宅ローン残高（あるかないかのみが重要）
        housing_loan_balance = random.choice([
            0,  # ローンなし
            0,  # ローンなし
            random.randint(1000000, 50000000)  # ローンあり（100万円〜5000万円）
        ])

        # FX取引量
        if current_stage_code in ["GOLD", "PLATINUM"]:
            # 上位ステージのユーザーはFX取引が多い傾向
            monthly_fx_trading_volume = random.choice([
                random.randint(0, 500),  # 基準値未満
                random.randint(1000, 5000)  # 基準値以上
            ])
        else:
            # 下位ステージのユーザーはFX取引が少ない傾向
            monthly_fx_trading_volume = random.choice([
                random.randint(0, 500),  # 基準値未満
                random.randint(1000, 5000),  # 基準値以上
                0, 0, 0  # 0件の確率を高める
            ])

        record = {
            "customer_id": customer_id,
            "current_stage_code": current_stage_code,
            "month_end_date": month_end_date.strftime("%Y-%m-%d"),
            "total_balance": total_balance,
            "foreign_currency_balance": foreign_currency_balance,
            "investment_trust_balance": investment_trust_balance,
            "monthly_foreign_currency_purchase": monthly_foreign_currency_purchase,
            "monthly_investment_trust_purchase": monthly_investment_trust_purchase,
            "housing_loan_balance": housing_loan_balance,
            "monthly_fx_trading_volume": monthly_fx_trading_volume
        }

        data.append(record)

    return data

def save_to_csv(data: List[Dict[str, Any]], file_path: str):
    """
    データをCSVファイルに保存する

    Parameters:
        data (List[Dict[str, Any]]): 保存するデータ
        file_path (str): 出力ファイルパス
    """
    # フィールド名の順序を定義
    fieldnames = [
        "customer_id",
        "current_stage_code",
        "month_end_date",
        "total_balance",
        "foreign_currency_balance",
        "investment_trust_balance",
        "monthly_foreign_currency_purchase",
        "monthly_investment_trust_purchase",
        "housing_loan_balance",
        "monthly_fx_trading_volume"
    ]

    with open(file_path, mode='w', newline='', encoding='utf-8') as file:
        writer = csv.DictWriter(file, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(data)

    print(f"{len(data)}件のデータを{file_path}に保存しました。")

def analyze_test_data(data):
    """
    生成したテストデータの分析を行い、条件を満たす顧客の分布を表示する

    Parameters:
        data (List[Dict]): 分析するデータ
    """
    stage_counts = {"NONE": 0, "SILVER": 0, "GOLD": 0, "PLATINUM": 0}

    # 各顧客の条件達成状況をチェック
    silver_conditions = 0
    gold_conditions = 0
    platinum_conditions = 0
    rank_up_conditions = 0

    for record in data:
        # 現在のステージカウント
        current_stage = record["current_stage_code"]
        stage_counts[current_stage] += 1

        # シルバー条件チェック
        if (record["total_balance"] >= 3000000 or
            record["monthly_foreign_currency_purchase"] >= 30000 or
            record["monthly_investment_trust_purchase"] >= 30000):
            silver_conditions += 1

        # 合計投資残高
        combined_balance = record["foreign_currency_balance"] + record["investment_trust_balance"]

        # ゴールド条件チェック
        if 5000000 <= combined_balance < 10000000:
            gold_conditions += 1

        # プラチナ条件チェック
        if combined_balance >= 10000000:
            platinum_conditions += 1

        # ランクアップ条件チェック
        rank_up_count = 0
        if record["housing_loan_balance"] > 0:
            rank_up_count += 1
        if record["monthly_fx_trading_volume"] >= 1000:
            rank_up_count += 1

        if rank_up_count > 0:
            rank_up_conditions += 1

    # 結果出力
    print("\n===== 生成データ分析 =====")
    print(f"合計データ件数: {len(data)}")
    print("\n◆ 現在のステージ分布:")
    for stage, count in stage_counts.items():
        print(f"  - {stage}: {count}件 ({count/len(data)*100:.1f}%)")

    print("\n◆ 条件達成状況:")
    print(f"  - シルバー条件達成: {silver_conditions}件 ({silver_conditions/len(data)*100:.1f}%)")
    print(f"  - ゴールド条件達成: {gold_conditions}件 ({gold_conditions/len(data)*100:.1f}%)")
    print(f"  - プラチナ条件達成: {platinum_conditions}件 ({platinum_conditions/len(data)*100:.1f}%)")
    print(f"  - ランクアップ条件達成: {rank_up_conditions}件 ({rank_up_conditions/len(data)*100:.1f}%)")

def main():
    """
    メイン実行関数
    """
    parser = argparse.ArgumentParser(description='銀行口座ステージ判定用のテストデータを生成します')
    parser.add_argument('-n', '--num_records', type=int, default=100,
                        help='生成するレコード数 (デフォルト: 100)')
    parser.add_argument('-o', '--output', type=str, default='bank_account_stage_test_data.csv',
                        help='出力ファイルパス (デフォルト: bank_account_stage_test_data.csv)')
    parser.add_argument('-d', '--date', type=str,
                        help='月末日 (YYYY-MM-DD形式、指定がない場合は現在の月の末日)')

    args = parser.parse_args()

    # 月末日の設定
    month_end_date = None
    if args.date:
        try:
            month_end_date = datetime.datetime.strptime(args.date, "%Y-%m-%d").date()
        except ValueError:
            print("日付の形式が正しくありません。YYYY-MM-DD形式で指定してください。")
            return

    # データ生成
    print(f"{args.num_records}件のテストデータを生成しています...")
    data = generate_test_data(args.num_records, month_end_date)

    # 分析
    analyze_test_data(data)

    # CSVファイルに保存
    save_to_csv(data, args.output)

if __name__ == "__main__":
    main()