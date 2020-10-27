# Database schema

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
