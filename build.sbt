import play.Project._

name := "fonolitics"

version := "1.0"

libraryDependencies ++= Seq(
							jdbc, anorm, "mysql" % "mysql-connector-java" % "5.1.25",
							"commons-pool"      %  "commons-pool"            % "1.6"	
							)

playScalaSettings