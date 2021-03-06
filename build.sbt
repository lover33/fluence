import SbtCommons._
import sbt.Keys._
import sbt._

import scala.sys.process._

name := "fluence"

commons

initialize := {
  val _ = initialize.value // run the previous initialization
  val required = "1.8" // counter.wast cannot be run under Java 9. Remove this check after fixes.
  val current = sys.props("java.specification.version")
  assert(current == required, s"Unsupported JDK: java.specification.version $current != $required")
}

/* Projects */

lazy val vm = (project in file("vm"))
  .settings(
    commons,
    libraryDependencies ++= Seq(
      "com.github.cretz.asmble" % "asmble-compiler" % "0.4.2-fl",
      cats,
      catsEffect,
      pureConfig,
      cryptoHashing,
      scalaTest,
      mockito
    )
  )
  .enablePlugins(AutomateHeaderPlugin)

lazy val `vm-counter` = (project in file("vm/examples/counter"))
  .settings(
    commons,
    // we have to build fat jar because is not possible to simply run [[CounterRunner]]
    // with sbt (like sbt vm-counter/run) because sbt uses custom ClassLoader.
    // 'Asmble' code required for loading some classes (like RuntimeHelpers)
    // only with system ClassLoader.
    assemblyJarName in assembly := "counter.jar",
    assemblyMergeStrategy in assembly := {
      // a module definition fails compilation for java 8, just skip it
      case PathList("module-info.class", xs @ _*) => MergeStrategy.first
      case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
    },
    // override `run` task
    run := {
      val log = streams.value.log
      log.info("Compiling counter.rs to counter.wasm and running with Fluence.")

      val scalaVer = scalaVersion.value.slice(0, scalaVersion.value.lastIndexOf("."))
      val projectRoot = file("").getAbsolutePath
      val cmd = s"sh vm/examples/run_example.sh counter $projectRoot $scalaVer"

      log.info(s"Running $cmd")

      assert(cmd ! log == 0, "Compile Rust to Wasm failed.")
    }
  )
  .dependsOn(vm)
  .enablePlugins(AutomateHeaderPlugin)

lazy val `vm-llamadb` = (project in file("vm/examples/llamadb"))
  .settings(
    commons,
    assemblyJarName in assembly := "llamadb.jar",
    assemblyMergeStrategy in assembly := {
      // a module definition fails compilation for java 8, just skip it
      case PathList("module-info.class", xs @ _*) => MergeStrategy.first
      case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
    },
    // override `run` task
    run := {
      val log = streams.value.log
      log.info("Compiling llamadb.rs to llama_db.wasm and running with Fluence.")

      val scalaVer = scalaVersion.value.slice(0, scalaVersion.value.lastIndexOf("."))
      val projectRoot = file("").getAbsolutePath
      val cmd = s"sh vm/examples/run_example.sh llama_db $projectRoot $scalaVer"

      log.info(s"Running $cmd")

      assert(cmd ! log == 0, "Compile Rust to Wasm failed.")
    }
  )
  .dependsOn(vm)
  .enablePlugins(AutomateHeaderPlugin)

lazy val statemachine = (project in file("statemachine"))
  .settings(
    commons,
    libraryDependencies ++= Seq(
      cats,
      catsEffect,
      circeGeneric,
      circeParser,
      pureConfig,
      cryptoHashing,
      slogging,
      scodecBits,
      prometheusClient,
      prometheusClientJetty,
      prometheusClientServlet,
      // Despite tmVersion is updated to 0.25.0, jtendermint:0.24.0 is the latest available and compatible with it.
      "com.github.jtendermint" % "jabci"          % "0.24.0",
      "org.bouncycastle"       % "bcpkix-jdk15on" % "1.56",
      "net.i2p.crypto"         % "eddsa"          % "0.3.0",
      scalaTest
    ),
    assemblyJarName in assembly := "statemachine.jar",
    assemblyMergeStrategy in assembly := {
      // a module definition fails compilation for java 8, just skip it
      case PathList("module-info.class", xs @ _*) => MergeStrategy.first
      case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
    },
    test in assembly := {},
    imageNames in docker := Seq(ImageName("fluencelabs/solver")),
    dockerfile in docker := {
      // Run `sbt docker` to create image

      // The assembly task generates a fat JAR file
      val artifact = assembly.value
      val artifactTargetPath = s"/${artifact.name}"

      // Tendermint constants
      val tmVersion = "0.25.0"
      val tmDataRoot = "/tendermint"
      val tmBinaryArchive = s"tendermint_${tmVersion}_linux_amd64.zip"
      val tmBinaryUrl = s"https://github.com/tendermint/tendermint/releases/download/v$tmVersion/$tmBinaryArchive"
      val tmP2pPort = 26656
      val tmRpcPort = 26657
      val tmPrometheusPort = 26660

      // State machine constants
      val solverDataRoot = "/solver"
      val solverRunScript = s"$solverDataRoot/run.sh"
      val stateMachinePrometheusPort = 26661

      val vmDataRoot = "/vmcode"

      new Dockerfile {
        from("openjdk:8-jre-alpine")
        // TODO: merge all these `run`s into a single run
        runRaw(s"wget $tmBinaryUrl && unzip -d /bin $tmBinaryArchive && rm $tmBinaryArchive")

        expose(tmP2pPort)
        expose(tmRpcPort)
        expose(tmPrometheusPort)
        expose(stateMachinePrometheusPort)

        volume(tmDataRoot)
        volume(solverDataRoot)
        volume(vmDataRoot)

        // includes solver run script and default configs in the image
        copy(baseDirectory.value / "docker" / "solver", solverDataRoot)

        copy(artifact, artifactTargetPath)

        entryPoint("sh", solverRunScript, artifactTargetPath)
      }
    }
  )
  .enablePlugins(AutomateHeaderPlugin)
  .enablePlugins(DockerPlugin)
  .dependsOn(vm)

lazy val externalstorage = (project in file("externalstorage"))
  .settings(
    commons,
    libraryDependencies ++= Seq(
      cats,
      catsEffect,
      sttp,
      sttpCirce,
      sttpCatsBackend,
      slogging,
      circeCore,
      circeGeneric,
      circeGenericExtras,
      pureConfig,
      scodecBits,
      scodecCore,
      web3jCrypto,
      cryptoHashing,
      scalaTest
    )
  )
  .enablePlugins(AutomateHeaderPlugin)

lazy val ethclient = (project in file("ethclient"))
  .settings(
    commons,
    libraryDependencies ++= Seq(
      web3jCore,
      slogging,
      scodecBits,
      cats,
      catsEffect,
      fs2,
      scalaTest
    ),
  )
  .enablePlugins(AutomateHeaderPlugin)

lazy val node = project
  .settings(
    commons,
    kindProjector,
    libraryDependencies ++= Seq(
      catsEffect,
      sttp,
      sttpCatsBackend,
      fs2io,
      pureConfig,
      circeGeneric,
      circeParser,
      http4sDsl,
      http4sServer,
      scalaTest
    ),
    assemblyMergeStrategy in assembly := {
      // a module definition fails compilation for java 8, just skip it
      case PathList("module-info.class", xs @ _*) => MergeStrategy.first
      case "META-INF/io.netty.versions.properties" =>
        MergeStrategy.first
      case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
    },
    test in Test := {
      docker.value
      (docker in statemachine).value
      (test in Test).value
    },
    mainClass in assembly := Some("fluence.node.MasterNodeApp"),
    assemblyJarName in assembly := "master-node.jar",
    test in assembly := {},
    imageNames in docker := Seq(ImageName("fluencelabs/node")),
    dockerfile in docker := {
      // The assembly task generates a fat JAR file
      val artifact: File = assembly.value
      val artifactTargetPath = s"/${artifact.name}"

      new Dockerfile {
        // docker is needed in image so it can connect to host's docker.sock and run solvers on host
        val dockerBinary = "https://download.docker.com/linux/static/stable/x86_64/docker-18.06.1-ce.tgz"
        from("openjdk:8-jre-alpine")
        runRaw(s"wget -q $dockerBinary -O- | tar -C /usr/bin/ -zxv docker/docker --strip-components=1")

        volume("/master") // anonymous volume to store all data

        /*
        * The following directory structure is assumed in node/src/main/resources:
        *    docker/
        *      tendermint/config/default_config.toml
        *      vmcode/vmcode-llamadb/llama_db.wasm
        *    entrypoint.sh
        */
        copy((resourceDirectory in Compile).value / "docker", "/master/")

        copy(artifact, artifactTargetPath)

        cmd("java", "-jar", artifactTargetPath)
        entryPoint("sh", "/master/entrypoint.sh")
      }
    }
  )
  .enablePlugins(AutomateHeaderPlugin, DockerPlugin)
  .dependsOn(ethclient, externalstorage)
