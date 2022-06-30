package com.moop.gamerguides.helper

import java.util.regex.Matcher
import java.util.regex.Pattern

object YouTubeUtil {
    const val youTubeKey = "AIzaSyC9fNMyFwG5cRy2d45c6zhxfuncKAWjFTM"

    fun getVideoId(videoUrl: String): String {
        var videoId = ""
        val regex =
            "http(?:s)?:\\/\\/(?:m.)?(?:www\\.)?youtu(?:\\.be\\/|be\\.com\\/(?:watch\\?(?:feature=youtu.be\\&)?v=|v\\/|embed\\/|user\\/(?:[\\w#]+\\/)+))([^&#?\\n]+)"
        val pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE)
        val matcher: Matcher = pattern.matcher(videoUrl)
        if (matcher.find()) {
            videoId = matcher.group(1)!!
        }
        return videoId
    }
}