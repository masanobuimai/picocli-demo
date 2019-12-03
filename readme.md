# picocliのテスト

* picocliとSpring Bootを組み合わせて簡単なプログラムを作成（MySpringAppとMyCommand）
* Windows10, openjdk11, gradleでビルド
* `gradlew nativeImage`を実行すると，下記のエラーになる
```
Error: Unable to compile C-ABI query code. Make sure GCC toolchain is installed on your system.
```

* [ここ](https://github.com/oracle/graal/issues/1509)を参考にWindows SDK for Windows7をインストールしてみても変化無し。
