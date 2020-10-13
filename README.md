[![Build Status](https://travis-ci.org/nowtryz/YJump.svg?branch=master)](https://travis-ci.org/nowtryz/YJump)

YJump plugin
============

> Plugin designed for [YCraft](https://ycraft.fr)

## Plugin information

### Roadmap
There are a lot of things to come, you can follow the development progress by taking a look to
[my roadmap](https://www.notion.so/nowtryz/ae979233fb1e4599ba3b148608918f96)

### Permissions:

| Node                            | Default | Description                                               | Children
|---------------------------------|:-------:|-----------------------------------------------------------|---------
| `jump.*`                        | no      | Gives access to all jump permissions                      | `jump.list`, `jump.play`, `jump.help`, `jump.fly`, `jump.admin.*`
| `jump.list`                     | yes     | Gives access to the `/jumps` command                      | *none*
| `jump.play`                     | yes     | Enables player to play a jump and leave jumps             | *none*
| `jump.help`                     | yes     | Give access to the `/jump help` command                   | *none*
| `jump.fly`                      | no      | Enables player to fly in jumps                            | *none*
| `jump.admin.*`                  | no      | Gives access to admin commands                            | `jump.admin.list`, `jump.admin.reload`, `jump.admin.create`, `jump.admin.edit`, `jump.admin.editorinteractions`
| `jump.admin.list`               | no      | Give access to the `/jump list` command                   | *none*
| `jump.admin.reload`             | no      | Gives access to the `/jump reload` command                | *none*
| `jump.admin.create`             | no      | Gives access to the `/jump create` command and edit jumps | `jump.admin.edit`
| `jump.admin.edit`               | no      | Enables the player to edit jumps                          | *none*
| `jump.admin.editorinteractions` | no      | Allow to places/destroy blocks while in editor (to use whith creative editor, cf configuration) | *none*

### Commands
All commands can be seen in games using `/jump help`

## Database schema

![Schema](http://yuml.me/91c8cd14.svg)
<!--
[Score{bg:yellowgreen}]0..*-1>[Jump{bg:orange}]
[Jump]<>1-checkpoints 0..*>[Location]
[Jump]<>1-spawn 0..1>[Location]
[Jump]<>1-start 0..1>[Location]
[Jump]<>1-end 0..1>[Location]

// Add more detail
[Jump|name: string; description: string|spawn: Location;start: Location; end: Location; item: ItemStack (serialized)]
[Score|player: UUID; duration: long]
[Location|world: string; x: double; y: double; z: double; pitch: float; yaw: float]
-->
