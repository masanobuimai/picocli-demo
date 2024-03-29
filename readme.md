# picocliのテスト

* picocliとSpring Bootを組み合わせて簡単なプログラムを作成（MySpringAppとMyCommand）
* プロキシ環境下のWindows10, openjdk11, gradleでビルド
* `gradlew nativeImage`を実行すると，下記のエラーになる
```
Error: Unable to compile C-ABI query code. Make sure GCC toolchain is installed on your system.
```

* [ここ](https://github.com/oracle/graal/issues/1509)を参考にWindows SDK for Windows7をインストールしてみても変化無し。

* Spring Bootだと，`build.gradle`の`graal`ブロックの`mainClass`は下記のようにしておく必要があるようだ。
```
mainClass 'org.springframework.boot.loader.JarLauncher'
```

---
## 解決編

https://twitter.com/RemkoPopma/status/1201797695339319296

* 手動でインストールしたWindows SDKを削除
* 開発環境のJDKのバージョンを11から8に変更
* chocolateyをインストールし，こっち経由でWindows SDKをインストール
* プロキシ環境下のWindows10, Oracle JDK8, gradleでビルド
* `gradlew build` したのち， `gradlew nativeImage` を実行
```
9:06:25: Executing task 'nativeImage'...

> Task :downloadGraalTooling SKIPPED
> Task :extractGraalTooling SKIPPED
> Task :compileJava UP-TO-DATE
> Task :processResources NO-SOURCE
> Task :classes UP-TO-DATE
> Task :jar SKIPPED

> Task :nativeImage
[myApp:5704]    classlist:  14,048.66 ms
[myApp:5704]        (cap):   6,459.32 ms
[myApp:5704]        setup:   9,229.63 ms
[myApp:5704]     analysis:  22,599.25 ms
Warning: Aborting stand-alone image build. com.oracle.svm.hosted.substitute.DeletedElementException: Unsupported field java.net.URL.handlers is reachable
To diagnose the issue, you can add the option --report-unsupported-elements-at-runtime. The unsupported element is then reported at run time when it is accessed the first time.
Detailed message:
Trace: 
	at parsing java.net.URL.setURLStreamHandlerFactory(URL.java:1118)
Call path from entry point to java.net.URL.setURLStreamHandlerFactory(URLStreamHandlerFactory): 
	at java.net.URL.setURLStreamHandlerFactory(URL.java:1110)
	at org.springframework.boot.loader.jar.JarFile.resetCachedUrlHandlers(JarFile.java:408)
	at org.springframework.boot.loader.jar.JarFile.registerUrlProtocolHandler(JarFile.java:398)
	at org.springframework.boot.loader.Launcher.launch(Launcher.java:49)
	at org.springframework.boot.loader.JarLauncher.main(JarLauncher.java:52)
	at com.oracle.svm.core.JavaMainWrapper.runCore(JavaMainWrapper.java:151)
	at com.oracle.svm.core.JavaMainWrapper.run(JavaMainWrapper.java:186)
	at com.oracle.svm.core.code.IsolateEnterStub.JavaMainWrapper_run_5087f5482cc9a6abc971913ece43acb471d2631b(generated:0)

Warning: Use -H:+ReportExceptionStackTraces to print stacktrace of underlying exception
[myApp:17784]    classlist:   5,605.32 ms
[myApp:17784]        (cap):   6,579.86 ms
[myApp:17784]        setup:   9,869.97 ms
[myApp:17784]   (typeflow):  14,263.80 ms
[myApp:17784]    (objects):  12,719.74 ms
[myApp:17784]   (features):   1,616.98 ms
[myApp:17784]     analysis:  29,038.76 ms
[myApp:17784]     (clinit):     306.13 ms
[myApp:17784]     universe:   1,001.83 ms
[myApp:17784]      (parse):   2,834.93 ms
[myApp:17784]     (inline):   6,360.96 ms
[myApp:17784]    (compile):  24,080.57 ms
[myApp:17784]      compile:  34,396.64 ms
[myApp:17784]        image:   1,728.07 ms
[myApp:17784]        write:     613.81 ms
[myApp:17784]      [total]:  82,745.59 ms
Warning: Image 'myApp' is a fallback image that requires a JDK for execution (use --no-fallback to suppress fallback image generation).
native image available at build\graal\myApp.exe (8 MB)

BUILD SUCCESSFUL in 2m 18s
2 actionable tasks: 1 executed, 1 up-to-date
9:08:44: Task execution finished 'nativeImage'.
```

途中，プロキシまわりでゴチャゴチャ言われたけどビルドは無事完了。  
`./build/graal/`にできたファイルの一覧
* `myApp.exe`
* `myApp.exp`
* `myApp.lib`
* `myApp.pdb`

カレントディレクトリを`./build/graal`にして，`myApp`を実行すると普通に実行できる。

### なんかおかしいな...
`myApp.exe`だけを別のディレクトリに移動して，実行するとエラーになる。`./build/graal`内のファイルを全部，別のディレクトリに移動してもエラーになる。
```
C:\temp> myApp.exe
エラー: メイン・クラスorg.springframework.boot.loader.JarLauncherが見つからなかったか ロードできませんでした
```

意図的に`./build/libs`のJarファイルを消して，`./build/graal/myApp.exe`を実行してもエラーになった。これか！でもなんで？（`gradlew nativeImage`のときのワーニングのせいかな？）

*追記*

https://twitter.com/RemkoPopma/status/1202047115037249538

上記のアドバイスに従い，`build.gradle`の`graal`ブロックを以下の様に修正。

```
graal {
  mainClass 'org.springframework.boot.loader.JarLauncher'
  outputName 'myApp'
  option '--report-unsupported-elements-at-runtime'
}
```

gradleの`clean build nativeImage`を順次実行。エラーも無くビルドに成功。
```
16:21:50: Executing task 'nativeImage'...

> Task :downloadGraalTooling SKIPPED
> Task :extractGraalTooling SKIPPED
> Task :compileJava UP-TO-DATE
> Task :processResources NO-SOURCE
> Task :classes UP-TO-DATE
> Task :jar SKIPPED

> Task :nativeImage
[myApp:15104]    classlist:  12,584.39 ms
[myApp:15104]        (cap):   7,696.49 ms
[myApp:15104]        setup:  12,247.52 ms
[myApp:15104]   (typeflow):  16,658.30 ms
[myApp:15104]    (objects):  10,204.75 ms
[myApp:15104]   (features):   1,298.67 ms
[myApp:15104]     analysis:  28,632.58 ms
[myApp:15104]     (clinit):     794.72 ms
[myApp:15104]     universe:   2,055.90 ms
[myApp:15104]      (parse):   3,879.33 ms
[myApp:15104]     (inline):   6,455.75 ms
[myApp:15104]    (compile):  26,991.40 ms
[myApp:15104]      compile:  39,304.24 ms
[myApp:15104]        image:   2,550.87 ms
[myApp:15104]        write:     839.07 ms
[myApp:15104]      [total]:  98,983.53 ms
native image available at build\graal\myApp.exe (9 MB)

BUILD SUCCESSFUL in 1m 46s
2 actionable tasks: 1 executed, 1 up-to-date
16:23:37: Task execution finished 'nativeImage'.
```

だけど `./build/graal/myApp.exe`を実行するとエラーになった。ままならない（これはSpring Boot側の問題っぽいな）
```
C:\picocli-demo\build\graal>myApp.exe
Exception in thread "main" java.lang.IllegalStateException: java.util.zip.ZipException: zip END header not found
        at org.springframework.boot.loader.ExecutableArchiveLauncher.<init>(ExecutableArchiveLauncher.java:42)
        at org.springframework.boot.loader.JarLauncher.<init>(JarLauncher.java:36)
        at org.springframework.boot.loader.JarLauncher.main(JarLauncher.java:52)
Caused by: java.util.zip.ZipException: zip END header not found
        at com.oracle.svm.core.jdk8.zipfile.ZipFile$Source.zerror(ZipFile.java:1317)
        at com.oracle.svm.core.jdk8.zipfile.ZipFile$Source.findEND(ZipFile.java:1218)
        at com.oracle.svm.core.jdk8.zipfile.ZipFile$Source.initCEN(ZipFile.java:1225)
        at com.oracle.svm.core.jdk8.zipfile.ZipFile$Source.<init>(ZipFile.java:1062)
        at com.oracle.svm.core.jdk8.zipfile.ZipFile$Source.get(ZipFile.java:1022)
        at java.util.zip.ZipFile.<init>(ZipFile.java:261)
        at java.util.zip.ZipFile.<init>(ZipFile.java:191)
        at java.util.jar.JarFile.<init>(JarFile.java:166)
        at java.util.jar.JarFile.<init>(JarFile.java:130)
        at org.springframework.boot.loader.jar.JarFile.<init>(JarFile.java:119)
        at org.springframework.boot.loader.jar.JarFile.<init>(JarFile.java:114)
        at org.springframework.boot.loader.jar.JarFile.<init>(JarFile.java:100)
        at org.springframework.boot.loader.jar.JarFile.<init>(JarFile.java:91)
        at org.springframework.boot.loader.archive.JarFileArchive.<init>(JarFileArchive.java:61)
        at org.springframework.boot.loader.archive.JarFileArchive.<init>(JarFileArchive.java:57)
        at org.springframework.boot.loader.Launcher.createArchive(Launcher.java:127)
        at org.springframework.boot.loader.ExecutableArchiveLauncher.<init>(ExecutableArchiveLauncher.java:39)
        ... 2 more
```

### ちなみに...
`gradlew clean` → `gradlew nativeImage` すると，こんなエラーになる。jarタスクがなんでかスキップされてて，jarファイルがないので当たり前といえば当たり前（なんでスキップするの？）
```
9:03:28: Executing task 'nativeImage'...

> Task :downloadGraalTooling SKIPPED
> Task :extractGraalTooling SKIPPED
> Task :compileJava
> Task :processResources NO-SOURCE
> Task :classes
> Task :jar SKIPPED
> Task :nativeImage FAILED
2 actionable tasks: 2 executed

FAILURE: Build failed with an exception.

* What went wrong:
A problem was found with the configuration of task ':nativeImage'.
> File 'c:\picocli-demo\build\libs\picocli-demo-0.0.1-SNAPSHOT.jar' specified for property 'jarFiles' does not exist.

* Try:
Run with --stacktrace option to get the stack trace. Run with --info or --debug option to get more log output. Run with --scan to get full insights.

* Get more help at https://help.gradle.org

BUILD FAILED in 1s
9:03:30: Task execution finished 'nativeImage'.
```
