// DO NOT EDIT THIS FILE
// IT IS AUTOGENERATED BY `sbtgen.sc` SCRIPT
// ALL CHANGES WILL BE LOST
// https://www.scala-js.org/
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.13.0")

// https://github.com/portable-scala/sbt-crossproject
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.3.1")

// https://scalacenter.github.io/scalajs-bundler/
addSbtPlugin("ch.epfl.scala" % "sbt-scalajs-bundler" % "0.21.1")

// https://github.com/scala-js/jsdependencies
addSbtPlugin("org.scala-js" % "sbt-jsdependencies" % "1.0.2")

////////////////////////////////////////////////////////////////////////////////

addSbtPlugin("io.7mind.izumi.sbt" % "sbt-izumi" % "0.0.99")

addSbtPlugin("com.github.sbt" % "sbt-pgp" % PV.sbt_pgp)

addSbtPlugin("org.scoverage" % "sbt-scoverage" % PV.sbt_scoverage)

addSbtPlugin("io.7mind.izumi" % "sbt-izumi-deps" % PV.izumi)

// Ignore scala-xml version conflict between scoverage where `coursier` requires scala-xml v2
// and scoverage requires scala-xml v1 on Scala 2.12,
// introduced when updating scoverage from 1.9.3 to 2.0.5
libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
