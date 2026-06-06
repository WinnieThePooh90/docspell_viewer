package paulokat.de.docspellviewer

/**
 * Open-source license and attribution notices shown in Settings → Licenses.
 */
data class ThirdPartyNotice(
    val name: String,
    val body: String
)

object ThirdPartyNotices {
    private val APACHE_2_0_NOTICE = """
Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at:

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
""".trimIndent()

    val appLicense: ThirdPartyNotice = ThirdPartyNotice(
        name = "${AppInfo.NAME} (application source code)",
        body = """
Copyright ${AppInfo.COPYRIGHT_YEAR} ${AppInfo.COPYRIGHT_HOLDER}

The source code of this Android application is licensed under the Apache License, Version 2.0 (${AppInfo.LICENSE_SPDX}).

Source repository: ${AppInfo.SOURCE_REPOSITORY}
Full license text: ${AppInfo.SOURCE_REPOSITORY}/blob/main/LICENSE

$APACHE_2_0_NOTICE
""".trim()
    )

    val introduction: String = """
The ${AppInfo.NAME} application source code is licensed under the Apache License 2.0 (see above).

This document also lists third-party open-source software ("FOSS") used in the app, and artwork under separate licenses. Any terms not contained in the respective FOSS licenses are offered by the app distributor alone.

Certain FOSS licenses, such as the GNU Affero General Public License, require that corresponding source code for distributed components be made available under the same license. Recipients who would like a copy of such source code may request it via ${AppInfo.SOURCE_REPOSITORY} or the project issue tracker. Please identify the FOSS package(s) requested, the application name and version, and a delivery address.
""".trim()

    val thirdPartyEntries: List<ThirdPartyNotice> = listOf(
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
