# 🎵 音乐星球 (MusicPlayer) — 安卓应用开发完整总结

> **开发日期：** 2026年6月20日 — 21日  
> **项目路径：** `D:\claude\安卓应用开发\MusicPlayer\`  
> **GitHub 仓库：** https://github.com/wulinxi/MusicPlayer  
> **APK 文件：** `MusicPlayer/app/build/outputs/apk/debug/app-debug.apk` (约 51MB)

---

## 📋 目录

1. [项目概述](#1-项目概述)
2. [技术栈与架构](#2-技术栈与架构)
3. [模块详解](#3-模块详解)
4. [界面与功能](#4-界面与功能)
5. [数据库设计](#5-数据库设计)
6. [关键功能实现](#6-关键功能实现)
7. [资源与素材](#7-资源与素材)
8. [开发修改记录](#8-开发修改记录)
9. [文件统计](#9-文件统计)

---

## 1. 项目概述

**音乐星球** 是一款功能完整的 Android 音乐播放器应用。用户可以进行账号注册/登录、浏览发现音乐、播放本地及在线歌曲、收藏歌曲、发表评论、查看个人中心等操作。

### 核心功能一览

| 功能模块 | 说明 |
|----------|------|
| 🔐 用户系统 | 账号注册、密码登录、QQ模拟授权登录 |
| 🎧 音乐播放 | 本地MP3播放 + 在线流媒体播放，封面CD旋转动画 |
| 🔍 在线搜索 | Retrofit 接入音乐API搜索，离线回退本地模拟数据 |
| ❤️ 收藏系统 | 歌曲收藏/取消收藏，收藏列表展示 |
| 💬 评论系统 | 歌曲评论发表、评论列表展示 |
| 📊 个人中心 | 用户信息、收藏、播放记录、播放次数统计 |
| 🔔 后台播放 | ExoPlayer + Foreground Service + 通知栏控制 |
| 🎤 歌词同步 | 4首预置歌曲实时歌词滚动显示 |

---

## 2. 技术栈与架构

### 架构模式：MVVM (Model-View-ViewModel)

```
┌─────────────────────────────────────────┐
│               Activity / Fragment        │  ← View 层
│         (UI 展示 + 用户交互)              │
├─────────────────────────────────────────┤
│              ViewModel                   │  ← ViewModel 层
│    (LiveData 通知 UI，业务逻辑处理)        │
├─────────────────────────────────────────┤
│            Room Database                 │  ← Model 层
│      (DAO → Entity → SQLite)             │
└─────────────────────────────────────────┘
```

### 技术栈

| 类别 | 技术 |
|------|------|
| **语言** | Java 8 |
| **最低SDK** | API 24 (Android 7.0) |
| **目标SDK** | API 36 (Android 15) |
| **构建工具** | Gradle 8.13.2 + AGP 8.13.2 |
| **UI框架** | Material Design (Material Components) |
| **数据库** | Room (SQLite ORM) |
| **网络请求** | Retrofit 2.9.0 + OkHttp 4.12.0 |
| **JSON解析** | Gson 2.10.1 |
| **图片加载** | Glide 4.16.0 |
| **媒体播放** | ExoPlayer (Media3) 1.2.0 |
| **架构组件** | ViewModel + LiveData + ViewBinding |
| **列表组件** | RecyclerView + ListAdapter + DiffUtil |
| **轮播图** | ViewPager2 |
| **下拉刷新** | SwipeRefreshLayout |

---

## 3. 模块详解

### 📁 项目结构

```
MusicPlayer/
├── app/
│   ├── build.gradle                    # 应用级构建配置（依赖、SDK版本）
│   ├── proguard-rules.pro              # 混淆规则
│   └── src/main/
│       ├── AndroidManifest.xml         # 应用清单（权限、Activity、Service）
│       ├── assets/                     # 预置资源
│       │   ├── music/                  # 4首预置MP3歌曲
│       │   ├── covers/                 # 默认封面图
│       │   └── backgrounds/            # 主页面背景图
│       ├── java/com/example/musicplayer/
│       │   ├── MusicPlayerApp.java     # Application 入口
│       │   ├── activity/               # 6个Activity
│       │   ├── fragment/               # 3个Fragment
│       │   ├── viewmodel/              # 6个ViewModel
│       │   ├── model/                  # 5个数据实体
│       │   ├── database/               # Room数据库 + 5个DAO
│       │   ├── adapter/                # 4个RecyclerView适配器
│       │   ├── api/                    # Retrofit API + 在线搜索
│       │   ├── service/                # 后台播放Service
│       │   ├── receiver/               # 耳机拔出广播接收器
│       │   └── util/                   # 工具类（歌词、文件）
│       └── res/
│           ├── layout/                 # 13个布局文件
│           ├── drawable/               # 18个矢量图/形状资源
│           ├── menu/                   # 底部导航菜单
│           ├── values/                 # 颜色、字符串、主题
│           └── ...
├── build.gradle                        # 项目级构建配置
├── settings.gradle                     # 项目设置
└── local.properties                    # SDK路径配置
```

---

## 4. 界面与功能

### 4.1 启动页 (SplashActivity)
- **布局：** `activity_splash.xml`
- **功能：** App名称淡入动画 → 2秒后自动跳转登录页
- **动画：** TextView alpha 0→1，持续1秒

### 4.2 登录页 (LoginActivity)
- **布局：** `activity_login.xml`（渐变背景 `bg_gradient_login.xml`）
- **功能：**
  - 用户名 + 密码登录
  - QQ 模拟授权登录（弹窗输入QQ号+密码，首次自动注册）
  - 跳转注册页
  - 表单验证（空值检查、错误提示）
- **ViewModel：** `LoginViewModel` — 异步查询用户，枚举状态返回

### 4.3 注册页 (RegisterActivity)
- **布局：** `activity_register.xml`
- **功能：**
  - 用户名、昵称、密码、确认密码输入
  - 用户名重复检查
  - 两次密码一致性验证
  - 成功后返回登录页
- **ViewModel：** `RegisterViewModel` — 异步插入用户

### 4.4 主界面 (MainActivity)
- **布局：** `activity_main.xml`（FrameLayout + 背景图 + BottomNavigationView）
- **功能：**
  - 底部三Tab导航：发现音乐 / 我的音乐 / 个人中心
  - Fragment 切换
  - 背景图加载（Glide 加载本地 `111.jpg`）

#### Tab 1: 发现音乐 (DiscoverFragment)
- **布局：** `fragment_discover.xml`
- **功能：**
  - 搜索框（SearchView）→ 跳转歌单搜索页
  - 热门歌曲列表（按播放次数排序 Top 20）
- **ViewModel：** `DiscoverViewModel` — 提供 hotSongs + recentSongs

#### Tab 2: 我的音乐 (MusicFragment)
- **布局：** `fragment_music.xml`
- **功能：**
  - 全部歌曲列表
  - 最近播放列表
  - "查看全部"按钮 → 跳转歌单页
  - "在线搜索添加歌曲"按钮 → 跳转歌单页并自动弹出在线搜索
- **ViewModel：** `MusicViewModel` — 提供 allSongs + recentPlays

#### Tab 3: 个人中心 (ProfileFragment)
- **布局：** `fragment_profile.xml`
- **功能：**
  - 头像展示（Glide 圆形裁剪，支持QQ头像URL）
  - 昵称 + 个性签名展示
  - 收藏数 + 播放次数统计
  - 收藏列表 + 最近播放列表
  - 编辑资料弹窗（修改昵称）
  - 清除缓存
  - 退出登录（确认弹窗 → 返回登录页）
- **ViewModel：** `ProfileViewModel` — 提供 favorites + recentPlays + playCount

### 4.5 播放器页 (PlayerActivity) ⭐ 核心
- **布局：** `activity_player.xml`（暗色高级背景 `bg_player_premium.xml`）
- **功能：**
  - **CD封面旋转动画：** ObjectAnimator 20秒/圈，无限循环，播放时旋转/暂停时停止
  - **播放控制：** 播放/暂停、上一首/下一首、停止
  - **进度条：** SeekBar 拖拽控制 + 实时时间更新（每500ms轮询）
  - **歌词同步：** 根据播放进度实时匹配歌词行
  - **收藏切换：** 一键收藏/取消收藏
  - **分享功能：** Intent 分享歌曲信息到其他应用
  - **歌单跳转：** 跳转到歌单列表页
  - **封面点击 → 歌曲详情页：** 跳转 SongDetailActivity
  - **后台 Service 绑定：** bindService → MusicService
- **ViewModel：** 直接使用 AppDatabase（同步查询）
- **播放记录：** 每次切歌自动记录

### 4.6 歌曲详情页 (SongDetailActivity)
- **布局：** `activity_song_detail.xml`
- **功能：**
  - 歌曲信息展示（标题、歌手、专辑、播放次数）
  - 收藏切换
  - 分享功能
  - 发表评论（输入框 + 发送按钮）
  - 评论列表展示
  - 相似歌曲推荐（同歌手，排除当前歌曲）
- **ViewModel：** `SongDetailViewModel` — 提供 similarSongs + comments + commentCount

### 4.7 歌单列表页 (SongListActivity)
- **布局：** `activity_song_list.xml`
- **功能：**
  - 全部歌曲展示
  - 本地搜索（SearchView 实时过滤）
  - FAB 按钮 → 在线搜索弹窗
  - **在线搜索弹窗 (`dialog_online_search.xml`)：**
    - 输入关键词 → Retrofit API 搜索
    - API 成功 → 显示真实结果
    - API 失败 → 回退本地模拟数据 (`OnlineSearchHelper`)
    - 点击添加按钮 → 插入数据库 + 刷新列表
  - **手动添加弹窗 (`dialog_add_song.xml`)：**
    - 输入歌名、歌手、专辑、远程URL
- **ViewModel：** `SongListViewModel` — 提供歌曲列表 + 搜索功能

---

## 5. 数据库设计

### Room Database (AppDatabase)
- **数据库名：** `music_player.db`
- **版本：** 1
- **单例模式：** 双重检查锁

### 5张数据表

| 表名 | 实体类 | 说明 |
|------|--------|------|
| `users` | User | 用户信息（username唯一索引） |
| `songs` | Song | 歌曲信息（本地路径 + 远程URL + 封面） |
| `favorites` | Favorite | 收藏关联（user_id + song_id 联合唯一索引） |
| `play_records` | PlayRecord | 播放记录 |
| `comments` | Comment | 歌曲评论 |

### 5个DAO接口

| DAO | 主要方法 |
|-----|---------|
| UserDao | findByUsername, findByIdSync, countByUsername, insert, update |
| SongDao | getAllSongs, searchSongs, getHotSongs, getFavoriteSongs, getSimilarSongs, incrementPlayCount |
| FavoriteDao | insert, removeByUserAndSong, isFavoriteSync, getFavoriteCount |
| PlayRecordDao | insert, getRecentPlaySongs, getPlayCount |
| CommentDao | insert, getBySongId, getCommentCount |

### 外键关系

```
favorites  ──FK──> users (CASCADE)
           ──FK──> songs (CASCADE)

play_records ──FK──> users (CASCADE)
             ──FK──> songs (CASCADE)

comments ──FK──> users (CASCADE)
         ──FK──> songs (CASCADE)
```

---

## 6. 关键功能实现

### 6.1 后台音乐播放 (MusicService)
- **技术：** ExoPlayer (Media3) + Foreground Service
- **音频源查找策略（findMediaUri）：**
  1. 远程URL（http开头）→ 直接流媒体播放
  2. remoteUrl 字段检查
  3. 本地文件精确匹配（filesDir/music/文件名）
  4. 模糊搜索（music目录下匹配歌名的mp3文件）
- **前台通知：** NotificationCompat + MediaStyle，显示歌名/歌手
- **播放模式：** 列表循环（上一首/下一首循环切换）

### 6.2 CD封面旋转动画
```java
ObjectAnimator.ofFloat(ivCover, "rotation", 0f, 360f);
// 20秒一圈，无限循环，LinearInterpolator
// 播放时 start() / 暂停时 pause()
```

### 6.3 本地音乐自动同步
```
App启动 / 切换到「我的音乐」Tab → 扫描 filesDir/music/
    ├── 新文件 → 自动入库（文件名格式「歌手 - 歌名.mp3」）
    └── 已存在 → 跳过
```
- **实现：** `MusicFileHelper.syncMusicFolder()` — 公共静态方法
- **触发时机：** MusicPlayerApp.onCreate() + MusicFragment.onResume()
- **支持格式：** mp3 / flac / wav / ogg / aac

### 6.4 NCM 文件自动解密
- **实现：** `NcmDecoder.java` — 纯 Java 移植 ncmdump 解密算法
- **算法流程：** RC4密钥解密 → AES-128-ECB → XOR → RC4音乐数据解密
- **触发：** 文件选择器选择 .ncm 文件时自动识别、解密为 .mp3/.flac
- **附加功能：** 同时提取 NCM 内嵌专辑封面图片
- **无需外部工具：** 不再依赖 ncmdump.exe，Android 端原生解密

### 6.5 本地文件导入
- **📂 导入本地按钮：** 「我的音乐」顶部直接弹文件选择器 → 选 MP3/NCM
- **文件选择器：** ActivityResultContracts.OpenDocument，无需存储权限
- **NCM 处理：** 自动解密 + 自动提取封面
- **歌名解析：** 文件名 `歌手 - 歌名.xxx` 自动拆为演唱者和标题

### 6.6 在线搜索（本地离线库）
- **搜索库：** OnlineSearchHelper 内置 160+ 首歌曲（20+ 位歌手）
- **搜索策略：** 歌手名匹配 → 歌名模糊匹配 → 单字匹配
- **API 备用：** Retrofit 连接公网 Vercel NeteaseCloudMusicApi（真机可用）
- **无假音频：** 在线添加的歌曲不分配假 URL，改为提示用户选择本地文件

### 6.7 歌词同步
- **实现：** `LyricsHelper` — LinkedHashMap 存储时间(秒) → 歌词行
- **预置歌词：** 4首陈奕迅歌曲的完整歌词
- **实时匹配：** 播放进度回调 → getLyricsLine(title, seconds) → 查找对应行

### 6.8 资产文件管理 (MusicFileHelper)
- **首次启动：** 将 `assets/music/`、`assets/covers/`、`assets/backgrounds/` 复制到内部存储 `filesDir`
- **已存在跳过：** 避免重复复制
- **路径管理：** 提供统一的 getMusicDir / getCoversDir / getDefaultCoverPath 等方法

### 6.9 数据库初始化与自动清理
1. 创建通知渠道（CHANNEL_ID = "music_playback"）
2. 复制 assets 音乐文件到内部存储
3. 扫描本地音乐文件夹同步新歌
4. 清理无音源歌曲（deleteSongsWithoutAudio）
5. 检查并创建测试用户（admin / 123456 / 音乐爱好者）
6. 预置或修补歌曲数据（封面路径修正）

### 6.10 ViewModel 异步模式
- 所有数据库操作在 `Executors.newSingleThreadExecutor()` 中执行
- 结果通过 `LiveData.postValue()` / `MutableLiveData` 通知 UI
- 避免主线程阻塞

---

## 7. 资源与素材

### 歌曲文件
| 歌曲 | 歌手 | 来源 |
|------|------|------|
| 孤独患者 | 陈奕迅 | ncmdump 解密 |
| 最佳损友 | 陈奕迅 | ncmdump 解密 |
| 盲婚哑嫁 The Code | 陈奕迅 | ncmdump 解密 |
| 粤语残片 | 陈奕迅 | ncmdump 解密 |
| Amani | Beyond | ncmdump 解密 |
| 唯一 | G.E.M.邓紫棋 | ncmdump 解密 |
| 多远都要在一起 | G.E.M.邓紫棋 | ncmdump 解密 |
| 海阔天空 | G.E.M.邓紫棋 | ncmdump 解密 |

> 以上 8 首歌均已从 .ncm 解密为 .mp3，存放在 `music/` 目录及 App 内部存储。

### 图片资源
- `assets/covers/default_cover.jpg` — 默认歌曲封面
- `assets/backgrounds/111.jpg` — 主页面背景（185KB）
- `pic/640.jpg` — 备选图片（55KB）
- `pic/111.jpg` — 背景图副本（1.8MB）

### 矢量图标（drawable/）
18个 XML 矢量图标：播放、暂停、上一首、下一首、停止、收藏(空/实)、分享、歌单、返回箭头、个人、锁、发现、音符、个人中心、添加、QQ、搜索输入框背景等。

### 颜色主题 (colors.xml)
- **主色：** #1E88E5 (Material Blue)
- **强调色：** #FF6F00 (Orange)
- **播放器背景：** #1A1A2E (暗色)
- **收藏红：** #E53935

---

## 8. 开发修改记录

> 由于本项目并非 Git 仓库，以下记录基于当前代码与配置文件的逆向分析。标注 ✅ 表示已确认识别到的功能。

### 阶段一：项目初始化
1. ✅ 创建 Android 项目（Gradle 8.13.2，AGP 8.13.2，compileSdk 36）
2. ✅ 配置 build.gradle（所有依赖声明）
3. ✅ 创建 AndroidManifest.xml（权限、Activity、Service、Receiver 注册）
4. ✅ 配置主题（Material Design DayNight）

### 阶段二：数据库层
5. ✅ 创建 5 个 Entity 类（User, Song, Favorite, PlayRecord, Comment）
6. ✅ 创建 AppDatabase（Room，单例模式，Callback 预置数据）
7. ✅ 创建 5 个 DAO 接口（UserDao, SongDao, FavoriteDao, PlayRecordDao, CommentDao）

### 阶段三：工具类
8. ✅ 创建 MusicFileHelper（assets 复制、文件路径管理）
9. ✅ 创建 LyricsHelper（4首歌歌词时间轴）

### 阶段四：用户系统
10. ✅ 创建 SplashActivity（启动页 + 动画 + 延时跳转）
11. ✅ 创建 LoginActivity（用户名密码登录 + 表单验证）
12. ✅ 创建 LoginViewModel（异步登录 + QQ模拟登录）
13. ✅ 创建 RegisterActivity（注册页 + 表单验证）
14. ✅ 创建 RegisterViewModel（异步注册 + 用户名查重）
15. ✅ 创建 QQ 登录弹窗布局 `dialog_qq_login.xml`

### 阶段五：主框架
16. ✅ 创建 MainActivity（底部三Tab导航 + Fragment容器）
17. ✅ 创建 DiscoverFragment（发现页：搜索 + 热门歌曲）
18. ✅ 创建 DiscoverViewModel
19. ✅ 创建 MusicFragment（我的音乐：全部歌曲 + 最近播放）
20. ✅ 创建 MusicViewModel
21. ✅ 创建 ProfileFragment（个人中心：信息 + 收藏 + 播放记录）
22. ✅ 创建 ProfileViewModel

### 阶段六：音乐播放器 ⭐
23. ✅ 创建 MusicService（ExoPlayer + Foreground Service）
24. ✅ 创建 PlayerActivity（CD旋转动画 + 完整播放控制）
25. ✅ 实现智能音频源查找（远程URL → remoteUrl → 本地精确 → 模糊匹配）
26. ✅ 实现封面圆形裁剪 + Glide加载
27. ✅ 实现进度条实时更新 + 拖拽控制
28. ✅ 实现歌词同步显示
29. ✅ 实现收藏切换
30. ✅ 实现分享功能
31. ✅ 实现播放记录自动写入
32. ✅ 创建 HeadphoneReceiver（耳机拔出广播）
33. ✅ 创建 MusicPlayerApp（Application 入口 + 通知渠道 + 数据初始化）

### 阶段七：网络搜索
34. ✅ 创建 ApiClient（Retrofit 单例，Base URL: http://10.0.2.2:3000/）
35. ✅ 创建 MusicApiService（search, song/url, song/detail 端点）
36. ✅ 创建 SearchResponse（网易云API格式适配）
37. ✅ 创建 OnlineSearchHelper（30+ 首本地模拟歌曲数据 + Demo音源URL）
38. ✅ 创建 SearchResultAdapter
39. ✅ 创建在线搜索弹窗布局 `dialog_online_search.xml`
40. ✅ 实现 API优先 → 离线回退 策略

### 阶段八：歌单与歌曲详情
41. ✅ 创建 SongListActivity（歌单列表 + 界面内搜索 + FAB添加 + 手动添加弹窗）
42. ✅ 创建 SongListViewModel
43. ✅ 创建 SongDetailActivity（详情 + 评论 + 相似歌曲推荐）
44. ✅ 创建 SongDetailViewModel
45. ✅ 创建 CommentAdapter
46. ✅ 创建 SongItemAdapter（ListAdapter + DiffUtil）
47. ✅ 创建 BannerAdapter（ViewPager2 无限轮播）

### 阶段九：UI/UX 细节
48. ✅ 创建所有 layout 布局文件（共13个）
49. ✅ 创建所有 drawable 矢量图标（共18个）
50. ✅ 创建所有 shape drawable（圆形、渐变背景、搜索框背景等）
51. ✅ 配置 colors.xml、strings.xml、themes.xml
52. ✅ 创建底部导航菜单 `bottom_nav_menu.xml`
53. ✅ 错误处理：Fragment 加载时 try-catch 兜底
54. ✅ 数据库旧数据修补逻辑（封面路径 + 文件名修正）

### 阶段十：资源准备
55. ✅ 下载 ncmdump 工具（v1.6.1 win64）
56. ✅ 使用 ncmdump 解密 .ncm 文件 → 4首 .mp3
57. ✅ 准备背景图片（111.jpg）
58. ✅ 准备默认封面图（default_cover.jpg）
59. ✅ 将 mp3 文件放入 assets/music/
60. ✅ 将图片放入 assets/covers/ 和 assets/backgrounds/

### 阶段十一：构建与调试
61. ✅ 安装在 Android 设备/模拟器
62. ✅ 构建 APK（app-debug.apk，51MB）

### 阶段十二：联网搜索修复（6月21日）
63. ✅ ApiClient BASE_URL 从模拟器地址 10.0.2.2 → 公网 Vercel API（真机可用）
64. ✅ 增加 8 秒超时 + API 不可达自动回退本地库
65. ✅ OnlineSearchHelper 歌曲库从 30 首扩充到 160+ 首（20+ 位歌手）
66. ✅ 去掉 SoundHelix 假 demo 音频 URL
67. ✅ 添加歌曲流程改为：搜歌 → 弹文件选择器 → 选本地 MP3 → 可播放
68. ✅ MusicService 新增 onPlaybackError 回调，无音频时 Toast 提示
69. ✅ fragment_music.xml 分隔线从灰色改为半透明

### 阶段十三：NCM 解密与本地导入（6月21日）
70. ✅ 新增 NcmDecoder.java — 纯 Java NCM→MP3 解密（RC4 + AES-128-ECB）
71. ✅ 文件选择器自动识别 .ncm 后缀，自动解密并提取封面
72. ✅ 「我的音乐」新增 📂导入本地 按钮，一键选文件导入
73. ✅ 文件名自动解析「歌手 - 歌名」格式
74. ✅ music 文件夹 .ncm 全部解密为 .mp3（8首）
75. ✅ 启动时自动扫描 music 目录同步新文件
76. ✅ MusicFragment.onResume 实时扫描，切 Tab 自动更新
77. ✅ 热门推荐按 play_count DESC 排序（点越多越靠前）
78. ✅ 清理无音源歌曲（deleteSongsWithoutAudio）
79. ✅ GitHub 仓库建立并持续推送（ssh.github.com:443）
80. ✅ 无音频歌曲列表显示橙色「无音频」标签

---

## 9. 文件统计

### 代码文件统计

| 类别 | 数量 | 文件 |
|------|------|------|
| **Java 源文件** | 37 | Activity(6) + Fragment(3) + ViewModel(6) + Model(5) + DAO(5) + Database(1) + Adapter(4) + Service(1) + Receiver(1) + API(3) + Util(3) + Application(1) |
| **布局文件 (XML)** | 13 | activity_* + fragment_* + item_* + dialog_* |
| **资源文件 (XML)** | 24 | drawable(18) + values(3) + menu(1) |
| **Gradle 配置** | 3 | build.gradle ×2 + settings.gradle |
| **资源文件 (非代码)** | 8 | mp3(8) + jpg(2) |

### 代码量估算

| 模块 | 估算行数 |
|------|---------|
| Activity 层 | ~1200 行 |
| Fragment 层 | ~300 行 |
| ViewModel 层 | ~300 行 |
| Model 层 | ~300 行 |
| Database 层 | ~250 行 |
| Adapter 层 | ~200 行 |
| Service | ~250 行 |
| API / Util | ~300 行 |
| XML 布局 | ~800 行 |
| **总计** | **~4500 行** |

---

## 🔧 技术亮点

1. **MVVM 架构清晰分离** — ViewModel + LiveData + Room 异步查询
2. **NCM 原生解密** — 纯 Java 实现 NCM→MP3 解密，无需外部工具
3. **智能音频源查找** — 远程URL → 本地精确 → 模糊搜索 多级回退
4. **本地文件即时同步** — 启动/切Tab 自动扫描 music 目录入库
5. **在线搜索 + 离线回退** — API 优先，失败降级 160+ 首本地库
6. **CD封面旋转动画** — ObjectAnimator 无缝衔接播放/暂停状态
7. **歌词实时同步** — LinkedHashMap 时间轴匹配
8. **前台Service** — 后台播放 + 通知栏控制
9. **DiffUtil 优化** — ListAdapter 实现高效列表更新
10. **数据库外键级联** — 用户删除自动清理关联数据
11. **错误兜底处理** — Fragment/Activity 加载失败有 Toast 提示而非崩溃
12. **单例模式** — AppDatabase 双重检查锁，Retrofit 单例

---

## 📦 相关文件清单

```
D:\claude\安卓应用开发\
├── MusicPlayer\                     ← Android 项目源码
│   └── app/build/outputs/apk/debug/
│       └── app-debug.apk            ← 最新构建产物 (51MB)
├── PROJECT_SUMMARY.md               ← 本文件
├── music\                           ← 8首 .mp3 歌曲
├── pic\                             ← 图片素材
├── ncmdump_tool\ncmdump.exe         ← .ncm 解密工具（备选）
├── NeteaseCloudMusicApi\            ← (预留 API 服务目录)
└── .gitignore                       ← Git 排除规则
```

---

> **Git 仓库：** https://github.com/wulinxi/MusicPlayer  
> **首次提交：** 2026年6月21日 | **最新提交：** 2026年6月21日  
> **分支：** main | **文件数：** 108+
