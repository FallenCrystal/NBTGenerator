# NBTGenerator

基于对
[数据生成器](https://zh.minecraft.wiki/w/Tutorial:%E8%BF%90%E8%A1%8C%E6%95%B0%E6%8D%AE%E7%94%9F%E6%88%90%E5%99%A8?variant=zh-cn)
的生成结果处理为 [注册表](https://zh.minecraft.wiki/w/%E6%B3%A8%E5%86%8C%E8%A1%A8?variant=zh-cn)
的 [NBT](https://zh.minecraft.wiki/w/NBT%E6%A0%BC%E5%BC%8F?variant=zh-cn) 的简易转换器

## 它有什么用?

通过收集数据生成器的结果并将其转换为NBT. 
使其可以非常容易地集成在其它需要Minecraft数据的外部应用程序中

默认情况下, 该应用程序将生成一个最小化 (可配置) 的用于发送给客户端的注册表.
在没有对于注册表的巨大变更的情况下, 它可以快速地生成针对快照版本或较新版本的注册表. 
帮助开发人员摆脱因为注册表问题而无法让客户端加入服务器. 或因注册表缺失内容造成的崩溃

仅支持为 1.18+ 的 Minecraft 生成数据. 尝试生成低于该版本的内容将可能导致出错或不可读.

## 注册表缺失了很多内容?

在没有调整参数的默认情况下, 将会对注册表进行最小化处理.  
如果要生成一份不经过最小化的注册表 请在启动时在后面添加 `--cleaner NONE` 作为参数.


更多参数用法可通过仅添加 `-h` 参数来获取帮助.


## 无法读取\<source>为文件夹?

这可能是因为您没有生成, 生成结果不正确或路径指向非文件夹导致的.

要获得可以被正确识别的数据生成器的输出, 请按照以下步骤生成:

  - 打开 Minecraft Launcher, 并等待它加载完毕
  - 点击配置
  - 点击创建新配置
  - 选择您想要的Minecraft版本
  - 点击版本选项框右上角的 "服务器" 下载按钮
  - 等待浏览器下载完毕后 将 `server.jar` 移动到和该应用程序的相同目录下
  - 新建 `generate.bat` 或 `generate.sh` 
  - 填入 `java -DbundlerMainClass=net.minecraft.data.Main -jar server.jar --all`
  - 双击运行 等待完成
  - 运行 `NBTGenerator`. 如果没有问题 您将得到一个 `output.nbt`

至少需要 Java 11 才可以运行该应用程序
