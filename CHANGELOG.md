# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.3.9]
### Fixed
- Игра теперь распознает mcmod.info example-мода

## [0.3.8]
### Fixed
- Фикс бага [#12](https://github.com/RareScrap/MinecraftTabInventory/issues/12)

## [0.3.7]
### Added
- Новые утильные методы findIn(...) для поиска внутри TabInventory:

## [0.3.6]
### Fixed
- Фикс бага: клиентские классы пытались загружаться на сервере при регистрации сообщений

## [0.3.5]
### Added
- Добавлены клиентские евенты с помощью которых можно реагировать на принятие сообщений `TabInventoryItemsMessage` и `SetTabSlotMessage`.

## [0.3.1]
### Added
- В bintray публикацию добавлен sources пакет

### Fixed
- Фикс бага [#6](https://github.com/RareScrap/MinecraftTabInventory/issues/6)

## [0.3.0] - 2018-10-13
### Changed
- Клиент теперь не запрашивает пакет с сервера при каждом переключении вкладки [#2](https://github.com/RareScrap/MinecraftTabInventory/issues/2)

// TODO
[Unreleased]: https://github.com/olivierlacan/keep-a-changelog/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/olivierlacan/keep-a-changelog/compare/v0.3.0...v1.0.0
