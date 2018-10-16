# MinecraftTabInventory 1.7.10

[ ![Download](https://api.bintray.com/packages/rarescrap/minecraft/minecraft-tab-inventory/images/download.svg) ](https://bintray.com/rarescrap/minecraft/minecraft-tab-inventory/_latestVersion)

Библиотека для Minecraft 1.7.10, позволяющая вам создавать инвентари с переключаемым содержимым.


## Скриншоты

![Alt Text](https://github.com/RareScrap/MinecraftTabInventory/blob/master/github_media/screenshot2.gif)
![Alt Text](https://github.com/RareScrap/MinecraftTabInventory/blob/master/github_media/screenshot1.gif)

## Быстрый старт

1. Откройте build.gradle и добавьте репозиторий с библиотекой к блоку repositories :
``` gradle
repositories {
    maven {
        url = "https://dl.bintray.com/rarescrap/minecraft"
    }
}
```

2. После добавьте библиотеку в блок dependencies:
### Gradle < 3.0
``` gradle
dependencies {
    compile 'ru.rarescrap.tabinventory:minecraft-tab-inventory-lib:0.2.1_1.7.10'
}
```

### Gradle 3.0 and newer
// TODO

3. Синхронизируйте проект и вперед!

```
./gradlew build
```

4. Пример использования библиотеки вы может найти в <a href="https://github.com/RareScrap/MinecraftTabInventory/tree/master/src/main">демонстрационном моде</a>. Так же вы можете посетить <a href="https://github.com/RareScrap/MinecraftTabInventory/wiki">раздел с документацией</a>.

Если вы используете Maven или Ivy, то подходящий конфиг зависимости вы можете найти в <a href="https://bintray.com/rarescrap/minecraft/minecraft-tab-inventory">bintray-репозитории</a>.

## Остались вопросы?
Задайте его в <a href="https://github.com/RareScrap/MinecraftTabInventory/issues">issues</a>. Я отвечу как только смогу :)
