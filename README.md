# MinecraftTabInventory 1.7.10

Библиотека для Minecraft 1.7.10, позволяющая вам создавать инвентари с переключаемым содержимым.


## Скриншоты

TODO

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
    
    ./gradlew build

Если вы используете Maven или Ivy, то подходящий конфиг зависимости вы можете найти в <a href="https://bintray.com/rarescrap/minecraft/minecraft-tab-inventory">bintray-репозитории</a>.

## Остались вопросы?
Посетите раздел с документацией (TODO: link)
