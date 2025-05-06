# 銀行口座ステージ月次バッチ処理

Spring Batchの練習題材として、銀行口座ステージの月次判定バッチ処理をで実装しましょう。

このバッチ処理は、月末の残高や取引状況に基づいて顧客のステージを判定し、翌月の1ヶ月間適用される優遇特典を決定します。

バッチ処理の全体フローは以下のようになります。

1. 入力CSVファイルの読み込み
2. CSVデータをバッチ処理用モデルに変換
3. 基本ステージの判定
4. ランクアップ条件の評価と適用
5. 結果の保存
6. ステージ遷移の記録

バッチ処理の詳細は[こちら](docs/design.md)を参照してください。


> [!NOTE]
> ソニー銀行の[優遇プログラム Club S](https://moneykit.net/visitor/fx/fx29.html)
> のステージ獲得条件を参考にし、少しだけシンプルにした条件になっています。

想定のユースケースで処理の入出力がどのようになるかのシミュレーションは[こちら](docs/usecase.md)を参照してください。
実装したバッチ処理がこの全ユースケースのテストを満たせばOKです。

SQLのみでこのシミュレーションを実行してみたい場合は、[こちら](docs/usecase-sql.md)を参照してください。

## バッチ処理の実装

1. **実力のある人** ... [設計書](docs/design.md)を参照して、0からSpring Batchで実装してみてください。[ユースケース](docs/usecase.md)を満たすようにテストケースを実装してください。
2. **プロジェクトの雛形が欲しい人** ... [`step-0-scaffolding`ブランチ](https://github.com/making/bank-account-stage-batch/tree/step-0-scaffolding)から始めてください。データベースのスキーマ、Jobの定義および、ItemProcessor, ItemWriterの空実装が含まれています。バッチ処理として実行可能な状態ですが、FAILします。<br>ユースケースを満たすためのテストケースもTestContainersを使って実装済みです。テストを満たすようにItemProcessorとItemWriterを実装するだけで良いです。
3. **ロジックの部品まで欲しい人** ... [`step-1-add-stage-pacakge`ブランチ](https://github.com/making/bank-account-stage-batch/tree/step-1-add-stage-pacakge)から始めてください。上記の2.に加えて、設計に従ったアカウント獲得の条件ロジックなどが`stage`パッケージに実装されています。この`stage`パッケージのクラス群を組み立ててItemProcessorとItemWriterを実装するだけで良いです。

`step-0-scaffolding`または`step-1-add-stage-pacakge`からフォークしてプロジェクトを作った場合、
該当のブランチにプルリクエストを送っていただければ、マージはしませんが、レビューコメントをいたします。

## テスト実行方法

TestContainersを使って、PostgreSQLのDockerコンテナを立ち上げて、テストを実行するので、Dockerのインストールが必要です。
次のコマンドでテストを実行できます。

```
./mvnw clean test
```

[ユースケース](docs/usecase.md)を満たすCSVファイルを読み込んでバッチ処理が行われ、データベースの書き込み結果が期待通りかどうかをチェックします。

ただし、初期状態では必ず失敗します。
まずは`com.example.bank.config.DataSourceConfig`に従って、`com.example.bank.job.BankAccountStageInput`を実装するところから始める必要があります。

## テストデータの生成方法

バッチ処理の入力となるCSVファイルを生成するためのスクリプトを用意しました。

```
cd scripts
python3 generate_bank_stage_test_data.py
```

スクリプトの詳細は[こちら](scripts/README.md)を参照してください。

## バッチ処理の実行方法

生成したファイルを使ってバッチ処理を実行したい場合は次のコマンドを実行してください。

```
./mvnw spring-boot:run -Dspring-boot.run.arguments="inputFile=file:./scripts/bank_account_stage_test_data.csv"
```

Docker Composeが起動してPostgreSQLのコンテナが立ち上がります。
バッチ終了後もコンテナは立ち上がったままになりますので、同じファイルで再度バッチ処理を実行すると(正しく実装されていれば)一意制約違反で失敗します。
必要に応じて`docker compose down`でコンテナを削除してください。

あるいはIDEで`src/test/java/com/example/bank/TestBankAccountStageBatchApplication.java`を実行しても良いです。
こちらの場合はTestContainersを使用するため、毎回コンテナがリセットされます。

実行可能jarを作成して実行することもできます。

```
./mvnw clean package -DskipTests 
```

この場合はPostgreSQLのDockerコンテナは立ち上がりませんので、プロパティでPostgreSQLの接続情報を指定してください。

```
java -jar target/bank-account-stage-batch-0.0.1-SNAPSHOT.jar --spring.datasource.url=jdbc:postgresql://localhost:32432/account --spring.datasource.username=myuser --spring.datasource.password=secret inputFile=file:./scripts/bank_account_stage_test_data.csv
```

デフォルトでSQLデバッグログが出力されます。無効にしたい場合は`logging.level.net.ttddyy.dsproxy.listener.logging.SLF4JQueryLoggingListener=info`を設定してください。

## アドバンスド課題

2026年1月1日から次の**ダイヤモンド**ステージの運用を開始できるようにしてください。

> ダイヤモンドステージは以下のいずれかの条件を満たせば獲得できます
> 
> * 月末の総残高が3億円以上
> * 月末の総残高が1億円以上かつ住宅ローン残高が1億円以上
> 
> なお、ダイヤモンドステージへは他のステージからの**ランクアップはできません**。