# RoutinePatchouli2JEI

连接 Patchouli 手册与 JEI：在 Patchouli 书页中可以通过 JEI 快捷键查询配方/用途或切换书签；在 JEI 的物品配方/用途结果中，也会出现对应的 Patchouli 条目分类。

在 Patchouli 书页将鼠标悬停到物品上：按 JEI 的配方、用途或书签键即可执行相应操作。JEI 配方/用途页里若出现“Patchouli”分类，会列出所有在 Spotlight、配方页或可识别正文提及该物品的手册章节；每条都可直接跳到对应章节。正文匹配使用完整注册 ID、无歧义的注册路径或当前语言的完整物品名称。

## 环境

本项目以 [Mozilla Public License 2.0](LICENSE) 发布。

- Minecraft 26.1.2、NeoForge 26.1.2.112、Java 25
- Patchouli 26.1-94
- JEI 29.53.0.115
- 仅需安装在客户端；Patchouli 和 JEI 为必需前置。

Patchouli JAR 默认从 `../moUnderSea/.gradle/local-mods/patchouli-neoforge-26.1-94.jar` 读取。可通过 `-Ppatchouli_jar=<JAR>` 指定其他本地路径。

## 开发

```powershell
.\gradlew.bat build
.\gradlew.bat runClient
```

带测试手册启动客户端：

```powershell
.\gradlew.bat runClient -PtestBook=true
```

测试手册仅在该开发运行中加入，可在创造模式的“工具与实用物品”栏找到，名称为“JEI Test Guide”，使用本模组定义的封面图标。进入世界后打开手册：搜索 Diamond 应出现聚光章节和纯正文章节；Oak Planks 应出现配方章节；Iron Ingot、Redstone Dust、Torch、Bread 等名称可检查普通正文匹配。JEI 条目右上角的书本按钮可跳到对应章节。测试手册内容会从发布 JAR 中排除。
