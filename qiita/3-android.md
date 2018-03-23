# Vue.js + Django REST Framework + Android でTODOアプリを書いてみるテスト Part 3/?

## はじめに

**注意** : ここは作業メモ、大した情報は無いですょ。あと、 **今回はAndroidしか出てきません** 。

Androidのアプリを書いてみようかと思うのですが、それなりのことをやろうとするとどうしてもサーバー側もなんとかしないと便利なものが作れない気がしてます。
ということで、この記事では(私が飽きたり挫折したりしなければ)、簡単なTODOアプリを作ることを目標として、

- サーバー側を [Django REST Framework][DjangoRestFramework] で適当なものを書く
- せっかくなので Vue.js でwebからのインターフェースもつくってみる
- サーバーからRESTで持ってきてAndroid AppからもRecycler Viewをつかって表示したり、TODOの新規作成・変更できるようにする

ということをやっていきたいと思ってます。

なお、この記事で作業しているリポジトリをGithubで公開しています。 > [rarewin/todoapp-study](https://github.com/rarewin/todoapp-study/tree/master/todoapp.server)
間違いの指摘や、もっと良い実装方法等について、紳士的な助言お待ちしております。

今回のパートでは、ようやくAndroidアプリに手を出します。
主に見ていく点は次の三点です。

- [前回](https://qiita.com/rarewin/items/aece9d05aab3964c0300) および [第1回](https://qiita.com/rarewin/items/c6a70689844eafe8c3a1) で作成したTODOのアプリを、API Tokenでの認証つきでherokuにデプロイしたので、そのサイトのURLとAPI Tokenの設定値をSharedPreferencesを使用して保存する
- Recycler Viewを使って登録されている一覧を表示できるようにする
- 新規でTODOを登録できるようにする

謝辞的な感じで、今回の記事については多分に [一番やさしいAndroidアプリ開発入門２][] を参考にさせてもらいました。
結構わかった気になれる内容なので、ちょくちょく9割引きくらいになるのでそのタイミング購入すると良いかもしれません。
あ、ワタクシ別に回し者というわけではございません。
~~あと、大阪在住のワタクシでも、講師のノリは若干ツラいので、サンプルで駄目そうなら止めた方が良いと思います(ぉ。~~

今回使用したコードは、 [GitHub](https://github.com/rarewin/todoapp-study/releases/tag/cp-qiita-part3) にアップしてますので、興味あればどうぞ。


### 過去の記事

- [Vue.js + Django REST Framework + Android でTODOアプリを書いてみるテスト Part 1/?](https://qiita.com/rarewin/items/c6a70689844eafe8c3a1)
- [Vue.js + Django REST Framework + Android でTODOアプリを書いてみるテスト Part 2/?](https://qiita.com/rarewin/items/aece9d05aab3964c0300)

## 準備

Androidのプロジェクトを準備します。今回は、AndroidStudio 3.2-canary2およびそれ以降のバージョンで作成してます。
今回は、設定的には以下のようにしてます。

- もちろんKotlin
- KitKat以降に対応
- MainActivityはBasic ActivityでFragmentは使用する

なお、新規でプロジェクトつくってるにもかかわらず、普通に以下のエラーが出ました。 __なんでよ。__

```
Error:Could not find org.jetbrains.kotlin:kotlin-gradle-plugin:1.2.20-eap-33.
Searched in the following locations:
    file:/opt/android-studio-3.1-canary9/gradle/m2repository/org/jetbrains/kotlin/kotlin-gradle-plugin/1.2.20-eap-33/kotlin-gradle-plugin-1.2.20-eap-33.pom
    file:/opt/android-studio-3.1-canary9/gradle/m2repository/org/jetbrains/kotlin/kotlin-gradle-plugin/1.2.20-eap-33/kotlin-gradle-plugin-1.2.20-eap-33.jar
    https://dl.google.com/dl/android/maven2/org/jetbrains/kotlin/kotlin-gradle-plugin/1.2.20-eap-33/kotlin-gradle-plugin-1.2.20-eap-33.pom
    https://dl.google.com/dl/android/maven2/org/jetbrains/kotlin/kotlin-gradle-plugin/1.2.20-eap-33/kotlin-gradle-plugin-1.2.20-eap-33.jar
    https://jcenter.bintray.com/org/jetbrains/kotlin/kotlin-gradle-plugin/1.2.20-eap-33/kotlin-gradle-plugin-1.2.20-eap-33.pom
    https://jcenter.bintray.com/org/jetbrains/kotlin/kotlin-gradle-plugin/1.2.20-eap-33/kotlin-gradle-plugin-1.2.20-eap-33.jar
Required by:
    project :
```

どうやら、以前からの設定ファイルが悪さをしていたようです。
`~/.AndroidStudioPreview3.1` ディレクトリを削除して、設定を遣り直したらビルドできるプロジェクトができました。

## ToDoアプリのサーバーの設定

### レイアウトを作る

URL等の設定画面は以下のような見た目にしてみました。
今回の要点からは外れる普通の内容なので、こちらにコードを貼ると長くなりすぎることもあり割愛します。
興味のある方はGitHubのコードの方を見てあげてください(まったく大したことはしてませんが)。

![setting.png](https://qiita-image-store.s3.amazonaws.com/0/122416/88396857-df93-b5c3-791a-82932cb0efb1.png)

### 設定値を保存する

で、画面はつくってはみたものの、設定値はどうするのかなぁと調べてみたところ、[キー値セットを保存する][] という記事を読むと、簡単な設定値なら SharedPreferences というものを使えば比較的簡単に保存できそうです。
ということで、以下のような設定値用のクラスを作成してみました(import文等は省略してます)。
なお、現状では色々と処理を省いてます。(URL先がアクセスできるかどうかのチェックとか)

```kotlin:app/src/main/java/org/tirasweel/todoapp/todo/TodoAppSetting.kt
class TodoAppSetting(context: Context) {

    // constants for TodoAppSetting
    companion object {
        private const val TODO_APP_SETTING = "SettingForTodoApp"
    }

    enum class SettingKey {
        SETTING_SERVER_URI,
        SETTING_API_TOKEN
    }

    private val sharedPref = context.getSharedPreferences(TODO_APP_SETTING, Context.MODE_PRIVATE)

    fun setServerUri(uri: String): Boolean {

        // fail on invalid url (empty string is allowed)
        if (!URLUtil.isValidUrl(uri) && uri.isNotEmpty())
            return false

        // store uri
        val editor = sharedPref.edit()
        editor.putString(SettingKey.SETTING_SERVER_URI.toString(), uri)

        return editor.commit()
    }

    fun getServerUri() = sharedPref.getString(SettingKey.SETTING_SERVER_URI.toString(), "")

    fun setApiToken(token: String): Boolean {

        // validate token if required

        // store API token
        val editor = sharedPref.edit()
        editor.putString(SettingKey.SETTING_API_TOKEN.toString(), token)

        return editor.commit()
    }

    fun getApiToken() = sharedPref.getString(SettingKey.SETTING_API_TOKEN.toString(), "")

}
```

このクラスを用いて無事に設定値を保存し、次回以降の起動時でも設定値を読み込むことができるようになりました。
もうちょっとすっきり書けそうな気もしますが、ひとまずやりたい事は最低限できたので、一旦こんな感じで。

## RecyclerView を使ってTODOの一覧を表示する

### レイアウトをいじる

で、ようやくここまで来ました。長かった……。

で、今回は上で「フラグメントを使用する」としてアクティビティをつくってしまったので、以下のように手動で色々と追加してます。
実際にRecyclerViewを使ったリストを作成する際には、EmptyActivityを作って右クリックのメニューから "New" → "Fragment" → "Fragment (List)" とした方がかしこいようです。
__上で紹介した [一番やさしいAndroidアプリ開発入門２][] でもそうやってます。__
~~なんでやらなかったって? 身についてなかったからですYO!! orz~~

まず、content_main.xmlを以下のような感じにしました。FrameLayoutにして、後々フラグメントを入れかえられるようにしてます。

```xml:app/src/main/res/layout/content_main.xml
<?xml version="1.0" encoding="utf-8"?>
<android.support.constraint.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:layout_behavior="@string/appbar_scrolling_view_behavior"
    tools:context=".MainActivity"
    tools:showIn="@layout/activity_main">

    <FrameLayout
        android:id="@+id/container_main"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:visibility="visible">

    </FrameLayout>

</android.support.constraint.ConstraintLayout>
```

このFrameLayoutに入れるフラグメントですが、まずはリスト表示するためのfragment_menu_list.xmlというファイルを新規で作成しました。
内容は以下のような感じ。ここでようやくRecyclerViewが登場してます。
`tools:listitem="@layout/fragment_main"`がミソな気がします。

```xml:app/src/main/res/layout/fragment_main_list.xml
<?xml version="1.0" encoding="utf-8"?>
<android.support.v7.widget.RecyclerView
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/list"
    android:name="org.tirasweel.todoapp.MainActivityFragment"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:layout_marginLeft="16dp"
    android:layout_marginRight="16dp"
    app:layoutManager="LinearLayoutManager"
    tools:context=".MainActivityFragment"
    tools:listitem="@layout/fragment_main" />
```

そして、その問題のfragment_main.xmlは以下のようにしました。
CardViewをつくって、その中にLinearLayoutを使いつつアイコンとしてImageViewやら、TODOの内容や期日表示のためのTextViewを用意しました。

```xml:app/src/main/res/layout/fragment_main.xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal">

    <android.support.v7.widget.CardView
        android:layout_width="match_parent"
        android:layout_height="wrap_content">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:orientation="horizontal">

            <ImageView
                android:id="@+id/image_todo_icon"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                app:srcCompat="@drawable/ic_media_stop_light" />

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:orientation="vertical">

                <TextView
                    android:id="@+id/text_todo_text"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content" />

                <TextView
                    android:id="@+id/text_todo_deadline"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content" />

            </LinearLayout>
        </LinearLayout>

    </android.support.v7.widget.CardView>
</LinearLayout>
```

このフラグメントを表示するため、以下のようなコードをMainActivityに仕込んでいます。

``` kotlin
        supportFragmentManager.beginTransaction()
                .add(R.id.container_main,
                        MainActivityFragment.newInstance(
                                mTodoAppSetting!!.getServerUri(),
                                mTodoAppSetting!!.getApiToken()),
                        "MAIN")
                .commit()
```

### Retrofit+OkHttp+gsonの組み合わせでAdapterをつくる

さて、問題はこの後のAdapterです。
が、その前にそもそもRESTでアクセスするかなんですが、なんのひねりもなく[Retrofit][]と[OkHttp][]と[gson][]をつかいます。

まずはgsonで使うためにTODOのモデルをつくります。
[前回](https://qiita.com/rarewin/items/aece9d05aab3964c0300) 作成したjsonが

```json
[
    {
        "text": "てすと",
        "deadline": null,
        "priority": null,
        "done": false,
        "memo": ""
    },
    {
        "text": "あいうえお",
        "deadline": null,
        "priority": null,
        "done": false,
        "memo": ""
    }
}
```

といった感じなので、モデルとしては以下のようになりました。
なお、importとかpackageは省略してます。data class使えるKotlinは、こういうのほんと楽。素敵。

あと、ActivityやFragment間で受け渡しできるようにSerializableを継承しております。

```kotlin:app/src/main/java/org/tirasweel/todoapp/todo/TodoModel.kt
data class TodoModel(val text: String,
                     val deadline: String?,
                     val priority: Int?,
                     val done: Boolean,
                     val memo: String?): Serializable
```

次にRetrofitのAPIは以下のようにinterfaceで定義。
ひとまず、今回は一覧で使うためのGETと、新規追加のPOSTを用意。
例によって例の如く、packageやimportは省略しております。
こちらは特にミソもないです。あるとしたら、GETの方は一覧になるのでArrayListになってる事くらいでしょうか。

```kotlin:app/src/main/java/org/tirasweel/todoapp/todo/TodoClient.kt
interface TodoClient {
    @GET("/todos/")
    fun getTodos(): Call<ArrayList<TodoModel>>

    @POST("/todos/")
    fun addTodo(@Body todo: TodoModel): Call<TodoModel>
}
```

次にAdapterです。
以下のようなコードになりました。
このあたりは、RecyclerViewのAdapterとしては何の変哲もないでしょうか。
ひとまず今回はTODOの内容(text_todo_text)、期日(text_todo_deadline)、適当なアイコン(image_todo_icon)にTodoModelの値を入れています。

```kotlin:app/src/main/java/org/tirasweel/todoapp/todo/TodoRecyclerViewAdapter.kt
class TodoRecyclerViewAdapter(
        private val mValues: List<TodoModel>,
        private val mListener: OnListFragmentInteractionListener?)
    : RecyclerView.Adapter<TodoRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_main, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.mTodoText.text = mValues[position].text
        holder.mTodoDeadline.text = mValues[position].deadline

        holder.mTodoIcon.setColorFilter(
                ContextCompat.getColor(
                        MyApplication.mAppContext,
                        when (mValues[position].priority) {
                            1 -> R.color.colorPri1
                            2 -> R.color.colorPri2
                            3 -> R.color.colorPri3
                            4 -> R.color.colorPri4
                            5 -> R.color.colorPri5
                            else -> R.color.colorPriNone
                        }))
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        var mTodoText = mView.text_todo_text
        var mTodoDeadline = mView.text_todo_deadline
        var mTodoIcon = mView.image_todo_icon
    }
}
```

さて、問題となるのは、今回ヘッダにTokenを含めないといけないことです。
結論から言うと、OkHttpClientをInterceptorを介してヘッダが付くように自分で作ってあげ、それをRetrofitに渡してあげればOKでした。
参考にさせていただいたサイト……は失念シテシマイマシタ……。

ひとまず、GETのコードはフラグメントの `onCreateView()` に、以下のように入れました。
ホスト名とか、APIのトークンについては、前述したTodoAppSettingクラスで取得した値をアクティビティからもらっています。

```kotlin:app/src/main/java/org/tirasweel/todoapp/MainActivityFragment.kt
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_main_list, container, false)

        if (!(view is RecyclerView)) {
            return view
        }

        val host = arguments!!.getString(ARG_host)
        val apitoken = arguments!!.getString(ARG_apitoken)

        val client = OkHttpClient.Builder()
                .addInterceptor(Interceptor { chain ->
                    val orig = chain.request()
                    val request = orig.newBuilder()
                            .header("Authorization", "Token " + apitoken)
                            .method(orig.method(), orig.body())
                            .build()
                    chain.proceed(request)
                }).build()

        val gson = GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create()

        val retrofit = Retrofit.Builder()
                .baseUrl(host)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build()

        val todoClient = retrofit.create(TodoClient::class.java)
        val call = todoClient!!.getTodos()

        view.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = TodoRecyclerViewAdapter(
                    emptyList(),
                    mListener
            )
        }

        call.enqueue(object : Callback<ArrayList<TodoModel>> {

            override fun onResponse(call: Call<ArrayList<TodoModel>>?,
                                    response: Response<ArrayList<TodoModel>>?) {
                val todoResponse = response!!.body()
                // Set the adapter
                view.adapter = TodoRecyclerViewAdapter(
                        todoResponse!!, mListener
                )
            }
            override fun onFailure(call: Call<ArrayList<TodoModel>>?, t: Throwable?) {
                makeToast(context, getString(R.string.msg_fail_get_todos))
            }
        })

        return view
    }
```

上記のコードで、以下のような表示ができました。
なお、まだ項目をつついても何も起こりません……。

![list.png](https://qiita-image-store.s3.amazonaws.com/0/122416/e9e9a28c-7363-549f-b2bc-2604bef8e8e3.png)


## 新規追加もできるようにする

一覧の取得ができるようになったので、今度はPOSTです。
入力画面だけ作ってしまえば簡単……です。
みなさまにおかれましては、決して、決して enqueue() をコールしわすれて頭を抱えないようにお気をつけください…………(1敗)。

```kotlin:app/src/main/java/org/tirasweel/todoapp/MainActivity.kt
                val client = OkHttpClient.Builder()
                        .addInterceptor(Interceptor { chain ->

                            val orig = chain.request()
                            val request = orig.newBuilder()
                                    .header("Authorization", "Token " + apitoken)
                                    .method(orig.method(), orig.body())
                                    .build()

                            chain.proceed(request)

                        }).build()

                val gson = GsonBuilder()
                        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                        .create()

                val retrofit = Retrofit.Builder()
                        .baseUrl(host)
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .client(client)
                        .build()

                val todoClient = retrofit.create(TodoClient::class.java)

                todoClient.addTodo(todo).enqueue(object: Callback<TodoModel> {
                    override fun onResponse(call: Call<TodoModel>?, response: Response<TodoModel>?) {
                        makeToast(MyApplication.mAppContext, getString(R.string.msg_new_todo_registerd))
                        updateList()
                    }
                    override fun onFailure(call: Call<TodoModel>?, t: Throwable?) {
                        makeToast(MyApplication.mAppContext, getString(R.string.msg_fail_new_todo_registered))
                    }
                })
```

長くなってきたのでレイアウトについては省略しますが、画面的には以下のような感じになっております。

![edit.png](https://qiita-image-store.s3.amazonaws.com/0/122416/5bc4a276-3291-4f7d-216c-65df03a71015.png)

## 今後の課題など

まず、今回はGET/POSTしかやっていないので、今後はPUTも使って編集できるようにしたいです。
元にしたDjangoのコードが、PUT作ってないだけでなくTODO項目にID付けてないので、Django側とAndroid側と両方に結構な変更が必要そう。
あと、Vueの画面も直さないといけませんか。よくばりすぎ……か。

次に、一覧の更新方法について検討したいと思ってます。
要は、引っぱって更新するやつです。
SwipeRefreshLayoutがミソらしいですが、今回はそこまで辿りつけませんでした。

そして、Qiita記事について、もうちょっと小出し小出しにできるようにしたいところです。
ここまでの記事についても、いささか欲張りすぎですな……。

あと、そもそも、コードをもっといい感じにしたいですね。
今回のコードも、Retrofit回りのコードがGET/POSTで「DRY規則なにそれ?」的になってるので、なおしたいっす。

## 参考にさせていただいたもの

- [キー値セットを保存する][]
- [URLUtil][]
- [リストとカードの作成][]
- [Retrofit][]
- [OkHttp][]
- [gson][]

[DjangoRestFramework]:http://www.django-rest-framework.org/
[一番やさしいAndroidアプリ開発入門２]:https://www.udemy.com/androidkotlin2/learn/v4/overview
[キー値セットを保存する]:https://developer.android.com/training/basics/data-storage/shared-preferences.html?hl=ja
[URLUtil]:https://developer.android.com/reference/android/webkit/URLUtil.html
[リストとカードの作成]:https://developer.android.com/training/material/lists-cards.html?hl=ja
[Retrofit]:http://square.github.io/retrofit/
[OkHttp]:http://square.github.io/okhttp/
[gson]:https://github.com/google/gson

