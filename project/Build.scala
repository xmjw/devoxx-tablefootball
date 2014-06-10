import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "football"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      // Add your project dependencies here,
      "postgresql" % "postgresql" % "9.1-901.jdbc4",
      "com.twilio.sdk" % "twilio-java-sdk" % "3.4.5",
      "org.ow2.spec.ee" % "ow2-validation-1.1-spec" % "1.0.0-M1"
    )
    val main = PlayProject(appName, appVersion, appDependencies, mainLang = JAVA).settings(
      // Add your own project settings here      
    )

}
