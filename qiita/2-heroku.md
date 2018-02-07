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

```shellsession
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

```shellsession
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

```diff
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

```shellsession
% git rm -f requirements.txt
```

## RESTアクセス用のAPIキーを作成する

さて、誰からでもRESTでアクセスされては困るので、APIトークンでのアクセス制御をしたいと思います。
といっても、既にある資産を使わせていただくだけですが。
今回は、チュートリアルの環境に組み込まれている Django REST frameworkにある機能をそのまま使わせてもらいます。
マニュアルは [こちら][TokenAuthentication] に。

まずは、settings.pyの INSTALLED_APPS に以下の行を追加します。 (snip snipはチョキチョキの意味)

```python:vuedj/settings.py
INSTALLED_APPS = [
... snip snip ...
    'rest_framework.authtoken',
]
```

その後、おもむろに `./manage.py migrate` を実行しましょう。

```shellsession
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

```shellsession
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

```python:vuedj/settings.py
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

```shellsession
% curl -X GET -H 'Authorization: Token (トークン文字列)' 'http://localhost:8000/todos/?format=json'
[{"text":"hoge","deadline":null,"priority":null,"done":false,"memo":"afdahoge"}, ....

}
% curl -X GET -H 'Authorization: Token hoge' 'http://localhost:8000/todos/?format=json'
{"detail":"Invalid token."}%
```

## Vueのコードでセッション認証を使うようにする

さて、アクセス制限するようにしたのはよいのですが、今度はVueのフロントエンドからDjango上のデータにアクセスできなくなってしまいました。
ここでVueのコードにトークンを埋めたら、結局アクセスし放題と変わりないので、Djangoのセッション管理機能をそのまま使います。

てっとり早く、差分は以下になります。
ちょっと正確なことは追ってないのですが、 `MIDDLEWARE_CLASSES` という定義は古いようで、
最近のDjangoだと `MIDDLEWARE` としないと色々と動かないようです?
あと、 `DEFAULT_AUTHENTICATION_CLASSES` に `rest_framework.authentication.SessionAuthentication` を追加してるのがミソかと思います。


```diff
diff --git a/todoapp.server/vuedj/settings.py b/todoapp.server/vuedj/settings.py
index 3c9973a..93d8363 100644
--- a/todoapp.server/vuedj/settings.py
+++ b/todoapp.server/vuedj/settings.py
@@ -45,13 +45,12 @@ INSTALLED_APPS = [
     'app',
 ]

-MIDDLEWARE_CLASSES = [
+MIDDLEWARE = [
     'django.middleware.security.SecurityMiddleware',
     'django.contrib.sessions.middleware.SessionMiddleware',
     'django.middleware.common.CommonMiddleware',
     'django.middleware.csrf.CsrfViewMiddleware',
     'django.contrib.auth.middleware.AuthenticationMiddleware',
-    'django.contrib.auth.middleware.SessionAuthenticationMiddleware',
     'django.contrib.messages.middleware.MessageMiddleware',
     'django.middleware.clickjacking.XFrameOptionsMiddleware',
 ]
@@ -148,5 +147,6 @@ REST_FRAMEWORK = {
     ],
     'DEFAULT_AUTHENTICATION_CLASSES': [
         'rest_framework.authentication.TokenAuthentication',
+        'rest_framework.authentication.SessionAuthentication',
     ],
 }
diff --git a/todoapp.server/vuedj/urls.py b/todoapp.server/vuedj/urls.py
index e1b2384..cc07fec 100644
--- a/todoapp.server/vuedj/urls.py
+++ b/todoapp.server/vuedj/urls.py
@@ -13,16 +13,21 @@ Including another URLconf
     1. Import the include() function: from django.conf.urls import url, include
     2. Add a URL to urlpatterns:  url(r'^blog/', include('blog.urls'))
 """
-from django.conf.urls import url, include
+from django.conf.urls import (
+    url,
+    include
+)
+from django.contrib import admin
+from django.urls import path

 from rest_framework import routers
-
 from app import views

 router = routers.SimpleRouter()
 router.register(r'todos', views.TodoViewSet)

 urlpatterns = [
+    path('admin/', admin.site.urls),
     url(r'^$', views.index, name='home'),
-    url(r'^', include(router.urls))
+    url(r'^', include(router.urls)),
 ]
```

なんにせよ、上記の変更で http://localhost:8000/admin/ にアクセスするとログイン画面が表示されるようになり、
トークン作る際に作成した管理ユーザでログインすると、無事にVueで書いたフロントエンドからデータをさわれるようになり…… __ませんでした__。

どうやら、一覧表示のGETはうまくいくのですが、POSTのときだけ失敗しています。
ブラウザのデバッガを見てると、以下のようなエラーが出ていました。

```
{…}
body: {…}
detail: "CSRF Failed: CSRF token missing or incorrect."
__proto__: Object { … }
bodyText: "{\"detail\":\"CSRF Failed: CSRF token missing or incorrect.\"}"
headers: {…}
map: {…}
```

[StackOverflowのこの記事][CSRF Failed: CSRF token missing or incorrect]を見てみると、セッション認証を使う場合はCSRFトークンを付けろとありました。
[StackOverflowからのリンクの張られているDjangoのページでも](https://docs.djangoproject.com/en/dev/ref/csrf/#ajax) AjaxでPOSTリクエストをする場合は、
CSRFトークンをクッキーから取得して渡せとありました。
JavaScript Cookie libraryを使えば楽勝だとも書いてあるので、今回はそちらに従います。
ということで、ひとまず npm コマンドでライブラリを入れます。

で、POSTのリクエストのヘッダに `X-CSRFToken` を付けて……とやっていたら、
なんと、[vue-resourceは引退しているらしい][vue-resource の引退について] ということがわかりました。
この世界は色々とはやい……。こわい……。
ということで、ついでに、代わりの [axios][axios] に乗り換えておきます。
そしてさようなら、vue-resourceくん……。

```shellsession
% npm install --save js-cookie axious
% npm uninstall --save vue-resource
```

ここまでやって、ようやくPOSTリクエストとDELETEリクエストのヘッダに `X-CSRFToken` をつけることにします。
`store.js` と `api.js` を以下のようにしました。

```javascript:src/store/store.js
import Vue from 'vue'
import Vuex from 'vuex'
import api from './api.js'

Vue.use(Vuex)

const apiRoot = 'http://localhost:8000'
const store = new Vuex.Store({
  state: {
    todos: []
  },
  mutations: {
    'GET_TODOS': function (state, response) {
      state.todos = response.data
    },
    'ADD_TODO': function (state, response) {
      state.todos.push(response.data)
    },
    'CLEAR_TODOS': function (state) {
      const todos = state.todos
      todos.splice(0, todos.length)
    },
    'API_FAIL': function (sate, error) {
      console.error(error)
    }
  },
  actions: {
    getTodos (store) {
      return api.get(apiRoot + '/todos/')
                .then((response) => store.commit('GET_TODOS', response))
                .catch((error) => store.commit('API_FAIL', error))
    },
    addTodo (store, todo) {
      return api.post(apiRoot + '/todos/', todo)
                .then((response) => store.commit('ADD_TODO', response))
                .catch((error) => store.commit('API_FAIL', error))
    },
    clearTodos (store) {
      return api.delete(apiRoot + '/todos/clear_todos/')
                .then((response) => store.commit('CLEAR_TODOS'))
                .catch((error) => store.commit('API_FAIL', error))
    }
  }
})

export default store
```

```javascript:src/store/api.js
import Axios from 'axios'
import Cookies from 'js-cookie'

const csrfToken = Cookies.get('csrftoken')
const requestHeader = {
  headers: {
    'X-CSRFToken': csrfToken
  }
}
const requestHeaderWithNullData = {
  headers: {
    'X-CSRFToken': csrfToken
  },
  data: {}
}

export default {
  get (url, request) {
    return Axios.get(url, request)
                .then((response) => Promise.resolve(response))
                .catch((error) => Promise.reject(error))
  },
  post (url, request) {
    return Axios.post(url, request, requestHeader)
                .then((response) => Promise.resolve(response))
                .catch((error) => Promise.reject(error))
  },
  patch (url, request) {
    return Axios.patch(url, request, requestHeader)
                .then((response) => Promise.resolve(response))
                .catch((error) => Promise.reject(error))
  },
  delete (url) {
    return Axios.delete(url, requestHeaderWithNullData)
                .then((response) => Promise.resolve(response))
                .catch((error) => Promise.reject(error))
  }
}
```

注意点として2点ありました。

- `vue-resource` では `body` というメンバだったのが、 `axios` では `data` になっている
- DELETEする場合、第二引数にあたえる辞書に `data` がないと `headers` が見向きもされない  [参考][Can't set headers for DELETE method]

とくに後者は罠でした……。

## herokuデプロイ用のDjangoの設定をする

というわけで、ようやく本題です。
Djangoのプロジェクトをherokuにデプロイするてっとり早い方法の手順としては、

- [django-heroku][] を使う
- Procfileをつくる

な感じです。
ということで、まずは [django-heroku][] を入れます。これはデータベースや、シークレットキーや、アクセス可能なホストやらの設定を
heroku的にいい感じに書き換えてくれるものです。

```shellsession
% pipenv install django-heroku
```

インストールできたら、以下の差分を投入します。これで設定の一つ目はほぼ完了。らくちん。

```diff
diff --git a/todoapp.server/vuedj/settings.py b/todoapp.server/vuedj/settings.py
index 93d8363..b16c5ea 100644
--- a/todoapp.server/vuedj/settings.py
+++ b/todoapp.server/vuedj/settings.py
@@ -11,6 +11,7 @@ https://docs.djangoproject.com/en/1.9/ref/settings/
 """

 import os
+import django_heroku

 # Build paths inside the project like this: os.path.join(BASE_DIR, ...)
 BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
@@ -150,3 +151,6 @@ REST_FRAMEWORK = {
         'rest_framework.authentication.SessionAuthentication',
     ],
 }
+
+# Configure Django App for Heroku.
+django_heroku.settings(locals())
```

つづいて、Procfileの方ですが、herokuでDjangoを動かすさいには [Gunicorn][] というWSGIのモジュールを使うのが一般的みたいです。
ということで、まずは入れましょう。

```shellsession
% pipenv install gunicorn
```

そして、heroku用のProcfileは以下のようになりました。

```Procfile
web: gunicorn vuedj.wsgi --log-file -
```

さて、ここまできて git にはAndroidアプリと共存したようなリポジトリを作成してしまった事に気付きました。
サブディレクトリだけherokuにデプロイとかできないのかなぁ、とか考えてたら、 [みんな大好きStack Overflow][How can I deploy/push only a subdirectory of my git repo to Heroku?] にて回答がありました。
git-subtreeをつかえばいけるようです。
また、今回の環境では PythonとNode.js の二つが必要になるので、
ということで、 [Herokuのマニュアル][Using Multiple Buildpacks for an App] に従って以下のようにNode.js→Pythonと設定できるようにしてあげました。

さらに、 `npm install` で `devDependencies` もインストールしてほしかったので、 `NPM_CONFIG_PRODUCTION` の値をfalseにしていたり、
さらにその後に `npm run dev` をしてほしかったので、以下の差分も追加してます。

```patch
diff --git a/todoapp.server/package.json b/todoapp.server/package.json
index 8d8bea2..931ddd0 100644
--- a/todoapp.server/package.json
+++ b/todoapp.server/package.json
@@ -12,7 +12,7 @@
     "test": "npm run unit && npm run e2e",
     "lint": "eslint --ext .js,.vue src test/unit/specs test/e2e/specs",
     "build": "node build/build.js",
-    "postinstall": "node build/build.js"
+    "heroku-postbuild": "node build/build.js"
   },
   "dependencies": {
     "axios": "^0.17.1",
```

ということで、ずらっと並べると以下のようなコマンドでデプロイしました。
なお、コマンド叩く際のカレントディレクトリが悪かったからなのか、うまいことremote登録してくれなかったので、herokuコマンドを余分に叩いてます。
(herokuコマンドの部分は一部伏せ字にしてます)。


```shellsession
% heroku login
Enter your Heroku credentials:
Email: xxxx@xxxxx.xxx
Password: **********
Two-factor code: ******
Logged in as rare@tirasweel.org
(todoapp.server-QYHaGtXw) rare@ambisch% heroku create
Creating app... done, ⬢ xxxxx-xxxxx-00000
https://xxxxx-xxxxx-00000.herokuapp.com/ | https://git.heroku.com/xxxxx-xxxxx-00000.git
% heroku buildpacks:set heroku/python
Buildpack set. Next release on xxxxx-xxxxx-00000 will use heroku/python.
Run git push heroku master to create a new release using this buildpack.
% heroku buildpacks:add --index 1 heroku/nodejs
Buildpack added. Next release on xxxxx-xxxxx-00000 will use:
  1. heroku/nodejs
  2. heroku/python
Run git push heroku master to create a new release using these buildpacks.
% heroku config:set NPM_CONFIG_PRODUCTION=false
Setting NPM_CONFIG_PRODUCTION and restarting ⬢ xxxxx-xxxxx-00000... done, v3
NPM_CONFIG_PRODUCTION: false
% git subtree push --prefix todoapp.server heroku master
git push using:  heroku master
Counting objects: 3, done.
Delta compression using up to 4 threads.
Compressing objects: 100% (2/2), done.
Writing objects: 100% (3/3), 314 bytes | 314.00 KiB/s, done.
Total 3 (delta 1), reused 0 (delta 0)

... snip ...

remote: -----> Launching...
remote:        Released v7
remote:        https://xxxxx-xxxxx-00000.herokuapp.com/ deployed to Heroku
remote:
remote: Verifying deploy... done.
To https://git.heroku.com/xxxxx-xxxxx-00000.git
   cc2cec0..2f6aa7f  2f6aa7f8e28661c7e3c1ee003785c18fc7f4304a -> master
% heroku run python manage.py migrate
Running python manage.py migrate on ⬢ xxxxx-xxxxx-00000... up, run.9898 (Free)
Operations to perform:
  Apply all migrations: admin, app, auth, authtoken, contenttypes, sessions
Running migrations:
  Applying contenttypes.0001_initial... OK
  Applying auth.0001_initial... OK
  Applying admin.0001_initial... OK
  Applying admin.0002_logentry_remove_auto_add... OK
  Applying app.0001_initial... OK
  Applying app.0002_auto_20180112_1451... OK
  Applying contenttypes.0002_remove_content_type_name... OK
  Applying auth.0002_alter_permission_name_max_length... OK
  Applying auth.0003_alter_user_email_max_length... OK
  Applying auth.0004_alter_user_username_opts... OK
  Applying auth.0005_alter_user_last_login_null... OK
  Applying auth.0006_require_contenttypes_0002... OK
  Applying auth.0007_alter_validators_add_error_messages... OK
  Applying auth.0008_alter_user_username_max_length... OK
  Applying auth.0009_alter_user_last_name_max_length... OK
  Applying authtoken.0001_initial... OK
  Applying authtoken.0002_auto_20160226_1747... OK
  Applying sessions.0001_initial... OK
% heroku run python manage.py createsuperuser
Running python manage.py createsuperuser on ⬢ xxxxx-xxxxx-00000... up, run.6738 (Free)
Username (leave blank to use 'u38481'): xxxx
Email address: xxxx@xxxxx.xxx
Password:
Password (again):
Superuser created successfully.
```


## 参考にさせていただいたもの

* [TokenAuthentication][]
* [Django Rest Framework Token Authentication @ Stack Overflow][]
* [Can't set headers for DELETE method][]
* [django-heroku][]
* [How can I deploy/push only a subdirectory of my git repo to Heroku?][]

[DjangoRestFramework]:http://www.django-rest-framework.org/
[DeploySubDirectoryToHeroku]:https://stackoverflow.com/questions/7539382/how-can-i-deploy-push-only-a-subdirectory-of-my-git-repo-to-heroku
[DebianPkgResources]:https://github.com/pypa/pip/issues/4668
[TokenAuthentication]:http://www.django-rest-framework.org/api-guide/authentication/#tokenauthentication
[Django Rest Framework Token Authentication @ Stack Overflow]:https://stackoverflow.com/questions/14838128/django-rest-framework-token-authentication
[CSRF Failed: CSRF token missing or incorrect]:https://stackoverflow.com/questions/26639169/csrf-failed-csrf-token-missing-or-incorrect
[vue-resource の引退について]:https://jp.vuejs.org/2016/11/03/retiring-vue-resource/
[axios]:https://github.com/axios/axios
[Can't set headers for DELETE method]:https://github.com/axios/axios/issues/509
[django-heroku]:https://github.com/heroku/django-heroku
[How can I deploy/push only a subdirectory of my git repo to Heroku?]:https://stackoverflow.com/questions/7539382/how-can-i-deploy-push-only-a-subdirectory-of-my-git-repo-to-heroku
[Gunicorn]:http://gunicorn.org/
[Using Multiple Buildpacks for an App]:https://devcenter.heroku.com/articles/using-multiple-buildpacks-for-an-app
