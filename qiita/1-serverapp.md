# Vue.js + Django REST Framework + Android でTODOアプリを書いてみるテスト Part 1/?

## はじめに

**注意** : ここは作業メモ、大した情報は無いですょ。あと、Androidは今回は出てきません。

Androidのアプリを書いてみようかと思うのですが、それなりのことをやろうとするとどうしてもサーバー側もなんとかしないと便利なものが作れない気がしてます。
ということで、この記事では(私が飽きたり挫折したりしなければ)、簡単なTODOアプリを作ることを目標として、

- サーバー側を [Django REST Framework][DjangoRestFramework] で適当なものを書く
- せっかくなので Vue.js でwebからのインターフェースもつくってみる
- サーバーからRESTで持ってきてAndroid AppからもRecycler Viewをつかって表示したり、TODOの新規作成・変更できるようにする

ということをやっていきたいと思ってます。
今後この記事の続きが作成されるかは謎ですが(ぉ)、一応手元ではRecycler Viewで一覧表示するところくらいまでは確認済みであったりはします。

なお、この記事で作業しているリポジトリをGithubで公開しています。 > [rarewin/todoapp-study](https://github.com/rarewin/todoapp-study/tree/master/todoapp.server)
間違いの指摘や、もっと良い実装方法等について、紳士的な助言お待ちしております。

今回のパートでは、 [VueとDjangoの組み合わせに関するチュートリアル][VueonDjango] をベースに、
[Bootstrap Vue][BootstrapVue] をつかってTODO登録と表示をそこそこの見た目↓で作成するところまでいきます。

![ss_part1_achievement.png](https://qiita-image-store.s3.amazonaws.com/0/122416/f7bb1e51-a8d0-a976-aeee-b8face54767f.png)


## 準備

まずはVue.js + [Django][Django] な環境をつくります。ここは、はじめにでも触れた通り、先人の知恵をお借りします。
今回お世話になったのは [こちらのサイト][VueonDjango] (英語です)で、DjangoとVue.jsを組み合わせる場合のチュートリアルの記事を書いてくれてます。
では、まずはvue-cliは別途入れといてもらい(私の環境ではインストールディレクトリだけホームにしておき`npm install -g vue-cli`しました)、
以下のように vue-cli でDjango + Vue なサーバ側のプロジェクトを初期化します。

```shell
% vue init NdagiStanley/vue-django todoapp.server

? Project name todoapp.server
? Project version 0.1.0
? Project description ToDoApp Server App
? Author Katsuki Kobayashi <rare@tirasweel.org>
? private Yes

   vue-cli · Generated "todoapp.server".
```

その後は、ひとまず参考サイトの解説のままに打ち込んでいきました。
ただ、私の環境だと参考サイト通りだと上手く動かない箇所が一つだけあったため、以下のようにkeyupのところをkeydownに変更してます。
問題ない気がすんですが、なんででしょう。

```diff:UserInput.patch
*** src/components/UserInput.vue.old  2018-01-09 22:05:13.210095371 +0900
--- src/components/UserInput.vue      2017-12-20 01:42:21.981627577 +0900
***************
*** 1,6 ****
  <template>
    <div id="user-inputs">
!     <input v-model="newTodoText" v-on:keyup.enter="createTodo">
      <button v-on:click="clearTodos">
        Clear
      </button>
--- 1,6 ----
  <template>
    <div id="user-inputs">
!     <input v-model="newTodoText" v-on:keydown.enter="createTodo">
      <button v-on:click="clearTodos">
        Clear
      </button>
```

……と書いていたたのですが、なんとこの記事を書いている間に、チュートリアルの中で使用しているvue-cli用テンプレート(チュートリアルの作者が作成してます)がVue 2.xに置き換わるというアクシデント(?)が!!
正確には、この数日の間に数ヶ月前のプルリクがマージされたようです。なんてこった。

そのため、紹介してるチュートリアルのコードは若干動きがおかしくなってしまったような感じです。
ひとまず、私が [チュートリアル][VueonDjango] 完了相当まで持っていってみたプロジェクトを [GitHubにタグ付けて](https://github.com/rarewin/todoapp-study/tree/cp-finish-tutorial-vue2) 置いておきました。
この環境にて、以下のようにコマンドを叩いてもらえれば、ひとまず http://localhost:8000/ にアクセスしてもらえると、TODOの登録画面が出ると思います。

```shell
% cd todoapp-study/todoapp.server
% npm install
% python3 -m venv .venv
% source ./.venv/bin/activate
% pip install -r requirements.txt
% python manage.py migrate
% sh deploy.sh
```

なお、OSはみんな大好きDebian GNU/Linuxのsidさんです。
Windowsでなければ、大体同じような感じでここまで来られるのではないでしょうか。

他にも

- http://localhost:8000/todos/
- http://localhost:8000/todos/?format=json

とかにアクセスすると、DjangoのREST Frameworkの管理画面が出てくるので、これはこれで楽しめると思います。
本記事では、こちらの環境をベースにすすめていきます。
ここまで内容については、ひとまず(英語ですけど) [チュートリアル][VueonDjango] を読んでいただければと。
(私も、vuexとかvue-resourceは、ちゃんと理解して使ってないので……。今後の課題。)

## TODOにフィールドを追加してみる

ここからが本記事の独自部分になりまする。
まずは、現状はTODOとしてテキストしかないので、他にもフィールドを持たせます。
今回は、単純に以下のような属性を追加してみます。

- 期日
- 優先度
- 完了
- メモ

そのため、Djangoのモデルに以下のように追記してフィールドを追加します。

```diff:models.patch
diff --git todoapp.server/app/models.py todoapp.server/app/models.py
index 32c11b3..eab115d 100644
--- todoapp.server/app/models.py
+++ todoapp.server/app/models.py
@@ -6,6 +6,10 @@ from django.db import models
 class Todo(models.Model):

     text = models.CharField(max_length=200)
+    deadline = models.DateField(null=True, blank=True)
+    priority = models.IntegerField(null=True, blank=True)
+    done = models.BooleanField(default=False)
+    memo = models.TextField(null=True, blank=True)

     def __str__(self):
         return 'Todo: ' + self.text
diff --git todoapp.server/app/serializers.py todoapp.server/app/serializers.py
index 84fa8b0..ca6baa7 100644
--- todoapp.server/app/serializers.py
+++ todoapp.server/app/serializers.py
@@ -6,4 +6,4 @@ class TodoSerializer(serializers.ModelSerializer):

     class Meta:
         model = Todo
-        fields = ('text', )
+        fields = ('text', 'deadline', 'priority', 'done', 'memo')
```

モデルをいじったので、上記の変更を加えたら以下のようにしてmigrationします。

```shell
% python manage.py makemigration
% python manage.py migrate
```

これにて、モデルにいくつか属性を追加して、DBもマイグレーションができました。
続けて、ブラウザからの操作画面も対応してあげることにします(まぁ、この状態でのDjangoのREST FrameworkからTODOの追加・編集はできるのですが)。

ところで、メモやら優先度はいいんですが、日付の入力があるのでdatepickerがあるとうれしいです。
今回は、"vue datepicker" でぐぐったら一発で出てきた [Vue用のDatepicker][VueDatepicker] を使ってみます。
また、このdatepickerで作られたDate型の値をDjangoに渡す際に文字列を整形するため [moment-strftime][MomentStrftime] も入れます。

まずはnpmでそれらをインストールします。

```shell
% npm install vuejs-datepicker moment-strftime --save
```

この後、UserInput.vueを以下のように編集しました。
見た目が汚いとか、色々とコンポーネント化しておけよというツッコミがありそうですが、追い追い修正していくということでひとつ……。
Djangoに日付を渡す際に、moment-strftimeで `%Y-%m-%d` 形式に変換してるあたりがミソでしょうか。
ただ、今回期日は未設定を許可しているので、nullチェックする必要がありました(一敗)。

```js:src/components/UserInput.vue
<template>
  <div id="user-inputs">
    <input v-model="newTodoText"
           v-on:keydown.enter="createTodo">
    <datepicker v-model="newTodoDeadline"
                :clear-button="true">
    </datepicker>
    <div>
      <input type="radio" id="1" value="1" v-model="newTodoPriority">
      <label for="1">1</label>
      <input type="radio" id="2" value="2" v-model="newTodoPriority">
      <label for="2">2</label>
      <input type="radio" id="3" value="3" v-model="newTodoPriority">
      <label for="3">3</label>
      <input type="radio" id="4" value="4" v-model="newTodoPriority">
      <label for="4">4</label>
      <input type="radio" id="5" value="5" v-model="newTodoPriority">
      <label for="5">5</label>
    </div>
    <textarea v-model="newTodoMemo"></textarea>
  </div>
</template>

<script>
 import Datepicker from 'vuejs-datepicker'
 import moment from 'moment-strftime'

 export default {
   name: 'user-inputs',
   data: function () {
     return {
       newTodoText: '',
       newTodoDeadline: null,
       newTodoPriority: null,
       newTodoMemo: ''
     }
   },
   methods: {
     createTodo () {
       console.log(this.newTodoText, 'created!')
       this.$store.dispatch('addTodo', {
         text: this.newTodoText,
         deadline: (this.newTodoDeadline == null) ? null : moment(this.newTodoDeadline).strftime('%Y-%m-%d'),
         priority: this.newTodoPriority,
         done: false,
         memo: this.newTodoMemo
       })
       this.newTodoText = ''
       this.newTodoDeadline = null
       this.newTodoPriority = null
       this.newTodoMemo = ''
     },
     clearTodos () {
       this.$store.dispatch('clearTodos')
       console.log('Todos cleared!')
     }
   },
   components: {
     Datepicker
   }
 }
</script>
```

## 見た目をbootstrapで少しマシにしてみる

この段階ではまだ見た目がぐちゃぐちゃなので、bootstrapをつかうことにしました。。
これも、ぐぐってみると [Bootstrap-Vue][BootstrapVue] とかいうそのまんまのがあるので、こちらを利用してみます。
利用するには、style-loaderとかcss-loaderも必要そうなことがマニュアルに記述されていたので一緒に入れてます。
あんまりまじめに導入方法読んでいないので、不要な事もやってるかも。

```shell
% npm install --save bootstrap-vue bootstrap
% npm install --save style-loader css-loader
```

で、試行錯誤を重ねた結果、以下のようにUserInput.vueを変更しました。
本職が組み込み屋なので、bootstrapは恐る恐る適当に(?)いじってます。なにか変な事してたら教えてほしいです。
なお、ここまで `v-on:keydown.enter` としていた部分が動かなくなったので `@keydown.enter` に変更しています。

```js:src/components/UserInput.vue
<template>
  <div id="user-inputs">
    <b-form inline @submit="createTodo">
      <b-row id="rowTodo">
        <b-col sm="4">
          <label class="sr-only" for="inputTodo">TODO</label>
          <b-input id="inputTodo"
                   v-model="newTodoText"
                   type="text"
                   @keydown.enter="createTodo"
                   required
                   placeholder="TODO Description">
          </b-input>
        </b-col>
        <b-col sm="2">
          <label class="sr-only" for="datepickerDeadline">Deadline</label>
          <datepicker v-model="newTodoDeadline"
                      id="datepickerDeadline"
                      placeholder="Deadline"
                      :clear-button="true">
          </datepicker>
        </b-col>
        <b-col sm="1">
          <label class="sr-only" for="selectPriority">Priority</label>
          <b-form-select id="selectPriority"
                         v-model="newTodoPriority"
                         :options="priorities">
            <option slot="first" :value="null">Priority</option>
          </b-form-select>
        </b-col>
        <b-col sm="4">
          <label class="sr-only" for="textTodoMemo">Memo</label>
          <b-form-textarea id="textTodoMemo"
                           style="width: 100%"
                           v-model="newTodoMemo"
                           placeholder="Memo for TODO"
                           :no-resize="true" :max-rows="1">
          </b-form-textarea>
        </b-col>
        <b-col sm="1">
          <b-button type="submit" variant="primary">Create</b-button>
        </b-col>
      </b-row>
    </b-form>
  </div>
</template>

<script>
 import Datepicker from 'vuejs-datepicker'
 import moment from 'moment-strftime'

 export default {
   name: 'user-inputs',
   data: function () {
     return {
       newTodoText: '',
       newTodoDeadline: null,
       newTodoPriority: null,
       newTodoMemo: '',
       priorities: {'1': '1 (High)', '2': '2', '3': '3', '4': '4', '5': '5 (Low)'}
     }
   },
   methods: {
     createTodo () {
       console.log(this.newTodoText, 'created!')
       this.$store.dispatch('addTodo', {
         text: this.newTodoText,
         deadline: (this.newTodoDeadline == null) ? null : moment(this.newTodoDeadline).strftime('%Y-%m-%d'),
         priority: this.newTodoPriority,
         done: false,
         memo: this.newTodoMemo
       })
       this.newTodoText = ''
       this.newTodoDeadline = null
       this.newTodoPriority = null
       this.newTodoMemo = ''
     },
     clearTodos () {
       this.$store.dispatch('clearTodos')
       console.log('Todos cleared!')
     }
   },
   components: {
     Datepicker
   }
 }
</script>

<style>
 div#rowTodo {
   background: black;
 }

 .vdp-datepicker input,
 input#inputTodo,
 select#selectPriority,
 input#textTodoMemo {
   width: 100%;
   height: 100%;
   margin: 0 0 0 0;
 }
</style>
```

そして、TodoList.vueの方も変更します。
今回b-tableを使って表示するようにしました。
ソートとかもやってくれるし便利。
また、ダブルクリックのイベントをみつけて、ひとまずログを出すようにはしてます。
今後への布石。

```js:src/components/TodoList.vue
<template>
  <div id="todo-list">
    <b-table striped hover
             @row-dblclicked="dblclicked"
             :items="todos"
             :fields="fields">
    </b-table>
  </div>
</template>

<script>
 export default {
   name: 'todo-list',
   computed: {
     todos () {
       return this.$store.state.todos
     }
   },
   methods: {
     dblclicked (item, index, event) {
       console.log('double clicked:', item.text)
     }
   },
   data () {
     return {
       fields: [
         {key: 'text', sortable: true},
         {key: 'deadline', sortable: true},
         {key: 'priority', sortable: true},
         {key: 'done', sortable: true},
         {key: 'memo', sortable: false}
       ]
     }
   }
 }
</script>
```

これで、再度 `sh deploy.sh` をして http://localhost:8000 にアクセスすると、冒頭にもある以下のような画面になりました。

![ss_part1_achievement.png](https://qiita-image-store.s3.amazonaws.com/0/122416/f7bb1e51-a8d0-a976-aeee-b8face54767f.png)


## ひとまずここまでのまとめ

ということで、今回の記事はここまで。
ひとまず

- TODO管理用のDBを作成 (Django)
- TODOをRESTで管理できるようにする (Django REST Framework)
- TODO一覧をブラウザで表示/新規追加できるようにする (Vue.js)

といったところまでいきました。

今後の目標として

- 個別のTODOの編集をできるようにする
  - Djangoをいじって個別編集/削除できるようにする (チュートリアルでは一括削除しかない)
  - Vue.jsをいじってTODO一覧のテーブルをつついて編集できるようにする
- Android側のアプリの実装も行なう
  - まずはRecyclerViewをつかって表示する
  - で個別に編集したり新規追加できるようにする

といった事を考えてます。


## 参考にさせていただいたもの

- [Vue on Django (Vue + Djangoな環境のチュートリアル)][VueonDjango]
- [Vue Datepicker (Vue用のDatepicker)][VueDatepicker]
- [Momentのstrftimeモジュール][MomentStrftime]
- [Django (Python用のウェブアプリフレームワーク)][Django]
- [Django REST Framework (Djangoで良い感じにRESTする)][DjangoRestframework]
- [Bootstrap Vue][BootstrapVue]

[VueonDjango]:https://assertnotmagic.com/2017/06/20/vue-on-django-part-1/
[VueDatepicker]:https://github.com/charliekassel/vuejs-datepicker
[MomentStrftime]:https://github.com/benjaminoakes/moment-strftime
[Django]:https://www.djangoproject.com/
[DjangoRestFramework]:http://www.django-rest-framework.org/
[BootstrapVue]:https://bootstrap-vue.js.org/
