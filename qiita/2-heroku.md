# Vue.js + Django REST Framework + Android でTODOアプリを書いてみるテスト Part 2/?

## はじめに

**注意** : ここは作業メモ、大した情報は無いですょ。あと、Androidは今回も出てきません。

Androidのアプリを書いてみようかと思うのですが、それなりのことをやろうとするとどうしてもサーバー側もなんとかしないと便利なものが作れない気がしてます。
ということで、この記事では(私が飽きたり挫折したりしなければ)、簡単なTODOアプリを作ることを目標として、

- サーバー側を [Django REST Framework][DjangoRestFramework] で適当なものを書く
- せっかくなので Vue.js でwebからのインターフェースもつくってみる
- サーバーからRESTで持ってきてAndroid AppからもRecycler Viewをつかって表示したり、TODOの新規作成・変更できるようにする

ということをやっていきたいと思ってます。
今後この記事の続きが作成されるかは謎ですが(ぉ)、一応手元ではRecycler Viewで一覧表示するところくらいまでは確認済みであったりはします。

なお、この記事で作業しているリポジトリをGithubで公開しています。 > [rarewin/todoapp-study](https://github.com/rarewin/todoapp-study/tree/master/todoapp.server)
間違いの指摘や、もっと良い実装方法等について、紳士的な助言お待ちしております。

今回のパートでは、Androidアプリ……を作る前に、Androidアプリからjsonでデータを取得できるように、  
[前回](https://qiita.com/rarewin/items/c6a70689844eafe8c3a1) 作成したVue+Djangoなwebアプリをherokuにデプロイしたいかと思います。  
その際、自由にアクセスされては困るので、APIキーを使って認証できるようにもしたいと思ってます。

## pip+venvからpipenvに乗り換える

まずは [前回](https://qiita.com/rarewin/items/c6a70689844eafe8c3a1) の環境から進めていきます。  
前回の環境では pip と venv を使ったのですが、どうやら heroku では、それらをまとめたような便利ツール pipenv がサポートされてるようです。  
ということで、さっそくそこの乗り換え作業から行きます。

で、私の環境は Debian/sid なんですが、どうやらDebianのPythonまわりには `pip freeze` で不要な行を出す問題があるようです。 [参考サイト][DebianPkgResources]  
これのせいでpipenvのインストール時に問題が発生したので、まずはrequirements.txtから以下の行を削除しておきます。

```
pkg-resources==0.0.0
```

つづけて、以下のような感じでコマンドを叩きます。なお、pipenvは予め `pip install pipenv` 等でインストール済みとします。

```
% pipenv --python 3.6
Creating a virtualenv for this project…
Using /usr/bin/python3.6m to create virtualenv…
⠋Running virtualenv with interpreter /usr/bin/python3.6m
Using base prefix '/usr'
New python executable in /home/rare/.local/share/virtualenvs/todoapp.server-QYHaGtXw/bin/python3.6m
Also creating executable in /home/rare/.local/share/virtualenvs/todoapp.server-QYHaGtXw/bin/python
Installing setuptools, pip, wheel...done.

Virtualenv location: /home/rare/.local/share/virtualenvs/todoapp.server-QYHaGtXw
Requirements.txt found, instead of Pipfile! Converting…
Warning: Your Pipfile now contains pinned versions, if your requirements.txt did.
We recommend updating your Pipfile to specify the "*" version, instead.
```

つづけて、勧められている通りupdateをかけときます。

```
% pipenv install
Pipfile.lock not found, creating…
Locking [dev-packages] dependencies…
Locking [packages] dependencies…
Updated Pipfile.lock (a69925)!
Installing dependencies from Pipfile.lock (a69925)…
  🐍   ▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉ 13/13 — 00:00:06
To activate this project's virtualenv, run the following:
 $ pipenv shell
```

このあと、メッセージにある通り `pipenv shell` とすると、venvで `source .venv/bin/activate` をやったような状態になります。  
デフォルトだと、環境の作成にpipのユーザのディレクトリが使われる点と、別途シェルが立ち上がってくるあたりが違いでしょうか。  
インストールされたディレクトリは `pipenv --venv` とすると表示されるようです。

さて、pipからpipenvに乗り換えたので、前回用意したシェルスクリプトは以下の変更を入れました。

``` diff
diff --git a/todoapp.server/deploy.sh b/todoapp.server/deploy.sh
index 42a3e0f..828098a 100644
--- a/todoapp.server/deploy.sh
+++ b/todoapp.server/deploy.sh
@@ -7,7 +7,7 @@ python format_index_html.py
 echo 'Done...'
 
 echo 'Install python modules'
-pip install -r requirements.txt
+pipenv install
 echo 'Done...'
 
 echo 'Collect static'
```

あと、これまでどうもありがとうrequirements.txtさん。

```
% git rm -f requirements.txt
```

## RESTアクセス用のAPIキーを作成する

さて、誰からでもRESTでアクセスされては困るので、APIトークンでのアクセス制御をしたいと思います。  
といっても、既にある資産を使わせていただくだけですが。  
今回は、チュートリアルの環境に組み込まれている Django REST frameworkにある機能をそのまま使わせてもらいます。  
マニュアルは [こちら][TokenAuthentication] に。

まずは、settings.pyの INSTALLED_APPS に以下の行を追加します。 (snip snipはチョキチョキの意味)

```settings.py
INSTALLED_APPS = [
... snip snip ...
    'rest_framework.authtoken',
]
```

その後、おもむろに `./manage.py migrate` を実行しましょう。

```
% ./manage.py migrate
Operations to perform:
  Apply all migrations: admin, app, auth, authtoken, contenttypes, sessions
Running migrations:
  Applying authtoken.0001_initial... OK
  Applying authtoken.0002_auto_20160226_1747... OK
```

では、次にトークンを発行したいと思います。  
方法は色々あるみたいなんですが、最近のバージョンでは `manage.py` にコマンド `drf_create_token` が実装されているようなので、そちらを使うのが簡単なようです。  
おっと、実行する前にちゃんとユーザを作っておきましょう(一敗)。

```
% ./manage.py createsuperuser
Username (leave blank to use 'xxxx'):
Email address: xxxx@hogehoge.org
Password:
Password (again):
Superuser created successfully.
% ./manage.py drf_create_token <username>
Generated token (ここにトークンの文字列が表示される) for user (引数で入れたusernameが表示)
```

さて、これだけだとトークンを作っただけでアクセスし放題になってしまうので、以下のようにsettings.pyに追記してトークンでのアクセス制御を有効にします。

```settings.py
REST_FRAMEWORK = {
    'DEFAULT_PERMISSION_CLASSES': [
        'rest_framework.permissions.IsAdminUser',
    ],
    'DEFAULT_AUTHENTICATION_CLASSES': [
        'rest_framework.authentication.TokenAuthentication',
    ],
    'PAGE_SIZE': 10
}
```

有効になると、以下のようになります。

``` shellsession
% curl -X GET -H 'Authorization: Token (トークン文字列)' 'http://localhost:8000/todos/?format=json'
[{"text":"hoge","deadline":null,"priority":null,"done":false,"memo":"afdahoge"}, ....

}
% curl -X GET -H 'Authorization: Token hoge' 'http://localhost:8000/todos/?format=json'
{"detail":"Invalid token."}%
```


## herokuデプロイ用のDjangoの設定をする



## 参考にさせていただいたもの

* [TokenAuthentication][]
* [Django Rest Framework Token Authentication @ Stack Overflow][]

[DeploySubDirectoryToHeroku]:https://stackoverflow.com/questions/7539382/how-can-i-deploy-push-only-a-subdirectory-of-my-git-repo-to-heroku
[DebianPkgResources]:https://github.com/pypa/pip/issues/4668
[TokenAuthentication]:http://www.django-rest-framework.org/api-guide/authentication/#tokenauthentication
[Django Rest Framework Token Authentication @ Stack Overflow]:https://stackoverflow.com/questions/14838128/django-rest-framework-token-authentication
