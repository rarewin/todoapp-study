# SwipeRefresh (Vue.js + Django REST Framework + Android でTODOアプリを書いてみるテスト Part 4/?)

## はじめに

**注意** : ここは作業メモ、大した情報は無いですょ。あと、 **今回はAndroidしか出てきません** 。

前回までにもげもげやってきたのですが、今回はRecyclerViewの表示を下に引っ張って(スワイプして)更新できるように検討してみました。
あと、今後はなるべく小出しに記事にしていこうかと思います。

この記事で作業しているリポジトリをGithubで公開しています。 > [rarewin/todoapp-study](https://github.com/rarewin/todoapp-study/tree/master/todoapp.server)
間違いの指摘や、もっと良い実装方法等について、紳士的な助言お待ちしております。

### 過去の記事

- [Vue.js + Django REST Framework + Android でTODOアプリを書いてみるテスト Part 1/?](https://qiita.com/rarewin/items/c6a70689844eafe8c3a1)
- [Vue.js + Django REST Framework + Android でTODOアプリを書いてみるテスト Part 2/?](https://qiita.com/rarewin/items/aece9d05aab3964c0300)
- [Vue.js + Django REST Framework + Android でTODOアプリを書いてみるテスト Part 3/?](https://qiita.com/rarewin/items/e4103f70963164cc2716)

## 準備

まずは、[Adding Swipe-to-Refresh To Your App][]を見てみると、SwipeRefreshLayoutを使うとよさそうです。
この記事によると、このレイアウトは1つのListViewかGridViewをサポートって書いてあるのでRecyclerViewはどうなんだって感じでしたが、普通に使えるようでした。onlyはsingleにかかってるんすかね。
実際のところは、[SwipeRefreshLayoutBasic][]にあるサンプルコードを見るのがわかりやすそうでした。

## 実際の作業

### SwipeRefreshLayoutの追加

まずは、レイアトのファイルの中で、RecylerViewをSwipeRefreshLayoutで囲みます。(前回からひっそりとID変更してます。)

```xml:app/src/main/res/layout/fragment_main_list.xml
<?xml version="1.0" encoding="utf-8"?>

<android.support.v4.widget.SwipeRefreshLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/swipe_refresh"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <android.support.v7.widget.RecyclerView
        xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:app="http://schemas.android.com/apk/res-auto"
        xmlns:tools="http://schemas.android.com/tools"
        android:id="@+id/todo_list"
        android:name="org.tirasweel.todoapp.MainActivityFragment"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_marginLeft="16dp"
        android:layout_marginRight="16dp"
        app:layoutManager="LinearLayoutManager"
        tools:context=".MainActivityFragment"
        tools:listitem="@layout/fragment_main" />

</android.support.v4.widget.SwipeRefreshLayout>
```

これだけだとフラグメントの `onCreateView()` で渡されるのがSwipeRefreshLayoutになったためか、RecyclerViewに何も表示されなくなってしまいました。
前回はフラグメントの `onCreateView()` でRecylerViewの設定を行ないましたが、そのタイミングでは早すぎるようで色々エラーが出たので、今回はサンプルコードにあるように `onViewCreated()` の方に移動させました。
以下のような感じです(どれがどう正しいのかわかってないので無駄なことしてるかもです。特にtodo_listをsafe call operator `.?` を使わないと例外で止まりました。)。

```kotlin:app/src/main/java/org/tirasweel/todoapp/MainActivityFragment.kt
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_main_list, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        val host = arguments!!.getString(ARG_host)
        val apitoken = arguments!!.getString(ARG_apitoken)

        // if json_host is invalid
        if (!URLUtil.isValidUrl(host)) {
            makeToast(MyApplication.mAppContext, getString(R.string.msg_invalid_url))
            return
        }

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

        // unless empty list registered first,
        // the error "E/RecyclerView﹕ No adapter attached; skipping layout" will be generated
        todo_list?.apply {
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
                todo_list?.adapter = TodoRecyclerViewAdapter(
                        todoResponse!!, mListener
                )

            }

            override fun onFailure(call: Call<ArrayList<TodoModel>>?, t: Throwable?) {
                makeToast(context, getString(R.string.msg_fail_get_todos))
            }
        })

    }
```

これで、以下のようにクルクルといつものやつが出てくるようになりました。
が、止める処理をしないと無限に回るようです。

![forever_spinning.gif](https://qiita-image-store.s3.amazonaws.com/0/122416/a96cbdc7-6717-aab6-6331-a3873f5c1109.gif)

### リスナーを追加する

スワイプでのリフレッシュが実行された場合、 `setOnRefreshListener()` で登録された[SwipeRefreshLayout.OnRefreshListener][]の`onRefresh()` がコールされるようです。
[SwipeRefreshLayout.OnRefreshListener][]は`onRefresh()`しかないので、Kotlinだとかなりあっさり書けますね。
上記でさわった `onViewCreated()` を再度いじって、以下のように変更しました。

くるくるを止めるには、 `setRefreshing()` で `false` を設定してあげれば止まるようですが、Kotlinの場合は `refreshing` でなくて `isRefreshing` につっこむようです。
元のJavaが `getRefreshing()` じゃなくて `isRefreshing()` だからみたいですね。
今回の場合は、Retrofitのクライアントのコールバック時で止めるようにしてみました。

```kotlin:app/src/main/java/org/tirasweel/todoapp/MainActivityFragment.kt
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_main_list, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        updateTodoList()

        swipe_refresh?.setOnRefreshListener {
            updateTodoList()
        }

    }

	/* 中略 */

    private fun updateTodoList() {

        val host = arguments!!.getString(ARG_host)
        val apitoken = arguments!!.getString(ARG_apitoken)

        // if json_host is invalid
        if (!URLUtil.isValidUrl(host)) {
            makeToast(MyApplication.mAppContext, getString(R.string.msg_invalid_url))
            return
        }

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

        // unless empty list registered first,
        // the error "E/RecyclerView﹕ No adapter attached; skipping layout" will be generated
        todo_list?.apply {
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
                todo_list?.adapter = TodoRecyclerViewAdapter(
                        todoResponse!!, mListener
                )
                swipe_refresh?.isRefreshing = false
            }

            override fun onFailure(call: Call<ArrayList<TodoModel>>?, t: Throwable?) {
                makeToast(context, getString(R.string.msg_fail_get_todos))
                swipe_refresh?.isRefreshing = false
            }
        })

    }
```

## 所感

ということで、意外にもあっさりと実装できました。
ただ、やっぱりインスタンスの作成のタイミングやらが理解しきれていないので、nullのチェックで逃げている箇所とかが多いですかね。
しっかりと理解していかねばならんところです。

ひとまず、今回の作業内容は [タグ付けてGitHubにアップしてます](https://github.com/rarewin/todoapp-study/releases/tag/cp-qiita-part4) 。
次は、TODO項目の編集あたりに手をつけたいと思ってます。

## 参考にさせていただいたもの

- [Adding Swipe-to-Refresh To Your App][]
- [SwipeRefreshLayoutBasic][]
- [SwipeRefreshLayout.OnRefreshListener][]

[Adding Swipe-to-Refresh To Your App]:https://developer.android.com/training/swipe/add-swipe-interface.html
[SwipeRefreshLayoutBasic]:https://developer.android.com/samples/SwipeRefreshLayoutBasic/index.html
[SwipeRefreshLayout.OnRefreshListener]:https://developer.android.com/reference/android/support/v4/widget/SwipeRefreshLayout.OnRefreshListener.html
