package paulokat.de.docspellviewer

/**
 * Third-party software notices for the Android app (FOSS attribution).
 * Structure modeled after common open-source attribution pages (e.g. Dropbox Android).
 */
data class ThirdPartyNotice(
    val name: String,
    val body: String
)

object ThirdPartyNotices {
    const val PAGE_TITLE = "Android App Third-Party Software Notices"

    private val APACHE_2_0_NOTICE = """
Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at:

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
""".trimIndent()

    val introduction: String = """
This document contains licensing information relating to the use of free and open-source software ("FOSS") with or within the ${AppInfo.NAME} Android app. Any terms, conditions, and restrictions governing the use or distribution of FOSS that are not contained within the license(s) governing use and distribution of the FOSS (the "FOSS Licenses") are offered and imposed by the app distributor alone. The authors, licensors, and distributors of the FOSS have disclaimed all warranties relating to any liability arising from the use and distribution of the FOSS.

Certain FOSS Licenses, such as the GNU Affero General Public License, require that corresponding source code for distributed FOSS binaries be made available under the same license. Recipients of ${AppInfo.NAME} who would like to receive a copy of such source code may request it via the contact information provided in the app repository or project documentation. Please identify: the FOSS package(s) requested; the application name and version number with which the FOSS is distributed; and a means to deliver the requested source code.
""".trim()

    val entries: List<ThirdPartyNotice> = listOf(
        ThirdPartyNotice(
            name = "Android Open Source Project",
            body = """
Copyright (c) The Android Open Source Project

$APACHE_2_0_NOTICE
""".trim()
        ),
        ThirdPartyNotice(
            name = "AndroidX (Core, Activity, Lifecycle, Navigation, Compose, Material3, Security Crypto)",
            body = """
Copyright (c) The Android Open Source Project

$APACHE_2_0_NOTICE
""".trim()
        ),
        ThirdPartyNotice(
            name = "Kotlin / kotlinx-coroutines",
            body = """
Copyright (c) JetBrains

$APACHE_2_0_NOTICE
""".trim()
        ),
        ThirdPartyNotice(
            name = "Retrofit",
            body = """
Copyright (c) Square, Inc.

$APACHE_2_0_NOTICE
""".trim()
        ),
        ThirdPartyNotice(
            name = "OkHttp",
            body = """
Copyright (c) Square, Inc.

$APACHE_2_0_NOTICE
""".trim()
        ),
        ThirdPartyNotice(
            name = "Moshi",
            body = """
Copyright (c) Square, Inc.

$APACHE_2_0_NOTICE
""".trim()
        ),
        ThirdPartyNotice(
            name = "Coil",
            body = """
Copyright (c) Coil Contributors

$APACHE_2_0_NOTICE
""".trim()
        ),
        ThirdPartyNotice(
            name = "AndroidSVG",
            body = """
Copyright (c) Paul LeBeau

$APACHE_2_0_NOTICE
""".trim()
        ),
        ThirdPartyNotice(
            name = "Docspell artwork (launcher icons)",
            body = """
Copyright (c) Docspell contributors (eikek/docspell project)

The launcher icons are derived from artwork in the Docspell project. Docspell is licensed under the GNU Affero General Public License v3 or later. You may obtain a copy of the license at:

https://www.gnu.org/licenses/agpl-3.0.html

Source artwork: https://github.com/eikek/docspell/tree/master/artwork
""".trim()
        )
    )
}
