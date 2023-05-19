# 编译原理实验
## lab1 设计词法分析器
> 词法分析器要求<br>
    关键字: int, if, for<br>
    标识符: 字母(字母|数字)*<br>
    无符号数：包括科学计数法，浮点数，整数<br>
    运算符或分界符：=, *, +, ++, =, {, },<br>

```
源码在package lexer1中
```

## lab2 设计非递归的语法分析器
> 求解算法步骤:<br>
> 1.消除左递归(包括直接左递归和间接左递归)<br> 
> 2.求所有非终结符的First集<br> 
> 3.求所有非终结符的Follow集<br>
> 4.求所有候选式的Select集  
> 5.构建分析表  
> 6.构建分析栈  

```
源码在package parser中
```

# *This project needs patience to tackle some challenge, and very worthy.*
These pictures show the time that I spent in this project. Recording by Wakatime
![这是wakatime记录的时间](resource/updateTimeRecord.png "wakatime")
![这是wakatime记录的时间](resource/updateTimeRecord2.png "wakatime")





