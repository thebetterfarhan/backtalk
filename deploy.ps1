#Requires -Version 7
param(
	[string]$Device = $env:ANDROID_SERIAL,
	[switch]$SkipBuild
)
$ErrorActionPreference = 'Stop'
$root = $PSScriptRoot
$adbArgs = if ($Device) { @('-s', $Device) } else { @() }
$versionCodeOverride = $null
if ($Device) {
	$installedVersionCode = 0L
	$packageInfo = adb @adbArgs shell dumpsys package fyi.quin.backtalk 2>$null
	$versionMatch = $packageInfo | Select-String 'versionCode=(\d+)'
	if ($versionMatch -and [int64]::TryParse($versionMatch.Matches[0].Groups[1].Value, [ref]$installedVersionCode)) {
		$versionCodeOverride = $installedVersionCode + 1
	}
}
if (-not (Test-Path "$root\local.properties")) {
	$sdk = $env:ANDROID_HOME ?? $env:ANDROID_SDK_ROOT ?? $env:ANDROID_SDK
	if (-not $sdk) { throw 'Set ANDROID_HOME to your Android SDK path.' }
	"sdk.dir=$($sdk -replace '\\', '/')" | Set-Content "$root\local.properties"
}
if (-not $SkipBuild) {
	$gradleArgs = @('assemblePhoneDebug', '--console=plain')
	if ($versionCodeOverride) {
		$gradleArgs += "-PbacktalkVersionCodeOverride=$versionCodeOverride"
	}
	& "$root\gradlew.bat" @gradleArgs
	if ($LASTEXITCODE) { exit $LASTEXITCODE }
}
$apk = Get-ChildItem "$root\build\outputs\apk\phone\debug\*.apk" | Sort-Object LastWriteTime | Select-Object -Last 1
if (-not $apk) { throw 'No APK found.' }
Write-Host "Installing $($apk.Name)"
adb @adbArgs install -r -d $apk.FullName
if ($LASTEXITCODE) { exit $LASTEXITCODE }
