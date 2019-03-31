package io.morgaroth.jenkinsclient

import java.util.Base64

import com.typesafe.config.Config

case class JenkinsConfig(address: String, login: String, token: String) {
  assert(login.nonEmpty && token.nonEmpty, "Jenkins credentials empty!")

  def getBasicAuthHeaderValue = s"Basic ${Base64.getEncoder.encodeToString(s"$login:$token".getBytes("utf-8"))}"
}

object JenkinsConfig {
  def fromConfig(config: Config) = new JenkinsConfig(
    config.getString("address"),
    config.getString("login"),
    config.getString("token"),
  )
}
