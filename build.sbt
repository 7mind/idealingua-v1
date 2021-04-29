

enablePlugins(SbtgenVerificationPlugin)

disablePlugins(AssemblyPlugin)

lazy val `idealingua-v1-model` = project.in(file("idealingua-v1/idealingua-v1-model"))
  .settings(
    libraryDependencies ++= Seq(
      compilerPlugin("org.typelevel" % "kind-projector" % V.kind_projector cross CrossVersion.full),
      "org.scalatest" %% "scalatest" % V.scalatest % Test,
      "org.scala-lang" % "scala-reflect" % scalaVersion.value % Provided,
      "io.7mind.izumi" %% "fundamentals-collections" % Izumi.version,
      "io.7mind.izumi" %% "fundamentals-platform" % Izumi.version,
      "io.7mind.izumi" %% "fundamentals-functional" % Izumi.version
    )
  )
  .settings(
    crossScalaVersions := Seq(
      "2.12.13",
      "2.13.5"
    ),
    scalaVersion := crossScalaVersions.value.head,
    coverageEnabled := { (isSnapshot.value, scalaVersion.value) match {
      case (_, "2.12.13") => false
      case (_, _) => coverageEnabled.value
    } },
    organization := "io.7mind.izumi",
    Compile / unmanagedSourceDirectories += baseDirectory.value / ".jvm/src/main/scala" ,
    Compile / unmanagedResourceDirectories += baseDirectory.value / ".jvm/src/main/resources" ,
    Test / unmanagedSourceDirectories += baseDirectory.value / ".jvm/src/test/scala" ,
    Test / unmanagedResourceDirectories += baseDirectory.value / ".jvm/src/test/resources" ,
    scalacOptions ++= Seq(
      s"-Xmacro-settings:product-name=${name.value}",
      s"-Xmacro-settings:product-version=${version.value}",
      s"-Xmacro-settings:product-group=${organization.value}",
      s"-Xmacro-settings:scala-version=${scalaVersion.value}",
      s"-Xmacro-settings:scala-versions=${crossScalaVersions.value.mkString(":")}"
    ),
    Test / testOptions += Tests.Argument("-oDF"),
    scalacOptions ++= { (isSnapshot.value, scalaVersion.value) match {
      case (_, "2.12.13") => Seq(
        "-Xsource:2.13",
        "-Ypartial-unification",
        if (insideCI.value) "-Wconf:any:error" else "-Wconf:any:warning",
        "-Wconf:cat=optimizer:warning",
        "-Wconf:cat=other-match-analysis:error",
        "-Wconf:msg=kind-projector:silent",
        "-Ybackend-parallelism",
        math.min(16, math.max(1, sys.runtime.availableProcessors() - 1)).toString,
        "-Xlint:adapted-args",
        "-Xlint:by-name-right-associative",
        "-Xlint:constant",
        "-Xlint:delayedinit-select",
        "-Xlint:doc-detached",
        "-Xlint:inaccessible",
        "-Xlint:infer-any",
        "-Xlint:missing-interpolator",
        "-Xlint:nullary-override",
        "-Xlint:nullary-unit",
        "-Xlint:option-implicit",
        "-Xlint:package-object-classes",
        "-Xlint:poly-implicit-overload",
        "-Xlint:private-shadow",
        "-Xlint:stars-align",
        "-Xlint:type-parameter-shadow",
        "-Xlint:unsound-match",
        "-opt-warnings:_",
        "-Ywarn-extra-implicit",
        "-Ywarn-unused:_",
        "-Ywarn-adapted-args",
        "-Ywarn-dead-code",
        "-Ywarn-inaccessible",
        "-Ywarn-infer-any",
        "-Ywarn-nullary-override",
        "-Ywarn-nullary-unit",
        "-Ywarn-numeric-widen",
        "-Ywarn-unused-import",
        "-Ywarn-value-discard",
        "-Ycache-plugin-class-loader:always",
        "-Ycache-macro-class-loader:last-modified"
      )
      case (_, "2.13.5") => Seq(
        "-Xlint:_,-eta-sam,-multiarg-infix,-byname-implicit",
        if (insideCI.value) "-Wconf:any:error" else "-Wconf:any:warning",
        "-Wconf:cat=optimizer:warning",
        "-Wconf:cat=other-match-analysis:error",
        "-Wconf:msg=kind-projector:silent",
        "-Ybackend-parallelism",
        math.min(16, math.max(1, sys.runtime.availableProcessors() - 1)).toString,
        "-Wdead-code",
        "-Wextra-implicit",
        "-Wnumeric-widen",
        "-Woctal-literal",
        "-Wvalue-discard",
        "-Wunused:_",
        "-Wmacros:after",
        "-Ycache-plugin-class-loader:always",
        "-Ycache-macro-class-loader:last-modified"
      )
      case (_, _) => Seq.empty
    } },
    scalacOptions ++= { (isSnapshot.value, scalaVersion.value) match {
      case (false, _) => Seq(
        "-opt:l:inline",
        "-opt-inline-from:izumi.**"
      )
      case (_, _) => Seq.empty
    } },
    scalacOptions -= "-Wconf:any:error"
  )
  .enablePlugins(IzumiPlugin)
  .disablePlugins(AssemblyPlugin)

lazy val `idealingua-v1-core` = project.in(file("idealingua-v1/idealingua-v1-core"))
  .dependsOn(
    `idealingua-v1-model` % "test->compile;compile->compile"
  )
  .settings(
    libraryDependencies ++= Seq(
      compilerPlugin("org.typelevel" % "kind-projector" % V.kind_projector cross CrossVersion.full),
      "org.scalatest" %% "scalatest" % V.scalatest % Test,
      "com.lihaoyi" %% "fastparse" % V.fastparse,
      "io.7mind.izumi" %% "fundamentals-reflection" % Izumi.version
    )
  )
  .settings(
    crossScalaVersions := Seq(
      "2.12.13",
      "2.13.5"
    ),
    scalaVersion := crossScalaVersions.value.head,
    coverageEnabled := { (isSnapshot.value, scalaVersion.value) match {
      case (_, "2.12.13") => false
      case (_, _) => coverageEnabled.value
    } },
    organization := "io.7mind.izumi",
    Compile / unmanagedSourceDirectories += baseDirectory.value / ".jvm/src/main/scala" ,
    Compile / unmanagedResourceDirectories += baseDirectory.value / ".jvm/src/main/resources" ,
    Test / unmanagedSourceDirectories += baseDirectory.value / ".jvm/src/test/scala" ,
    Test / unmanagedResourceDirectories += baseDirectory.value / ".jvm/src/test/resources" ,
    scalacOptions ++= Seq(
      s"-Xmacro-settings:product-name=${name.value}",
      s"-Xmacro-settings:product-version=${version.value}",
      s"-Xmacro-settings:product-group=${organization.value}",
      s"-Xmacro-settings:scala-version=${scalaVersion.value}",
      s"-Xmacro-settings:scala-versions=${crossScalaVersions.value.mkString(":")}"
    ),
    Test / testOptions += Tests.Argument("-oDF"),
    scalacOptions ++= { (isSnapshot.value, scalaVersion.value) match {
      case (_, "2.12.13") => Seq(
        "-Xsource:2.13",
        "-Ypartial-unification",
        if (insideCI.value) "-Wconf:any:error" else "-Wconf:any:warning",
        "-Wconf:cat=optimizer:warning",
        "-Wconf:cat=other-match-analysis:error",
        "-Wconf:msg=kind-projector:silent",
        "-Ybackend-parallelism",
        math.min(16, math.max(1, sys.runtime.availableProcessors() - 1)).toString,
        "-Xlint:adapted-args",
        "-Xlint:by-name-right-associative",
        "-Xlint:constant",
        "-Xlint:delayedinit-select",
        "-Xlint:doc-detached",
        "-Xlint:inaccessible",
        "-Xlint:infer-any",
        "-Xlint:missing-interpolator",
        "-Xlint:nullary-override",
        "-Xlint:nullary-unit",
        "-Xlint:option-implicit",
        "-Xlint:package-object-classes",
        "-Xlint:poly-implicit-overload",
        "-Xlint:private-shadow",
        "-Xlint:stars-align",
        "-Xlint:type-parameter-shadow",
        "-Xlint:unsound-match",
        "-opt-warnings:_",
        "-Ywarn-extra-implicit",
        "-Ywarn-unused:_",
        "-Ywarn-adapted-args",
        "-Ywarn-dead-code",
        "-Ywarn-inaccessible",
        "-Ywarn-infer-any",
        "-Ywarn-nullary-override",
        "-Ywarn-nullary-unit",
        "-Ywarn-numeric-widen",
        "-Ywarn-unused-import",
        "-Ywarn-value-discard",
        "-Ycache-plugin-class-loader:always",
        "-Ycache-macro-class-loader:last-modified"
      )
      case (_, "2.13.5") => Seq(
        "-Xlint:_,-eta-sam,-multiarg-infix,-byname-implicit",
        if (insideCI.value) "-Wconf:any:error" else "-Wconf:any:warning",
        "-Wconf:cat=optimizer:warning",
        "-Wconf:cat=other-match-analysis:error",
        "-Wconf:msg=kind-projector:silent",
        "-Ybackend-parallelism",
        math.min(16, math.max(1, sys.runtime.availableProcessors() - 1)).toString,
        "-Wdead-code",
        "-Wextra-implicit",
        "-Wnumeric-widen",
        "-Woctal-literal",
        "-Wvalue-discard",
        "-Wunused:_",
        "-Wmacros:after",
        "-Ycache-plugin-class-loader:always",
        "-Ycache-macro-class-loader:last-modified"
      )
      case (_, _) => Seq.empty
    } },
    scalacOptions ++= { (isSnapshot.value, scalaVersion.value) match {
      case (false, _) => Seq(
        "-opt:l:inline",
        "-opt-inline-from:izumi.**"
      )
      case (_, _) => Seq.empty
    } },
    scalacOptions -= "-Wconf:any:error"
  )
  .enablePlugins(IzumiPlugin)
  .disablePlugins(AssemblyPlugin)

lazy val `idealingua-v1-runtime-rpc-scala` = project.in(file("idealingua-v1/idealingua-v1-runtime-rpc-scala"))
  .settings(
    libraryDependencies ++= Seq(
      compilerPlugin("org.typelevel" % "kind-projector" % V.kind_projector cross CrossVersion.full),
      "org.scalatest" %% "scalatest" % V.scalatest % Test,
      "org.scala-lang" % "scala-reflect" % scalaVersion.value % Provided,
      "org.typelevel" %% "cats-core" % Izumi.Deps.fundamentals_bioJVM.org_typelevel_cats_core_version,
      "org.typelevel" %% "cats-effect" % Izumi.Deps.fundamentals_bioJVM.org_typelevel_cats_effect_version,
      "io.7mind.izumi" %% "fundamentals-bio" % Izumi.version,
      "io.7mind.izumi" %% "fundamentals-json-circe" % Izumi.version,
      "dev.zio" %% "zio" % Izumi.Deps.fundamentals_bioJVM.dev_zio_zio_version % Test,
      "dev.zio" %% "zio-interop-cats" % Izumi.Deps.fundamentals_bioJVM.dev_zio_zio_interop_cats_version % Test
    )
  )
  .settings(
    crossScalaVersions := Seq(
      "2.12.13",
      "2.13.5"
    ),
    scalaVersion := crossScalaVersions.value.head,
    coverageEnabled := { (isSnapshot.value, scalaVersion.value) match {
      case (_, "2.12.13") => false
      case (_, _) => coverageEnabled.value
    } },
    organization := "io.7mind.izumi",
    Compile / unmanagedSourceDirectories += baseDirectory.value / ".jvm/src/main/scala" ,
    Compile / unmanagedResourceDirectories += baseDirectory.value / ".jvm/src/main/resources" ,
    Test / unmanagedSourceDirectories += baseDirectory.value / ".jvm/src/test/scala" ,
    Test / unmanagedResourceDirectories += baseDirectory.value / ".jvm/src/test/resources" ,
    scalacOptions ++= Seq(
      s"-Xmacro-settings:product-name=${name.value}",
      s"-Xmacro-settings:product-version=${version.value}",
      s"-Xmacro-settings:product-group=${organization.value}",
      s"-Xmacro-settings:scala-version=${scalaVersion.value}",
      s"-Xmacro-settings:scala-versions=${crossScalaVersions.value.mkString(":")}"
    ),
    Test / testOptions += Tests.Argument("-oDF"),
    scalacOptions ++= { (isSnapshot.value, scalaVersion.value) match {
      case (_, "2.12.13") => Seq(
        "-Xsource:2.13",
        "-Ypartial-unification",
        if (insideCI.value) "-Wconf:any:error" else "-Wconf:any:warning",
        "-Wconf:cat=optimizer:warning",
        "-Wconf:cat=other-match-analysis:error",
        "-Wconf:msg=kind-projector:silent",
        "-Ybackend-parallelism",
        math.min(16, math.max(1, sys.runtime.availableProcessors() - 1)).toString,
        "-Xlint:adapted-args",
        "-Xlint:by-name-right-associative",
        "-Xlint:constant",
        "-Xlint:delayedinit-select",
        "-Xlint:doc-detached",
        "-Xlint:inaccessible",
        "-Xlint:infer-any",
        "-Xlint:missing-interpolator",
        "-Xlint:nullary-override",
        "-Xlint:nullary-unit",
        "-Xlint:option-implicit",
        "-Xlint:package-object-classes",
        "-Xlint:poly-implicit-overload",
        "-Xlint:private-shadow",
        "-Xlint:stars-align",
        "-Xlint:type-parameter-shadow",
        "-Xlint:unsound-match",
        "-opt-warnings:_",
        "-Ywarn-extra-implicit",
        "-Ywarn-unused:_",
        "-Ywarn-adapted-args",
        "-Ywarn-dead-code",
        "-Ywarn-inaccessible",
        "-Ywarn-infer-any",
        "-Ywarn-nullary-override",
        "-Ywarn-nullary-unit",
        "-Ywarn-numeric-widen",
        "-Ywarn-unused-import",
        "-Ywarn-value-discard",
        "-Ycache-plugin-class-loader:always",
        "-Ycache-macro-class-loader:last-modified"
      )
      case (_, "2.13.5") => Seq(
        "-Xlint:_,-eta-sam,-multiarg-infix,-byname-implicit",
        if (insideCI.value) "-Wconf:any:error" else "-Wconf:any:warning",
        "-Wconf:cat=optimizer:warning",
        "-Wconf:cat=other-match-analysis:error",
        "-Wconf:msg=kind-projector:silent",
        "-Ybackend-parallelism",
        math.min(16, math.max(1, sys.runtime.availableProcessors() - 1)).toString,
        "-Wdead-code",
        "-Wextra-implicit",
        "-Wnumeric-widen",
        "-Woctal-literal",
        "-Wvalue-discard",
        "-Wunused:_",
        "-Wmacros:after",
        "-Ycache-plugin-class-loader:always",
        "-Ycache-macro-class-loader:last-modified"
      )
      case (_, _) => Seq.empty
    } },
    scalacOptions ++= { (isSnapshot.value, scalaVersion.value) match {
      case (false, _) => Seq(
        "-opt:l:inline",
        "-opt-inline-from:izumi.**"
      )
      case (_, _) => Seq.empty
    } },
    scalacOptions -= "-Wconf:any:error"
  )
  .enablePlugins(IzumiPlugin)
  .disablePlugins(AssemblyPlugin)

lazy val `idealingua-v1-runtime-rpc-http4s` = project.in(file("idealingua-v1/idealingua-v1-runtime-rpc-http4s"))
  .dependsOn(
    `idealingua-v1-runtime-rpc-scala` % "test->compile;compile->compile",
    `idealingua-v1-test-defs` % "test->compile"
  )
  .settings(
    libraryDependencies ++= Seq(
      compilerPlugin("org.typelevel" % "kind-projector" % V.kind_projector cross CrossVersion.full),
      "org.scalatest" %% "scalatest" % V.scalatest % Test,
      "org.http4s" %% "http4s-dsl" % V.http4s,
      "org.http4s" %% "http4s-circe" % V.http4s,
      "org.http4s" %% "http4s-blaze-server" % V.http4s,
      "org.http4s" %% "http4s-blaze-client" % V.http4s,
      "org.asynchttpclient" % "async-http-client" % V.asynchttpclient,
      "io.7mind.izumi" %% "logstage-core" % Izumi.version,
      "io.7mind.izumi" %% "logstage-adapter-slf4j" % Izumi.version
    )
  )
  .settings(
    crossScalaVersions := Seq(
      "2.12.13",
      "2.13.5"
    ),
    scalaVersion := crossScalaVersions.value.head,
    coverageEnabled := { (isSnapshot.value, scalaVersion.value) match {
      case (_, "2.12.13") => false
      case (_, _) => coverageEnabled.value
    } },
    organization := "io.7mind.izumi",
    Compile / unmanagedSourceDirectories += baseDirectory.value / ".jvm/src/main/scala" ,
    Compile / unmanagedResourceDirectories += baseDirectory.value / ".jvm/src/main/resources" ,
    Test / unmanagedSourceDirectories += baseDirectory.value / ".jvm/src/test/scala" ,
    Test / unmanagedResourceDirectories += baseDirectory.value / ".jvm/src/test/resources" ,
    scalacOptions ++= Seq(
      s"-Xmacro-settings:product-name=${name.value}",
      s"-Xmacro-settings:product-version=${version.value}",
      s"-Xmacro-settings:product-group=${organization.value}",
      s"-Xmacro-settings:scala-version=${scalaVersion.value}",
      s"-Xmacro-settings:scala-versions=${crossScalaVersions.value.mkString(":")}"
    ),
    Test / testOptions += Tests.Argument("-oDF"),
    scalacOptions ++= { (isSnapshot.value, scalaVersion.value) match {
      case (_, "2.12.13") => Seq(
        "-Xsource:2.13",
        "-Ypartial-unification",
        if (insideCI.value) "-Wconf:any:error" else "-Wconf:any:warning",
        "-Wconf:cat=optimizer:warning",
        "-Wconf:cat=other-match-analysis:error",
        "-Wconf:msg=kind-projector:silent",
        "-Ybackend-parallelism",
        math.min(16, math.max(1, sys.runtime.availableProcessors() - 1)).toString,
        "-Xlint:adapted-args",
        "-Xlint:by-name-right-associative",
        "-Xlint:constant",
        "-Xlint:delayedinit-select",
        "-Xlint:doc-detached",
        "-Xlint:inaccessible",
        "-Xlint:infer-any",
        "-Xlint:missing-interpolator",
        "-Xlint:nullary-override",
        "-Xlint:nullary-unit",
        "-Xlint:option-implicit",
        "-Xlint:package-object-classes",
        "-Xlint:poly-implicit-overload",
        "-Xlint:private-shadow",
        "-Xlint:stars-align",
        "-Xlint:type-parameter-shadow",
        "-Xlint:unsound-match",
        "-opt-warnings:_",
        "-Ywarn-extra-implicit",
        "-Ywarn-unused:_",
        "-Ywarn-adapted-args",
        "-Ywarn-dead-code",
        "-Ywarn-inaccessible",
        "-Ywarn-infer-any",
        "-Ywarn-nullary-override",
        "-Ywarn-nullary-unit",
        "-Ywarn-numeric-widen",
        "-Ywarn-unused-import",
        "-Ywarn-value-discard",
        "-Ycache-plugin-class-loader:always",
        "-Ycache-macro-class-loader:last-modified"
      )
      case (_, "2.13.5") => Seq(
        "-Xlint:_,-eta-sam,-multiarg-infix,-byname-implicit",
        if (insideCI.value) "-Wconf:any:error" else "-Wconf:any:warning",
        "-Wconf:cat=optimizer:warning",
        "-Wconf:cat=other-match-analysis:error",
        "-Wconf:msg=kind-projector:silent",
        "-Ybackend-parallelism",
        math.min(16, math.max(1, sys.runtime.availableProcessors() - 1)).toString,
        "-Wdead-code",
        "-Wextra-implicit",
        "-Wnumeric-widen",
        "-Woctal-literal",
        "-Wvalue-discard",
        "-Wunused:_",
        "-Wmacros:after",
        "-Ycache-plugin-class-loader:always",
        "-Ycache-macro-class-loader:last-modified"
      )
      case (_, _) => Seq.empty
    } },
    scalacOptions ++= { (isSnapshot.value, scalaVersion.value) match {
      case (false, _) => Seq(
        "-opt:l:inline",
        "-opt-inline-from:izumi.**"
      )
      case (_, _) => Seq.empty
    } },
    scalacOptions -= "-Wconf:any:error"
  )
  .enablePlugins(IzumiPlugin)
  .disablePlugins(AssemblyPlugin)

lazy val `idealingua-v1-transpilers` = project.in(file("idealingua-v1/idealingua-v1-transpilers"))
  .dependsOn(
    `idealingua-v1-core` % "test->compile;compile->compile",
    `idealingua-v1-runtime-rpc-scala` % "test->compile;compile->compile",
    `idealingua-v1-test-defs` % "test->compile",
    `idealingua-v1-runtime-rpc-typescript` % "test->compile",
    `idealingua-v1-runtime-rpc-go` % "test->compile",
    `idealingua-v1-runtime-rpc-csharp` % "test->compile"
  )
  .settings(
    libraryDependencies ++= Seq(
      compilerPlugin("org.typelevel" % "kind-projector" % V.kind_projector cross CrossVersion.full),
      "org.scalatest" %% "scalatest" % V.scalatest % Test,
      "org.scala-lang.modules" %% "scala-xml" % V.scala_xml,
      "org.scalameta" %% "scalameta" % V.scalameta,
      "io.7mind.izumi" %% "fundamentals-bio" % Izumi.version,
      "io.7mind.izumi" %% "fundamentals-json-circe" % Izumi.version
    )
  )
  .settings(
    crossScalaVersions := Seq(
      "2.12.13",
      "2.13.5"
    ),
    scalaVersion := crossScalaVersions.value.head,
    coverageEnabled := { (isSnapshot.value, scalaVersion.value) match {
      case (_, "2.12.13") => false
      case (_, _) => coverageEnabled.value
    } },
    Test / fork := true,
    organization := "io.7mind.izumi",
    Compile / unmanagedSourceDirectories += baseDirectory.value / ".jvm/src/main/scala" ,
    Compile / unmanagedResourceDirectories += baseDirectory.value / ".jvm/src/main/resources" ,
    Test / unmanagedSourceDirectories += baseDirectory.value / ".jvm/src/test/scala" ,
    Test / unmanagedResourceDirectories += baseDirectory.value / ".jvm/src/test/resources" ,
    scalacOptions ++= Seq(
      s"-Xmacro-settings:product-name=${name.value}",
      s"-Xmacro-settings:product-version=${version.value}",
      s"-Xmacro-settings:product-group=${organization.value}",
      s"-Xmacro-settings:scala-version=${scalaVersion.value}",
      s"-Xmacro-settings:scala-versions=${crossScalaVersions.value.mkString(":")}"
    ),
    Test / testOptions += Tests.Argument("-oDF"),
    scalacOptions ++= { (isSnapshot.value, scalaVersion.value) match {
      case (_, "2.12.13") => Seq(
        "-Xsource:2.13",
        "-Ypartial-unification",
        if (insideCI.value) "-Wconf:any:error" else "-Wconf:any:warning",
        "-Wconf:cat=optimizer:warning",
        "-Wconf:cat=other-match-analysis:error",
        "-Wconf:msg=kind-projector:silent",
        "-Ybackend-parallelism",
        math.min(16, math.max(1, sys.runtime.availableProcessors() - 1)).toString,
        "-Xlint:adapted-args",
        "-Xlint:by-name-right-associative",
        "-Xlint:constant",
        "-Xlint:delayedinit-select",
        "-Xlint:doc-detached",
        "-Xlint:inaccessible",
        "-Xlint:infer-any",
        "-Xlint:missing-interpolator",
        "-Xlint:nullary-override",
        "-Xlint:nullary-unit",
        "-Xlint:option-implicit",
        "-Xlint:package-object-classes",
        "-Xlint:poly-implicit-overload",
        "-Xlint:private-shadow",
        "-Xlint:stars-align",
        "-Xlint:type-parameter-shadow",
        "-Xlint:unsound-match",
        "-opt-warnings:_",
        "-Ywarn-extra-implicit",
        "-Ywarn-unused:_",
        "-Ywarn-adapted-args",
        "-Ywarn-dead-code",
        "-Ywarn-inaccessible",
        "-Ywarn-infer-any",
        "-Ywarn-nullary-override",
        "-Ywarn-nullary-unit",
        "-Ywarn-numeric-widen",
        "-Ywarn-unused-import",
        "-Ywarn-value-discard",
        "-Ycache-plugin-class-loader:always",
        "-Ycache-macro-class-loader:last-modified"
      )
      case (_, "2.13.5") => Seq(
        "-Xlint:_,-eta-sam,-multiarg-infix,-byname-implicit",
        if (insideCI.value) "-Wconf:any:error" else "-Wconf:any:warning",
        "-Wconf:cat=optimizer:warning",
        "-Wconf:cat=other-match-analysis:error",
        "-Wconf:msg=kind-projector:silent",
        "-Ybackend-parallelism",
        math.min(16, math.max(1, sys.runtime.availableProcessors() - 1)).toString,
        "-Wdead-code",
        "-Wextra-implicit",
        "-Wnumeric-widen",
        "-Woctal-literal",
        "-Wvalue-discard",
        "-Wunused:_",
        "-Wmacros:after",
        "-Ycache-plugin-class-loader:always",
        "-Ycache-macro-class-loader:last-modified"
      )
      case (_, _) => Seq.empty
    } },
    scalacOptions ++= { (isSnapshot.value, scalaVersion.value) match {
      case (false, _) => Seq(
        "-opt:l:inline",
        "-opt-inline-from:izumi.**"
      )
      case (_, _) => Seq.empty
    } },
    scalacOptions -= "-Wconf:any:error"
  )
  .enablePlugins(IzumiPlugin)
  .disablePlugins(AssemblyPlugin)

lazy val `idealingua-v1-test-defs` = project.in(file("idealingua-v1/idealingua-v1-test-defs"))
  .dependsOn(
    `idealingua-v1-runtime-rpc-scala` % "test->compile;compile->compile"
  )
  .settings(
    libraryDependencies ++= Seq(
      compilerPlugin("org.typelevel" % "kind-projector" % V.kind_projector cross CrossVersion.full),
      "org.scalatest" %% "scalatest" % V.scalatest % Test,
      "dev.zio" %% "zio" % Izumi.Deps.fundamentals_bioJVM.dev_zio_zio_version,
      "dev.zio" %% "zio-interop-cats" % Izumi.Deps.fundamentals_bioJVM.dev_zio_zio_interop_cats_version
    )
  )
  .settings(
    crossScalaVersions := Seq(
      "2.12.13",
      "2.13.5"
    ),
    scalaVersion := crossScalaVersions.value.head,
    coverageEnabled := { (isSnapshot.value, scalaVersion.value) match {
      case (_, "2.12.13") => false
      case (_, _) => coverageEnabled.value
    } },
    organization := "io.7mind.izumi",
    Compile / unmanagedSourceDirectories += baseDirectory.value / ".jvm/src/main/scala" ,
    Compile / unmanagedResourceDirectories += baseDirectory.value / ".jvm/src/main/resources" ,
    Test / unmanagedSourceDirectories += baseDirectory.value / ".jvm/src/test/scala" ,
    Test / unmanagedResourceDirectories += baseDirectory.value / ".jvm/src/test/resources" ,
    scalacOptions ++= Seq(
      s"-Xmacro-settings:product-name=${name.value}",
      s"-Xmacro-settings:product-version=${version.value}",
      s"-Xmacro-settings:product-group=${organization.value}",
      s"-Xmacro-settings:scala-version=${scalaVersion.value}",
      s"-Xmacro-settings:scala-versions=${crossScalaVersions.value.mkString(":")}"
    ),
    Test / testOptions += Tests.Argument("-oDF"),
    scalacOptions ++= { (isSnapshot.value, scalaVersion.value) match {
      case (_, "2.12.13") => Seq(
        "-Xsource:2.13",
        "-Ypartial-unification",
        if (insideCI.value) "-Wconf:any:error" else "-Wconf:any:warning",
        "-Wconf:cat=optimizer:warning",
        "-Wconf:cat=other-match-analysis:error",
        "-Wconf:msg=kind-projector:silent",
        "-Ybackend-parallelism",
        math.min(16, math.max(1, sys.runtime.availableProcessors() - 1)).toString,
        "-Xlint:adapted-args",
        "-Xlint:by-name-right-associative",
        "-Xlint:constant",
        "-Xlint:delayedinit-select",
        "-Xlint:doc-detached",
        "-Xlint:inaccessible",
        "-Xlint:infer-any",
        "-Xlint:missing-interpolator",
        "-Xlint:nullary-override",
        "-Xlint:nullary-unit",
        "-Xlint:option-implicit",
        "-Xlint:package-object-classes",
        "-Xlint:poly-implicit-overload",
        "-Xlint:private-shadow",
        "-Xlint:stars-align",
        "-Xlint:type-parameter-shadow",
        "-Xlint:unsound-match",
        "-opt-warnings:_",
        "-Ywarn-extra-implicit",
        "-Ywarn-unused:_",
        "-Ywarn-adapted-args",
        "-Ywarn-dead-code",
        "-Ywarn-inaccessible",
        "-Ywarn-infer-any",
        "-Ywarn-nullary-override",
        "-Ywarn-nullary-unit",
        "-Ywarn-numeric-widen",
        "-Ywarn-unused-import",
        "-Ywarn-value-discard",
        "-Ycache-plugin-class-loader:always",
        "-Ycache-macro-class-loader:last-modified"
      )
      case (_, "2.13.5") => Seq(
        "-Xlint:_,-eta-sam,-multiarg-infix,-byname-implicit",
        if (insideCI.value) "-Wconf:any:error" else "-Wconf:any:warning",
        "-Wconf:cat=optimizer:warning",
        "-Wconf:cat=other-match-analysis:error",
        "-Wconf:msg=kind-projector:silent",
        "-Ybackend-parallelism",
        math.min(16, math.max(1, sys.runtime.availableProcessors() - 1)).toString,
        "-Wdead-code",
        "-Wextra-implicit",
        "-Wnumeric-widen",
        "-Woctal-literal",
        "-Wvalue-discard",
        "-Wunused:_",
        "-Wmacros:after",
        "-Ycache-plugin-class-loader:always",
        "-Ycache-macro-class-loader:last-modified"
      )
      case (_, _) => Seq.empty
    } },
    scalacOptions ++= { (isSnapshot.value, scalaVersion.value) match {
      case (false, _) => Seq(
        "-opt:l:inline",
        "-opt-inline-from:izumi.**"
      )
      case (_, _) => Seq.empty
    } },
    scalacOptions -= "-Wconf:any:error"
  )
  .enablePlugins(IzumiPlugin)
  .disablePlugins(AssemblyPlugin)

lazy val `idealingua-v1-runtime-rpc-typescript` = project.in(file("idealingua-v1/idealingua-v1-runtime-rpc-typescript"))
  .settings(
    libraryDependencies ++= Seq(
      compilerPlugin("org.typelevel" % "kind-projector" % V.kind_projector cross CrossVersion.full),
      "org.scalatest" %% "scalatest" % V.scalatest % Test
    )
  )
  .settings(
    crossScalaVersions := Seq(
      "2.12.13",
      "2.13.5"
    ),
    scalaVersion := crossScalaVersions.value.head,
    coverageEnabled := { (isSnapshot.value, scalaVersion.value) match {
      case (_, "2.12.13") => false
      case (_, _) => coverageEnabled.value
    } },
    organization := "io.7mind.izumi",
    Compile / unmanagedSourceDirectories += baseDirectory.value / ".jvm/src/main/scala" ,
    Compile / unmanagedResourceDirectories += baseDirectory.value / ".jvm/src/main/resources" ,
    Test / unmanagedSourceDirectories += baseDirectory.value / ".jvm/src/test/scala" ,
    Test / unmanagedResourceDirectories += baseDirectory.value / ".jvm/src/test/resources" ,
    scalacOptions ++= Seq(
      s"-Xmacro-settings:product-name=${name.value}",
      s"-Xmacro-settings:product-version=${version.value}",
      s"-Xmacro-settings:product-group=${organization.value}",
      s"-Xmacro-settings:scala-version=${scalaVersion.value}",
      s"-Xmacro-settings:scala-versions=${crossScalaVersions.value.mkString(":")}"
    ),
    Test / testOptions += Tests.Argument("-oDF"),
    scalacOptions ++= { (isSnapshot.value, scalaVersion.value) match {
      case (_, "2.12.13") => Seq(
        "-Xsource:2.13",
        "-Ypartial-unification",
        if (insideCI.value) "-Wconf:any:error" else "-Wconf:any:warning",
        "-Wconf:cat=optimizer:warning",
        "-Wconf:cat=other-match-analysis:error",
        "-Wconf:msg=kind-projector:silent",
        "-Ybackend-parallelism",
        math.min(16, math.max(1, sys.runtime.availableProcessors() - 1)).toString,
        "-Xlint:adapted-args",
        "-Xlint:by-name-right-associative",
        "-Xlint:constant",
        "-Xlint:delayedinit-select",
        "-Xlint:doc-detached",
        "-Xlint:inaccessible",
        "-Xlint:infer-any",
        "-Xlint:missing-interpolator",
        "-Xlint:nullary-override",
        "-Xlint:nullary-unit",
        "-Xlint:option-implicit",
        "-Xlint:package-object-classes",
        "-Xlint:poly-implicit-overload",
        "-Xlint:private-shadow",
        "-Xlint:stars-align",
        "-Xlint:type-parameter-shadow",
        "-Xlint:unsound-match",
        "-opt-warnings:_",
        "-Ywarn-extra-implicit",
        "-Ywarn-unused:_",
        "-Ywarn-adapted-args",
        "-Ywarn-dead-code",
        "-Ywarn-inaccessible",
        "-Ywarn-infer-any",
        "-Ywarn-nullary-override",
        "-Ywarn-nullary-unit",
        "-Ywarn-numeric-widen",
        "-Ywarn-unused-import",
        "-Ywarn-value-discard",
        "-Ycache-plugin-class-loader:always",
        "-Ycache-macro-class-loader:last-modified"
      )
      case (_, "2.13.5") => Seq(
        "-Xlint:_,-eta-sam,-multiarg-infix,-byname-implicit",
        if (insideCI.value) "-Wconf:any:error" else "-Wconf:any:warning",
        "-Wconf:cat=optimizer:warning",
        "-Wconf:cat=other-match-analysis:error",
        "-Wconf:msg=kind-projector:silent",
        "-Ybackend-parallelism",
        math.min(16, math.max(1, sys.runtime.availableProcessors() - 1)).toString,
        "-Wdead-code",
        "-Wextra-implicit",
        "-Wnumeric-widen",
        "-Woctal-literal",
        "-Wvalue-discard",
        "-Wunused:_",
        "-Wmacros:after",
        "-Ycache-plugin-class-loader:always",
        "-Ycache-macro-class-loader:last-modified"
      )
      case (_, _) => Seq.empty
    } },
    scalacOptions ++= { (isSnapshot.value, scalaVersion.value) match {
      case (false, _) => Seq(
        "-opt:l:inline",
        "-opt-inline-from:izumi.**"
      )
      case (_, _) => Seq.empty
    } },
    scalacOptions -= "-Wconf:any:error"
  )
  .enablePlugins(IzumiPlugin)
  .disablePlugins(AssemblyPlugin)

lazy val `idealingua-v1-runtime-rpc-go` = project.in(file("idealingua-v1/idealingua-v1-runtime-rpc-go"))
  .settings(
    libraryDependencies ++= Seq(
      compilerPlugin("org.typelevel" % "kind-projector" % V.kind_projector cross CrossVersion.full),
      "org.scalatest" %% "scalatest" % V.scalatest % Test
    )
  )
  .settings(
    crossScalaVersions := Seq(
      "2.12.13",
      "2.13.5"
    ),
    scalaVersion := crossScalaVersions.value.head,
    coverageEnabled := { (isSnapshot.value, scalaVersion.value) match {
      case (_, "2.12.13") => false
      case (_, _) => coverageEnabled.value
    } },
    organization := "io.7mind.izumi",
    Compile / unmanagedSourceDirectories += baseDirectory.value / ".jvm/src/main/scala" ,
    Compile / unmanagedResourceDirectories += baseDirectory.value / ".jvm/src/main/resources" ,
    Test / unmanagedSourceDirectories += baseDirectory.value / ".jvm/src/test/scala" ,
    Test / unmanagedResourceDirectories += baseDirectory.value / ".jvm/src/test/resources" ,
    scalacOptions ++= Seq(
      s"-Xmacro-settings:product-name=${name.value}",
      s"-Xmacro-settings:product-version=${version.value}",
      s"-Xmacro-settings:product-group=${organization.value}",
      s"-Xmacro-settings:scala-version=${scalaVersion.value}",
      s"-Xmacro-settings:scala-versions=${crossScalaVersions.value.mkString(":")}"
    ),
    Test / testOptions += Tests.Argument("-oDF"),
    scalacOptions ++= { (isSnapshot.value, scalaVersion.value) match {
      case (_, "2.12.13") => Seq(
        "-Xsource:2.13",
        "-Ypartial-unification",
        if (insideCI.value) "-Wconf:any:error" else "-Wconf:any:warning",
        "-Wconf:cat=optimizer:warning",
        "-Wconf:cat=other-match-analysis:error",
        "-Wconf:msg=kind-projector:silent",
        "-Ybackend-parallelism",
        math.min(16, math.max(1, sys.runtime.availableProcessors() - 1)).toString,
        "-Xlint:adapted-args",
        "-Xlint:by-name-right-associative",
        "-Xlint:constant",
        "-Xlint:delayedinit-select",
        "-Xlint:doc-detached",
        "-Xlint:inaccessible",
        "-Xlint:infer-any",
        "-Xlint:missing-interpolator",
        "-Xlint:nullary-override",
        "-Xlint:nullary-unit",
        "-Xlint:option-implicit",
        "-Xlint:package-object-classes",
        "-Xlint:poly-implicit-overload",
        "-Xlint:private-shadow",
        "-Xlint:stars-align",
        "-Xlint:type-parameter-shadow",
        "-Xlint:unsound-match",
        "-opt-warnings:_",
        "-Ywarn-extra-implicit",
        "-Ywarn-unused:_",
        "-Ywarn-adapted-args",
        "-Ywarn-dead-code",
        "-Ywarn-inaccessible",
        "-Ywarn-infer-any",
        "-Ywarn-nullary-override",
        "-Ywarn-nullary-unit",
        "-Ywarn-numeric-widen",
        "-Ywarn-unused-import",
        "-Ywarn-value-discard",
        "-Ycache-plugin-class-loader:always",
        "-Ycache-macro-class-loader:last-modified"
      )
      case (_, "2.13.5") => Seq(
        "-Xlint:_,-eta-sam,-multiarg-infix,-byname-implicit",
        if (insideCI.value) "-Wconf:any:error" else "-Wconf:any:warning",
        "-Wconf:cat=optimizer:warning",
        "-Wconf:cat=other-match-analysis:error",
        "-Wconf:msg=kind-projector:silent",
        "-Ybackend-parallelism",
        math.min(16, math.max(1, sys.runtime.availableProcessors() - 1)).toString,
        "-Wdead-code",
        "-Wextra-implicit",
        "-Wnumeric-widen",
        "-Woctal-literal",
        "-Wvalue-discard",
        "-Wunused:_",
        "-Wmacros:after",
        "-Ycache-plugin-class-loader:always",
        "-Ycache-macro-class-loader:last-modified"
      )
      case (_, _) => Seq.empty
    } },
    scalacOptions ++= { (isSnapshot.value, scalaVersion.value) match {
      case (false, _) => Seq(
        "-opt:l:inline",
        "-opt-inline-from:izumi.**"
      )
      case (_, _) => Seq.empty
    } },
    scalacOptions -= "-Wconf:any:error"
  )
  .enablePlugins(IzumiPlugin)
  .disablePlugins(AssemblyPlugin)

lazy val `idealingua-v1-runtime-rpc-csharp` = project.in(file("idealingua-v1/idealingua-v1-runtime-rpc-csharp"))
  .settings(
    libraryDependencies ++= Seq(
      compilerPlugin("org.typelevel" % "kind-projector" % V.kind_projector cross CrossVersion.full),
      "org.scalatest" %% "scalatest" % V.scalatest % Test
    )
  )
  .settings(
    crossScalaVersions := Seq(
      "2.12.13",
      "2.13.5"
    ),
    scalaVersion := crossScalaVersions.value.head,
    coverageEnabled := { (isSnapshot.value, scalaVersion.value) match {
      case (_, "2.12.13") => false
      case (_, _) => coverageEnabled.value
    } },
    organization := "io.7mind.izumi",
    Compile / unmanagedSourceDirectories += baseDirectory.value / ".jvm/src/main/scala" ,
    Compile / unmanagedResourceDirectories += baseDirectory.value / ".jvm/src/main/resources" ,
    Test / unmanagedSourceDirectories += baseDirectory.value / ".jvm/src/test/scala" ,
    Test / unmanagedResourceDirectories += baseDirectory.value / ".jvm/src/test/resources" ,
    scalacOptions ++= Seq(
      s"-Xmacro-settings:product-name=${name.value}",
      s"-Xmacro-settings:product-version=${version.value}",
      s"-Xmacro-settings:product-group=${organization.value}",
      s"-Xmacro-settings:scala-version=${scalaVersion.value}",
      s"-Xmacro-settings:scala-versions=${crossScalaVersions.value.mkString(":")}"
    ),
    Test / testOptions += Tests.Argument("-oDF"),
    scalacOptions ++= { (isSnapshot.value, scalaVersion.value) match {
      case (_, "2.12.13") => Seq(
        "-Xsource:2.13",
        "-Ypartial-unification",
        if (insideCI.value) "-Wconf:any:error" else "-Wconf:any:warning",
        "-Wconf:cat=optimizer:warning",
        "-Wconf:cat=other-match-analysis:error",
        "-Wconf:msg=kind-projector:silent",
        "-Ybackend-parallelism",
        math.min(16, math.max(1, sys.runtime.availableProcessors() - 1)).toString,
        "-Xlint:adapted-args",
        "-Xlint:by-name-right-associative",
        "-Xlint:constant",
        "-Xlint:delayedinit-select",
        "-Xlint:doc-detached",
        "-Xlint:inaccessible",
        "-Xlint:infer-any",
        "-Xlint:missing-interpolator",
        "-Xlint:nullary-override",
        "-Xlint:nullary-unit",
        "-Xlint:option-implicit",
        "-Xlint:package-object-classes",
        "-Xlint:poly-implicit-overload",
        "-Xlint:private-shadow",
        "-Xlint:stars-align",
        "-Xlint:type-parameter-shadow",
        "-Xlint:unsound-match",
        "-opt-warnings:_",
        "-Ywarn-extra-implicit",
        "-Ywarn-unused:_",
        "-Ywarn-adapted-args",
        "-Ywarn-dead-code",
        "-Ywarn-inaccessible",
        "-Ywarn-infer-any",
        "-Ywarn-nullary-override",
        "-Ywarn-nullary-unit",
        "-Ywarn-numeric-widen",
        "-Ywarn-unused-import",
        "-Ywarn-value-discard",
        "-Ycache-plugin-class-loader:always",
        "-Ycache-macro-class-loader:last-modified"
      )
      case (_, "2.13.5") => Seq(
        "-Xlint:_,-eta-sam,-multiarg-infix,-byname-implicit",
        if (insideCI.value) "-Wconf:any:error" else "-Wconf:any:warning",
        "-Wconf:cat=optimizer:warning",
        "-Wconf:cat=other-match-analysis:error",
        "-Wconf:msg=kind-projector:silent",
        "-Ybackend-parallelism",
        math.min(16, math.max(1, sys.runtime.availableProcessors() - 1)).toString,
        "-Wdead-code",
        "-Wextra-implicit",
        "-Wnumeric-widen",
        "-Woctal-literal",
        "-Wvalue-discard",
        "-Wunused:_",
        "-Wmacros:after",
        "-Ycache-plugin-class-loader:always",
        "-Ycache-macro-class-loader:last-modified"
      )
      case (_, _) => Seq.empty
    } },
    scalacOptions ++= { (isSnapshot.value, scalaVersion.value) match {
      case (false, _) => Seq(
        "-opt:l:inline",
        "-opt-inline-from:izumi.**"
      )
      case (_, _) => Seq.empty
    } },
    scalacOptions -= "-Wconf:any:error"
  )
  .enablePlugins(IzumiPlugin)
  .disablePlugins(AssemblyPlugin)

lazy val `idealingua-v1-compiler` = project.in(file("idealingua-v1/idealingua-v1-compiler"))
  .dependsOn(
    `idealingua-v1-transpilers` % "test->compile;compile->compile",
    `idealingua-v1-runtime-rpc-scala` % "test->compile;compile->compile",
    `idealingua-v1-runtime-rpc-typescript` % "test->compile;compile->compile",
    `idealingua-v1-runtime-rpc-go` % "test->compile;compile->compile",
    `idealingua-v1-runtime-rpc-csharp` % "test->compile;compile->compile",
    `idealingua-v1-test-defs` % "test->compile;compile->compile"
  )
  .settings(
    libraryDependencies ++= Seq(
      compilerPlugin("org.typelevel" % "kind-projector" % V.kind_projector cross CrossVersion.full),
      "org.scalatest" %% "scalatest" % V.scalatest % Test,
      "com.typesafe" % "config" % V.typesafe_config
    )
  )
  .settings(
    crossScalaVersions := Seq(
      "2.12.13",
      "2.13.5"
    ),
    scalaVersion := crossScalaVersions.value.head,
    coverageEnabled := { (isSnapshot.value, scalaVersion.value) match {
      case (_, "2.12.13") => false
      case (_, _) => coverageEnabled.value
    } },
    organization := "io.7mind.izumi",
    Compile / unmanagedSourceDirectories += baseDirectory.value / ".jvm/src/main/scala" ,
    Compile / unmanagedResourceDirectories += baseDirectory.value / ".jvm/src/main/resources" ,
    Test / unmanagedSourceDirectories += baseDirectory.value / ".jvm/src/test/scala" ,
    Test / unmanagedResourceDirectories += baseDirectory.value / ".jvm/src/test/resources" ,
    scalacOptions ++= Seq(
      s"-Xmacro-settings:product-name=${name.value}",
      s"-Xmacro-settings:product-version=${version.value}",
      s"-Xmacro-settings:product-group=${organization.value}",
      s"-Xmacro-settings:scala-version=${scalaVersion.value}",
      s"-Xmacro-settings:scala-versions=${crossScalaVersions.value.mkString(":")}"
    ),
    Test / testOptions += Tests.Argument("-oDF"),
    scalacOptions ++= { (isSnapshot.value, scalaVersion.value) match {
      case (_, "2.12.13") => Seq(
        "-Xsource:2.13",
        "-Ypartial-unification",
        if (insideCI.value) "-Wconf:any:error" else "-Wconf:any:warning",
        "-Wconf:cat=optimizer:warning",
        "-Wconf:cat=other-match-analysis:error",
        "-Wconf:msg=kind-projector:silent",
        "-Ybackend-parallelism",
        math.min(16, math.max(1, sys.runtime.availableProcessors() - 1)).toString,
        "-Xlint:adapted-args",
        "-Xlint:by-name-right-associative",
        "-Xlint:constant",
        "-Xlint:delayedinit-select",
        "-Xlint:doc-detached",
        "-Xlint:inaccessible",
        "-Xlint:infer-any",
        "-Xlint:missing-interpolator",
        "-Xlint:nullary-override",
        "-Xlint:nullary-unit",
        "-Xlint:option-implicit",
        "-Xlint:package-object-classes",
        "-Xlint:poly-implicit-overload",
        "-Xlint:private-shadow",
        "-Xlint:stars-align",
        "-Xlint:type-parameter-shadow",
        "-Xlint:unsound-match",
        "-opt-warnings:_",
        "-Ywarn-extra-implicit",
        "-Ywarn-unused:_",
        "-Ywarn-adapted-args",
        "-Ywarn-dead-code",
        "-Ywarn-inaccessible",
        "-Ywarn-infer-any",
        "-Ywarn-nullary-override",
        "-Ywarn-nullary-unit",
        "-Ywarn-numeric-widen",
        "-Ywarn-unused-import",
        "-Ywarn-value-discard",
        "-Ycache-plugin-class-loader:always",
        "-Ycache-macro-class-loader:last-modified"
      )
      case (_, "2.13.5") => Seq(
        "-Xlint:_,-eta-sam,-multiarg-infix,-byname-implicit",
        if (insideCI.value) "-Wconf:any:error" else "-Wconf:any:warning",
        "-Wconf:cat=optimizer:warning",
        "-Wconf:cat=other-match-analysis:error",
        "-Wconf:msg=kind-projector:silent",
        "-Ybackend-parallelism",
        math.min(16, math.max(1, sys.runtime.availableProcessors() - 1)).toString,
        "-Wdead-code",
        "-Wextra-implicit",
        "-Wnumeric-widen",
        "-Woctal-literal",
        "-Wvalue-discard",
        "-Wunused:_",
        "-Wmacros:after",
        "-Ycache-plugin-class-loader:always",
        "-Ycache-macro-class-loader:last-modified"
      )
      case (_, _) => Seq.empty
    } },
    scalacOptions ++= { (isSnapshot.value, scalaVersion.value) match {
      case (false, _) => Seq(
        "-opt:l:inline",
        "-opt-inline-from:izumi.**"
      )
      case (_, _) => Seq.empty
    } },
    scalacOptions -= "-Wconf:any:error",
    assembly / mainClass := Some("izumi.idealingua.compiler.CommandlineIDLCompiler"),
    assembly / assemblyMergeStrategy := {
          case path if path.contains("scala/annotation/nowarn") =>
            MergeStrategy.last
          case p =>
            (assembly / assemblyMergeStrategy).value(p)
    },
    Compile / assembly / artifact := {
          val art = (Compile / assembly / artifact).value
          art.withClassifier(Some("assembly"))
    },
    addArtifact(Compile / assembly / artifact, assembly)
  )
  .enablePlugins(AssemblyPlugin, IzumiPlugin)

lazy val `idealingua` = (project in file(".agg/idealingua-v1-idealingua"))
  .settings(
    publish / skip := true,
    crossScalaVersions := Seq(
      "2.12.13",
      "2.13.5"
    ),
    scalaVersion := crossScalaVersions.value.head
  )
  .enablePlugins(IzumiPlugin)
  .disablePlugins(AssemblyPlugin)
  .aggregate(
    `idealingua-v1-model`,
    `idealingua-v1-core`,
    `idealingua-v1-runtime-rpc-scala`,
    `idealingua-v1-runtime-rpc-http4s`,
    `idealingua-v1-transpilers`,
    `idealingua-v1-test-defs`,
    `idealingua-v1-runtime-rpc-typescript`,
    `idealingua-v1-runtime-rpc-go`,
    `idealingua-v1-runtime-rpc-csharp`,
    `idealingua-v1-compiler`
  )

lazy val `idealingua-jvm` = (project in file(".agg/idealingua-v1-idealingua-jvm"))
  .settings(
    publish / skip := true,
    crossScalaVersions := Seq(
      "2.12.13",
      "2.13.5"
    ),
    scalaVersion := crossScalaVersions.value.head
  )
  .disablePlugins(AssemblyPlugin)
  .aggregate(
    `idealingua-v1-model`,
    `idealingua-v1-core`,
    `idealingua-v1-runtime-rpc-scala`,
    `idealingua-v1-runtime-rpc-http4s`,
    `idealingua-v1-transpilers`,
    `idealingua-v1-test-defs`,
    `idealingua-v1-runtime-rpc-typescript`,
    `idealingua-v1-runtime-rpc-go`,
    `idealingua-v1-runtime-rpc-csharp`,
    `idealingua-v1-compiler`
  )

lazy val `idealingua-v1-jvm` = (project in file(".agg/.agg-jvm"))
  .settings(
    publish / skip := true,
    crossScalaVersions := Seq(
      "2.12.13",
      "2.13.5"
    ),
    scalaVersion := crossScalaVersions.value.head
  )
  .disablePlugins(AssemblyPlugin)
  .aggregate(
    `idealingua-jvm`
  )

lazy val `idealingua-v1` = (project in file("."))
  .settings(
    publish / skip := true,
    ThisBuild / publishMavenStyle := true,
    ThisBuild / scalacOptions ++= Seq(
      "-encoding",
      "UTF-8",
      "-target:jvm-1.8",
      "-feature",
      "-unchecked",
      "-deprecation",
      "-language:higherKinds",
      "-explaintypes"
    ),
    ThisBuild / javacOptions ++= Seq(
      "-encoding",
      "UTF-8",
      "-source",
      "1.8",
      "-target",
      "1.8",
      "-deprecation",
      "-parameters",
      "-Xlint:all",
      "-XDignore.symbol.file"
    ),
    ThisBuild / scalacOptions ++= Seq(
      s"-Xmacro-settings:sbt-version=${sbtVersion.value}",
      s"-Xmacro-settings:git-repo-clean=${com.typesafe.sbt.SbtGit.GitKeys.gitUncommittedChanges.value}",
      s"-Xmacro-settings:git-branch=${com.typesafe.sbt.SbtGit.GitKeys.gitCurrentBranch.value}",
      s"-Xmacro-settings:git-described-version=${com.typesafe.sbt.SbtGit.GitKeys.gitDescribedVersion.value.getOrElse("")}",
      s"-Xmacro-settings:git-head-commit=${com.typesafe.sbt.SbtGit.GitKeys.gitHeadCommit.value.getOrElse("")}"
    ),
    crossScalaVersions := Nil,
    scalaVersion := "2.12.13",
    ThisBuild / organization := "io.7mind.izumi",
    sonatypeProfileName := "io.7mind",
    sonatypeSessionName := s"[sbt-sonatype] ${name.value} ${version.value} ${java.util.UUID.randomUUID}",
    ThisBuild / publishTo := 
    (if (!isSnapshot.value) {
        sonatypePublishToBundle.value
      } else {
        Some(Opts.resolver.sonatypeSnapshots)
    })
    ,
    ThisBuild / credentials += Credentials(file(".secrets/credentials.sonatype-nexus.properties")),
    ThisBuild / homepage := Some(url("https://izumi.7mind.io")),
    ThisBuild / licenses := Seq("BSD-style" -> url("http://www.opensource.org/licenses/bsd-license.php")),
    ThisBuild / developers := List(
              Developer(id = "7mind", name = "Septimal Mind", url = url("https://github.com/7mind"), email = "team@7mind.io"),
            ),
    ThisBuild / scmInfo := Some(ScmInfo(url("https://github.com/7mind/izumi"), "scm:git:https://github.com/7mind/izumi.git")),
    ThisBuild / scalacOptions += """-Xmacro-settings:scalatest-version=VExpr(V.scalatest)""",
    ThisBuild / scalacOptions += s"-Xmacro-settings:is-ci=${insideCI.value}"
  )
  .enablePlugins(IzumiPlugin)
  .disablePlugins(AssemblyPlugin)
  .aggregate(
    `idealingua`
  )
