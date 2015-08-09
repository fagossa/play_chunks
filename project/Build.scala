import _root_.sbt.ProjectReference
import com.typesafe.sbt.packager.archetypes.JavaAppPackaging
import com.typesafe.sbt.packager.docker.DockerPlugin
import sbt.Keys._
import sbt._

object Dependencies {

    object Version {
        val play_json = "2.3.4"

        val swagger = "1.3.12"
    }

    object Compile {
        val play_json = "com.typesafe.play" %% "play-json" % Version.play_json withSources()

        val swagger_play2 = Seq(
            "com.wordnik" %% "swagger-play2" % Version.swagger,
            "com.wordnik" %% "swagger-play2-utils" % Version.swagger
        )

        lazy val modules = Seq(play_json, swagger_play2)
    }

    object Test {

    }
}

object ProjectBuild extends Build {

    lazy val root = Project(
        id="metric",
        base = file(".")
        //settings = deploymentSetting()//,
        //dependencies = Dependencies.Compile.modules
    )

}
