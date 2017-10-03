# TheLights
利用MatrixLayout实现的 关灯游戏 （开发中）

![github](https://github.com/HYY-yu/TheLights/blob/master/demo.gif "show")

## MatrixLayout 介绍
用于显示一个 n x m 大小的整数矩阵，映射到内部的ChildView的Background中。如：通过 addStatus(0,drawable) 添加矩阵的0元素映射，addStatus(1,drawable) 
添加矩阵的1元素映射。
> 其内部维护一个整数矩阵，特点是可以自己记录所有的操作历史，并提供重放（从头至尾放映操作历史），重现（复现上步操作），回退（回退到上步操作）。

所以可以作为一些经典游戏的框架，如 ：贪吃蛇，俄罗斯方块，关灯，五子棋等方格型游戏。
