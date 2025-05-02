## MyPortal
家計簿・水槽シミュレータなど、自分用の好きな機能を統合したアプリです。

## 使用技術
<p style="display: inline">
    <!-- 言語一覧 -->
    <img src="https://img.shields.io/badge/-Kotlin-0095D5.svg?logo=kotlin&style=plastic" alt="Kotlin">
</p>

## 目次
1. [プロジェクトについて](#プロジェクトについて)
2. [環境](#環境)

## プロジェクトについて
　本アプリは現在、タブを選択して3つの機能を利用できます。
<p style="text-align: center">
    <img src="Attached File/tabs.png" width="250" alt="tabs.png">
</p>

### 1. トップページ
　天気予報や、家計簿機能に保存されているデータのサマリーなどを掲載する予定の領域です。

### 2. 家計簿（BUDGET）

- 支出機能（EXPENDITURE）
  - 支出の内訳（EXPENDITURE BREAKDOWN）
  
  　月別または年別で、支出の内訳を確認できます。各カテゴリ名はクリック可能で、クリックするとより細かいカテゴリ別の内訳へ進む事ができます。円グラフ上部のパンくずリストをクリックして、大きなカテゴリに戻る事も可能です。
  <p style="text-align: center">
    <img src="Attached File/expenditure_breakdown_root.png" width="300" alt="expenditure_breakdown_root.png">
    <img src="Attached File/expenditure_breakdown_root_food.png" width="300" alt="expenditure_breakdown_root_food.png">
  </p>

  - 支出の登録（EXPENDITURE ENTRY）
  
  　支出の登録は入力すべき事項が多く、煩わしくなりがちな機能です。そこで本アプリでは、入力欄下部の入力窓に項目名・金額・備考をスペースで区切って入力してエンターキーを押すと、自動でデータを成形・カテゴライズする機能を実装しています。ユーザーが手動でカテゴリを修正する事も可能です。なお、カテゴライズのアルゴリズムには、入力された項目名とカテゴリのエイリアス間のレーベンシュタイン距離を使用しています。
  <p style="text-align: center">
    <img src="Attached File/expenditure_entry.gif" width="" alt="expenditure_entry.gif">
  </p>

  - 支出の年次推移（ANNUAL TREND OF EXPENDITURES）
  <p style="text-align: center">
    <img src="Attached File/annual_trend_of_expenditures.png" width="600" alt="annual_trend_of_expenditures.png">
  </p>


- 収入機能（INCOME）
  - 収入の推移（TREND OF INCOMES）
  - 収入の登録（INCOME ENTRY）
  <p style="text-align: center">
    <img src="Attached File/income.png" width="600" alt="income.png">
  </p>


- タイムライン（TIMELINE）

  　タイムラインには、支出・収入データが日付の新しい順に並んでいます。検索窓にスペースで区切ったキーワードを入力して、AND検索を行う事ができます。
  <p style="text-align: center">
    <img src="Attached File/timeline.png" width="600" alt="timeline.png">
  </p>

### 3. 水槽シミュレータ（AQUARIUM）
　動物の群れをシミュレーションするBoidアルゴリズムを主に使用した水槽シミュレータです。水質・空腹度・体力などを数値化しており、魚の運動に影響を与えます。魚の各品種ごとに最大加速度や巡航速度、群泳の度合いなどをクラスで定義しており、「群泳」など似た性質を持つ品種は抽象クラスで共通化もされています。<br>
　また、画面をクリックするとエサを与えることができます。魚はエサを食べると、数ヶ月単位の現実的なペースで体が大きく成長していきます。他の個体と体の大きさを比較して、危険だと感じると退避するように定義されている品種も存在します。
<p style="text-align: center">
  <img src="Attached File/aquarium_group.png" width="600" alt="aquarium_group.png">
</p>
<p style="text-align: center">
  <img src="Attached File/aquarium_feeding.png" width="600" alt="aquarium_feeding.png">
</p>

## 環境
追記予定
