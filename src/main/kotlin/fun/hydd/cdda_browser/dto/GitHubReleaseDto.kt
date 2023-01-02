package `fun`.hydd.cdda_browser.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
data class GitHubReleaseDto(
  @JsonProperty("name") val name: String,
  @JsonProperty("tag_name") val tagName: String,
  @JsonProperty("target_commitish") val commitHash: String,
  @JsonProperty("prerelease") val isExperiment: Boolean,
  @JsonProperty("created_at") val date: Date
)
