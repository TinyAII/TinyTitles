# 称号系统 TinyTitles

给玩家提供可自定义的称号（名称/颜色），显示在 TAB 面板、聊天栏和头顶名字前。多称号共存（默认显示1个可调），管理员发放，特殊称号进服全服通报。全中文界面，中英双语命令，零依赖。

## 功能特性

- **自定义称号**：名称/颜色由管理员定义，玩家名不上色、称号与名字空格隔开
- **三处显示**：TAB 面板 / 聊天栏 / 头顶名字前（Team prefix）
- **多称号共存**：可拥有多个称号，上限默认显示 1 个（`max-display` 可调）
- **管理员发放**：只做管理员发放，玩家可 GUI 勾选显示/隐藏
- **解锁状态 GUI**：显示所有称号，已解锁排前、未解锁灰显，鼠标悬停显示状态
- **特殊称号特权**：可设置进服全服通报（聊天栏，非屏幕中间，不打扰游戏）
- **动态创建**：管理员游戏内 /称号 创建 即可定义新称号

## 命令（中英双语）

| 功能 | 中文 | 英文 |
|---|---|---|
| 选择称号（GUI）| /称号 | /title |
| 使用显示 | /称号 使用 <称号1> [称号2] [称号3] | /title use ... |
| 我的称号 | /称号 列表 | /title list |
| 清除显示 | /称号 清除 | /title clear |
| 发放 | /称号 给予 <玩家> <称号> | /title give <p> <t> |
| 收回 | /称号 收回 <玩家> <称号> | /title remove <p> <t> |
| 创建称号 | /称号 创建 <名字> <颜色如&e> [描述] | /title create <n> <c> |
| 重载 | /称号 重载 | /title reload |

## 配置（plugins/TinyTitles/config.yml）

```yaml
settings:
  max-display: 1        # 玩家最多同时显示称号数（默认1）
  show-in-tab: true     # TAB 面板显示
  show-in-chat: true    # 聊天栏显示
  show-in-nametag: true # 头顶名字显示（Team prefix）

titles:
  王公贵族:
    color: "&e"
    desc: "王公贵族，身份尊贵"
    broadcast-on-join: true   # 进服全服通报（聊天栏）
```

## 权限

- `titles.use`：使用称号功能（默认 true）
- `titles.admin`：称号管理（默认 op）

## 安装

1. 下载 jar 放入 `plugins/` 目录
2. 重启服务器（或 reload）
3. 启动日志显示 TinyAII 横幅 + 称号系统已启用

> 需要 Java 17+，支持 Paper/Spigot 1.16 ~ 26.2。零依赖。

---

# TinyTitles - Title System

Custom titles for players (name/color), shown in TAB, chat and above head. Multiple titles (display limit configurable), admin-issued only, special titles broadcast on join. Full Chinese UI, bilingual commands, zero dependency.

## Features

- Custom titles with color, player name stays default color
- Display in: TAB / chat / nametag (Team prefix)
- Multiple titles, display limit default 1 (configurable `max-display`)
- Admin-only issue, players toggle display via GUI
- GUI shows all titles: unlocked first, locked grayed
- Special titles broadcast on join (chat, not intrusive)
- Dynamic title creation in-game

## Commands

- `/title` GUI, `/title use <title1> [title2]...`, `/title list`, `/title clear`
- `/title give <player> <title>`, `/title remove <player> <title>`, `/title create <name> <color> [desc]`, `/title reload`

## Install

1. Put jar into `plugins/`
2. Restart server (or reload)
3. Startup log shows TinyAII banner + title system enabled

> Java 17+, Paper/Spigot 1.16 ~ 26.2. Zero dependency.

## License

MIT License - free, open source. TinyAII brand banner preserved.
