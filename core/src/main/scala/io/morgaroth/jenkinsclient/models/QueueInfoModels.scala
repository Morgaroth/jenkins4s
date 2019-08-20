package io.morgaroth.jenkinsclient.models

case class JenkinsQueueItemInfo(
                                 _class: String,
                                 actions: Vector[BuildAction],
                                 blocked: Boolean,
                                 buildable: Boolean,
                                 id: Long,
                                 inQueueSince: Long,
                                 params: String,
                                 stuck: Boolean,
                                 task: JenkinsQueueTaskInfo,
                                 url: String,
                                 why: Option[String],
                                 cancelled: Option[Boolean],
                                 timestamp: Option[Long],
                                 executable: Option[JenkinsBuildRef],
                               )

case class JenkinsQueueTaskInfo(
                                 _class: String,
                                 name: String,
                                 url: String,
                                 color: String,
                               )

case class JenkinsGlobalQueue(
                               _class: String,
                               discoverableItems: Vector[String],
                               items: Vector[JenkinsQueueItemInfo]
                             )