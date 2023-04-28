# 鬥地主殘局計算器

## 使用簡介
騰訊歡樂鬥地主規則
只算第一手牌，沒有遞歸交互

python 示例

```
comp = [K,K,Q,9,6,6,4,4]
mine = [JOKER,7,7,5,5,3]
comp = Pokers(comp)
mine = Pokers(mine)
if dfs(mine, comp, True):
    solution()
# 我方先出，計算第一手牌
```

```
comp = [K,K,Q,9,4,4]
mine = [JOKER,7,7,3]
comp = Pokers(comp)
mine = Pokers(mine)
if dfs(mine, comp, True, [6, 6]):
    solution()
# 電腦出對6，輪到我方
```

```
comp = [K,K,Q,9,6,6,4]
mine = [JOKER,7,7,5,5,3]
comp = Pokers(comp)
mine = Pokers(mine)
if dfs(mine, comp, True, 4):
    solution()
# 電腦對4，輪到我方
```

## 參數說明
### Python
```
dfs(無需理會, 無需理會, 輪到我方出牌, 上一手牌（None為Pass或開局，單張為int，否則為數組））
```
### Java
```
happyPoker.dfs(true /*輪到我方出牌*/, new int[]{} /*上一手牌，空為Pass或開局*/)
```

### 上一手牌參數數組順序
順子
```
dfs(mine, comp, True, [3,4,5,6,7])
```
三帶一
```
dfs(mine, comp, True, [3,3,3,4])
```
三帶一對
```
dfs(mine, comp, True, [3,3,3,4,4])
```
連對
```
dfs(mine, comp, True, [3,3,4,4,5,5])
```
四帶二
```
dfs(mine, comp, True, [4,4,4,4,5,5])
```
飛機
```
dfs(mine, comp, True, [4,4,4,5,5,5,3,6])
```

## 其它
某些複雜牌局需調大Java堆空間
```
java -Xmx8g HappyPoker
```
