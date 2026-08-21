# AGENTS.md

This file provides guidance to Codex (Codex.ai/code) when working with code in this repository.

## Project
- 原项目地址 `/Users/Admin/VayPh/Philippine`
- 原项目分支地址 `/Users/Admin/vayTest/Vietnam`
- 重构当前项目 ui对标原项目，尽量重构的跟之前项目不一样但是逻辑不能改变，不能影响之前的功能
- 能优化的优化，新建的view 或者页面 尽量吧注释中文改成英文的
**PaPaVay** — an Android cash-loan / lending app for the Vietnamese market (VND, default locale `vi`). Single-module Gradle project, Kotlin + View Binding (no Compose), MVVM. Code comments are in Chinese.

- Gradle namespace / source package: `com.vayflex.cash`
- `applicationId`: `com.pp.vay.vnavi`
- `minSdk` 23, `targetSdk`/`compileSdk` 36, JVM target 11

## Build & Run

Uses the Gradle wrapper. Build variants combine a **flavor** (`dev`/`prod`, `environment` dimension) with a **build type** (`debug`/`release`):

```bash
./gradlew assembleDevDebug        # dev flavor, debug build
./gradlew assembleProdRelease     # prod flavor, release build (minified + shrunk)
./gradlew installDevDebug         # build + install on a connected device
./gradlew lintDevDebug            # Android lint
```