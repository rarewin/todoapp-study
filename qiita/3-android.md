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
.AndroidStudioPreview3.1ディレクトリを削除して、設定を遣り直したらビルドできるプロジェクトができました。

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
もうちょっとすっきり書けそうな気もしますが、一旦こんな感じで。

## RecyclerView を使ってTODOの一覧を表示する

### レイアウトをいじる

で、ようやくここまで来ました。長かった。
まず、content_main.xmlを以下のような感じにしました。

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

まず、一覧表示用のフラグメントにをさわりますが、もちろんHello Worldは消しておくとして、Constraint Layoutの下に問題のRecyclerViewを追加しました。
app:layout_constraintほげほげを設定しておくのを忘れずに……(1敗)。

- fragment_master_list.xml
  - RecyclerView
	- tools:listitem="@layout/fragment_master"

- fragment_master.xml
  - CardView


content_main.xmlの `fragment` を `Constraintlayout` に変換する。


```kotlin
package org.tirasweel.todoapp

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView


import org.tirasweel.todoapp.MainActivityFragment.OnListFragmentInteractionListener
import org.tirasweel.todoapp.dummy.DummyContent.DummyItem

import kotlinx.android.synthetic.main.fragment_main.view.*

/**
 * [RecyclerView.Adapter] that can display a [DummyItem] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 * TODO: Replace the implementation with code for your data type.
 */
class TodoRecyclerViewAdapter(
        private val mValues: List<DummyItem>,
        private val mListener: OnListFragmentInteractionListener?)
    : RecyclerView.Adapter<TodoRecyclerViewAdapter.ViewHolder>() {

    private val mOnClickListener: View.OnClickListener

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as DummyItem
            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
            mListener?.onListFragmentInteraction(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_main, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]
        holder.mIdView.text = item.id
        holder.mContentView.text = item.content

        with(holder.mView) {
            tag = item
            setOnClickListener(mOnClickListener)
        }
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mIdView: TextView = mView.item_number
        val mContentView: TextView = mView.content

        override fun toString(): String {
            return super.toString() + " '" + mContentView.text + "'"
        }
    }
}
```


## 参考にさせていただいたもの

- [キー値セットを保存する][]
- [URLUtil][]

- [ListsAndCards][]

[キー値セットを保存する]:https://developer.android.com/training/basics/data-storage/shared-preferences.html?hl=ja
[URLUtil]:https://developer.android.com/reference/android/webkit/URLUtil.html
[ListsAndCards]:https://developer.android.com/training/material/lists-cards.html

[リストとカードの作成]:https://developer.android.com/training/material/lists-cards.html?hl=ja
