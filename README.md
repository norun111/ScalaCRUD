# 2020年度インターンScalaコース事前課題

Fringe81が実施するインターンScalaコースの事前課題です。

**本課題の内容およびソースコード等を他人に無断で共有することを固く禁じます。**

## 注意点

既存のファイルは特に理由がない限り消さないでください。  
特に、以下のファイルをGitに追加し忘れないように注意してください。

- .gitignore
- .scalafmt

## 課題内容

後述するAPI仕様を満たすWebアプリケーションを実装して下さい。  
APIの開発に必要なDBのテーブルの設計およびプログラムの開発を含みます。

既存のコードはあくまでサンプルなので、下記の開発環境に関わるもの以外は全て変更しても構いません。

### 環境

言語やライブラリは以下のものを使用して下さい。  
すでにbuild.sbtやproject/plugins.sbtで指定してあるので編集しないで下さい。

- Scala 2.12.xもしくは2.13.x
- JDK 1.8もしくは1.11あたりが安定しています
- Play 2.6.x
    - play-json
- sbt 1.x
- h2 1.4.x
- ScalikeJDBC 3.2.x

これ以外のものは使用しないようにして下さい。  
開発環境にはIDEのIntelliJ IDEAを推奨しますが、特に指定はしません。

## データベースについて

データベースは[h2](https://www.playframework.com/documentation/2.6.x/Developing-with-the-H2-Database)を使用します。  
テーブルスキーマおよび初期データはPlayの[Evolutions](https://www.playframework.com/documentation/2.6.x/Evolutions)という機能を用いて設定して下さい。

また、全てのテーブルの`id`にはDBの`auto_increment`を使用せず、`java.util.UUID.randomUUID().toString()`を使って生成するようにして下さい。

## 認証、ユーザーについて

今回のアプリケーションでは認証機能を含みません。  
ユーザーの新規登録、ログイン、ログアウトといった概念は考えなくて構いません。  
`conf/evolutions/default/1.sql`に書いてある`users`テーブルの定義をそのまま用いて下さい。  

## 実装対象のAPI仕様

### 共通事項

参照系はGET、更新系をPOSTとする。  

正常系はHTTPステータスコード200を返却する。

異常系は

- リクエストが不正な場合は400
  - どう不正なのかエラーメッセージを表示すること
- サーバ側でエラーが発生した場合は503

をHTTPステータスコードとして返却する。  

### API一覧

実装イメージとしてはTwitterのようなマイクロブログアプリケーション。

- 投稿/コメント一覧
- 新規投稿作成
- 投稿へのコメント一覧
- 投稿へのコメント作成（コメントへのコメント作成も可能）

### 投稿/コメント一覧
GET /posts

- request parameter
  
  - なし
- request header
  
  - なし
- response
  - 全ての投稿/コメントが投稿日降順に並ぶ
  - `parent_post_id`はその投稿がコメントである場合、そのコメント先のID
    - コメントでない場合はフィールドが無い or nullとする
  - `comment_count`はその投稿に対するコメント数
    - 配下のコメントがない場合はcomment_countは0となること

  ```json
  {
    "posts": [
      {
        "id": "EE6B25AA-BCD2-4F8D-B175-03D50B166D81",
        "user_id": "11111111-1111-1111-1111-111111111111",
        "text": "hello world",
        "comment_count": 1,
        "posted_at": "2018-05-01 12:34:56"
      },
      {
        "id": "A75BC6C7-2B76-48F2-8EC8-5BE51F9EB4FA",
        "user_id": "22222222-2222-2222-2222-222222222222",
        "text": "good morning",
        "parent_post_id": "EE6B25AA-BCD2-4F8D-B175-03D50B166D81",
        "comment_count": 2,
        "posted_at": "2018-05-01 11:20:10"
      },
      ...その他、省略...
    ]
  }
  ```

### 新規投稿作成
POST /posts/create

- request body
  - `{"user_id": "33333333-3333-3333-3333-333333333333", "text": "have a good night!"}`
  - `text`の長さは1文字以上100文字以下
  - `user_id`は存在するuserのIDであることを確認する
- response
  - `{"result": "OK"}`


### 投稿へのコメント一覧
GET /posts/:post\_id/comments

- request parameter
  - なし
  - 取得するコメント先の投稿のID(post\_id)はpathに含んでいる
  - コメントを指定することも可能で、その場合は、そのコメントのコメント一覧が取得できること
- response
  - `comments`の中見は`posts`と同じ
  - 存在しない`post_id`を指定した場合は空json(`{"comments": []}`)でよい

  ```json
  {
    "comments": [
      {
        "id": "A75BC6C7-2B76-48F2-8EC8-5BE51F9EB4FA",
        "user_id": "11111111-1111-1111-1111-111111111111",
        "text": "good morning",
        "parent_post_id": "EE6B25AA-BCD2-4F8D-B175-03D50B166D81",
        "comment_count": 2,
        "posted_at": "2018-05-01 11:20:10"
      }
    ]
  }
  ```

### 投稿へのコメント作成（コメントへのコメント作成も可能）
POST /posts/:post\_id/comments/create

- request body
  - `{"user_id": "11111111-1111-1111-1111-111111111111", "text": "thank you!"}`
  - コメント先の投稿のID(post\_id)はpathに含んでいる
  - `user_id`は存在するuserのIDであることを確認する
  - `post_id`は存在する投稿/コメントのIDであることを確認する
  - `text`は1文字以上100文字以下
- response
  - `{"result": "OK"}`
